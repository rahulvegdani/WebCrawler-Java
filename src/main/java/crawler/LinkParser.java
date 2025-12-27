package crawler;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.util.*;

public class LinkParser {

    public static List<String> getLinks(String url) throws Exception {
        Connection conn = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                           "AppleWebKit/537.36 (KHTML, like Gecko) " +
                           "Chrome/120.0.0.0 Safari/537.36")
                .timeout(10000)
                .ignoreHttpErrors(true)
                .followRedirects(true);

        Document doc = conn.get();
        if (doc == null) return Collections.emptyList();

        Elements anchors = doc.select("a[href]");
        List<String> urls = new ArrayList<>(anchors.size());
        for (Element a : anchors) {
            String href = a.absUrl("href");
            if (href == null || href.isEmpty()) continue;
            // normalize remove trailing fragment
            int idx = href.indexOf('#');
            if (idx > 0) href = href.substring(0, idx);
            urls.add(href);
        }
        return urls;
    }
}
