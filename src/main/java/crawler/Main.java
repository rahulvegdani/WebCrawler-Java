package crawler;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the URL to crawl: ");
        String startUrl = scanner.nextLine().trim();

        System.out.print("Enter maximum number of pages to crawl: ");
        int maxPages = 50;
        try {
            maxPages = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, using default 50 pages.");
        }

        System.out.print("Enter number of threads: ");
        int threads = 10;
        try {
            threads = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, using default 10 threads.");
        }

        System.out.print("Enter politeness delay (ms) between requests to same host (suggest 500-2000): ");
        int politeness = 1000;
        try {
            politeness = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, using default 1000 ms.");
        }

        // Start crawler
        WebCrawler crawler = new WebCrawler(threads, politeness);
        crawler.startCrawling(startUrl, maxPages);

        scanner.close();
    }
}
