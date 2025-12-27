package crawler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import com.google.gson.Gson;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.URI;
import java.net.URISyntaxException;

public class WebCrawler {

    private final int threadCount;
    private final int politenessMillis;
    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();
    private final GraphStore graphStore = new GraphStore();
    private final Set<String> deadLinks = ConcurrentHashMap.newKeySet();

    // throttle: last access time per host (ms)
    private final ConcurrentHashMap<String, Long> hostLastAccess = new ConcurrentHashMap<>();

    public WebCrawler(int threadCount, int politenessMillis) {
        this.threadCount = threadCount;
        this.politenessMillis = politenessMillis;
    }

    public void startCrawling(String startUrl, int maxPages) {
        if (startUrl == null || startUrl.isEmpty()) {
            System.out.println("Start URL empty. Aborting.");
            return;
        }

        String startDomain = extractHost(startUrl);
        if (startDomain == null) startDomain = "";

        queue.add(startUrl);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);

        try {
            while ((visited.size() < maxPages)) {
                String url = queue.poll();
                if (url == null) {
                    // no url right now; wait briefly for tasks to add more
                    if (executor.getActiveCount() == 0 && queue.isEmpty()) break;
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }

                // mark visited (thread-safe)
                boolean newlyAdded = visited.add(url);
                if (!newlyAdded) continue;

                // submit task; pass shared structures and shared hostLastAccess & policy
                executor.submit(new CrawlerTask(
                        url, queue, visited, graphStore, deadLinks,
                        startDomain, hostLastAccess, politenessMillis));
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            saveResults();
        }
    }

    private String extractHost(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private void saveResults() {
        // visited links
        try (FileWriter fw = new FileWriter("visited_links.csv")) {
            for (String url : visited) {
                fw.write(url + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing visited_links.csv: " + e.getMessage());
        }

        // dead links
        try (FileWriter fw = new FileWriter("dead_links.csv")) {
            for (String url : deadLinks) {
                fw.write(url + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing dead_links.csv: " + e.getMessage());
        }

        // graph JSON
        try (FileWriter fw = new FileWriter("graph.json")) {
            Gson gson = new Gson();
            gson.toJson(graphStore.getGraph(), fw);
        } catch (IOException e) {
            System.err.println("Error writing graph.json: " + e.getMessage());
        }

        System.out.println("Crawling complete!");
        System.out.println("Visited links: " + visited.size());
        System.out.println("Dead links: " + deadLinks.size());
    }
}
