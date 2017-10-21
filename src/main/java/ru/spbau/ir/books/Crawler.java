package ru.spbau.ir.books;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Crawler {

    private final Map<URL, Path> processedUrls = new HashMap<>();
    private final Set<URL> seen = new HashSet<>();
    private final Path pathForStoring;
    private final Frontier frontier;
    private final AtomicInteger docsCounter = new AtomicInteger(1);

    private Crawler(Path forStoring, Path handledURLs, Path mainUrls) {
        pathForStoring = forStoring;
        Path handledURLs1 = handledURLs;
        Path mainURLs = mainUrls;
        frontier = new Frontier(mainUrls, handledURLs);
        int threadsNumber = 100;
        List<Thread> threadsList = new ArrayList<>();
        for (int i = 0; i < threadsNumber; i++) {
            threadsList.add(new Thread(this::crawlerThread));
            threadsList.get(i).start();
        }
        for (Thread thread : threadsList) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    public static void main(String[] args) {
        String path = System.getProperty("user.dir");
        Path mainPath = Paths.get(path + "/build/resources/main/mainUrls.txt");
        Path handledUrlsPath = Paths.get(path + "/build/resources/main/handledUrls.txt");
        Path pageStoragePath = Paths.get(path + "/build/resources/main/pageStorage");
        new Crawler(pageStoragePath, handledUrlsPath, mainPath);

        /* for Linux
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        Path mainPath = Paths.get(loader.getResource("mainUrls.txt").getPath());
        Path handledUrlsPath = Paths.get(loader.getResource("handledUrls.txt").getPath());
        Path pageStoragePath = Paths.get(loader.getResource("pageStorage").getPath());
        Crawler crawler = new Crawler(pageStoragePath, handledUrlsPath, mainPath);*/
    }

    private void crawlerThread() {
        while (!frontier.done()) {
            WebsiteAndUrl siteAndUrl = frontier.nextSite();
            Website site = siteAndUrl.site;
            URL url = siteAndUrl.url;
            if (!seen.contains(url)) {
                seen.add(url);
                if (site.permitsCrawl(url)) {
                    Document document = site.getDocument(url);
                    document.store(pathForStoring, docsCounter.get());
                    docsCounter.incrementAndGet();
                    processedUrls.put(url, pathForStoring);
                    frontier.addUrl(document.parse(), site);
                }
            }
        }
    }

    static class WebsiteAndUrl {
        final Website site;
        final URL url;

        WebsiteAndUrl(Website site, URL url) {
            this.site = site;
            this.url = url;
        }
    }
}
