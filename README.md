## 📘 Academic Context

This project was developed as part of the **Data Structures** course.
It demonstrates practical implementation of core data structure concepts such as:
- Graph representation (URL link graph)
- Queue-based traversal
- Hash-based visited tracking
- Concurrent data structures



\# Multithreaded Web Crawler in Java



A Java-based multithreaded web crawler that efficiently crawls web pages within a domain using concurrency, politeness delay, and link graph generation.



\## 🔧 Technologies Used

\- Java

\- Maven

\- JSoup (HTML parsing)

\- Gson (JSON generation)

\- ExecutorService (Multithreading)



\## ✨ Key Features

\- Multithreaded crawling using thread pool

\- Domain-restricted crawling

\- Politeness delay per host to avoid overloading servers

\- Detection of dead links (HTTP status ≥ 400)

\- Thread-safe data structures (ConcurrentHashMap, ConcurrentLinkedQueue)

\- Generates crawl graph in JSON format



\## ▶️ How to Run

```bash

mvn clean compile

mvn exec:java



