package crawler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CrawlerTask implements Runnable {

    private final String url;
    private final Queue<String> queue;
    private final Set<String> visited;
    private final GraphStore graphStore;
    private final Set<String> deadLinks;
    private final String startDomain;
    private final ConcurrentHashMap<String, Long> hostLastAccess;
    private final int politenessMillis;

    public CrawlerTask(String url, Queue<String> queue, Set<String> visited,
                       GraphStore graphStore, Set<String> deadLinks,
                       String startDomain,
                       ConcurrentHashMap<String, Long> hostLastAccess,
                       int politenessMillis) {
        this.url = url;
        this.queue = queue;
        this.visited = visited;
        this.graphStore = graphStore;
        this.deadLinks = deadLinks;
        this.startDomain = startDomain;
        this.hostLastAccess = hostLastAccess;
        this.politenessMillis = politenessMillis;
    }

    @Override
    public void run() {
        try {
            // politeness: ensure gap between requests to same host
            enforcePoliteness(url);

            // check status of the page itself
            int status = HttpClientUtil.getStatus(url);
            if (status >= 400) {
                System.out.println("❌ Dead Link: " + url + " (Status " + status + ")");
                deadLinks.add(url);
                graphStore.addNode(url, Collections.emptyList());
                return;
            }

            // parse links (JSoup)
            List<String> links = LinkParser.getLinks(url);
            if (links == null) links = Collections.emptyList();

            // add node to graph (even if empty)
            graphStore.addNode(url, links);

            // queue outgoing links (only same domain)
            for (String link : links) {
                if (link == null || link.isEmpty()) continue;
                if (!isSameDomain(link)) continue; // domain restriction

                // add to queue if not visited / not already queued
                if (!visited.contains(link) && !queue.contains(link)) {
                    queue.add(link);
                }
            }

            System.out.println("Crawled: " + url + " | Links found: " + links.size() + " | Queue size: " + queue.size());

        } catch (Exception e) {
            System.out.println("⚠ Error fetching " + url + ": " + e.getMessage());
            deadLinks.add(url);
            graphStore.addNode(url, Collections.emptyList());
        }
    }

    private boolean isSameDomain(String link) {
        try {
            URI uri = new URI(link);
            String host = uri.getHost();
            if (host == null) return false;
            // compare ignoring leading www.
            return normalizeHost(host).endsWith(normalizeHost(startDomain));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private String normalizeHost(String host) {
        if (host == null) return "";
        return host.startsWith("www.") ? host.substring(4) : host;
    }

    private void enforcePoliteness(String urlStr) {
        try {
            URI uri = new URI(urlStr);
            String host = uri.getHost();
            if (host == null) return;

            long now = System.currentTimeMillis();
            Long last = hostLastAccess.get(host);
            if (last != null) {
                long diff = now - last;
                if (diff < politenessMillis) {
                    long wait = politenessMillis - diff;
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            hostLastAccess.put(host, System.currentTimeMillis());
        } catch (Exception e) {
            // ignore
        }
    }
}
