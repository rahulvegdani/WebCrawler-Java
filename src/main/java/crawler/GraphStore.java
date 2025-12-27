package crawler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GraphStore {

    private final Map<String, List<String>> graph = new ConcurrentHashMap<>();

    public void addNode(String url, List<String> links) {
        if (links == null) links = Collections.emptyList();
        graph.put(url, links);
    }

    public Map<String, List<String>> getGraph() {
        return graph;
    }
}
