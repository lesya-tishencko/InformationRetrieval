package ru.spbau.ir.books;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Crawler {

    private final Map<URL, Path> processedUrls = new HashMap<>();
    private final Path pathForStoring;
    private final Path handledURLs;
    private final Path mainURLs;
    private Frontier frontier;
    private int threadsNumber = 100;
    private List<Thread> threadsList = new ArrayList<>();
    private volatile int docsCounter = 1;

    public Crawler(Path forStoring, Path handledURLs, Path mainUrls) {
        pathForStoring = forStoring;
        this.handledURLs = handledURLs;
        this.mainURLs = mainUrls;
        frontier = new Frontier(mainUrls, handledURLs);
        for (int i = 0; i < threadsNumber; i++) {
            threadsList.add(new Thread(this::crawlerThread));
            threadsList.get(i).start();
        }
        for (Thread thread : threadsList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    public void crawlerThread() {
        while (!frontier.done()) {
            WebsiteAndUrl siteAndUrl = frontier.nextSite();
            Website site = siteAndUrl.site;
            URL url = siteAndUrl.url;
            if (!processedUrls.containsKey(url)) {
                if (site.permitsCrawl(url)) {
                    Document document = site.getDocument(url);
                    document.store(pathForStoring, docsCounter);
                    docsCounter++;
                    processedUrls.put(url, pathForStoring);
                    frontier.addUrl(document.parse(), site);
                }
            }
        }
    }

    public static void main(String[] args) {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        Path mainPath = Paths.get(loader.getResource("mainUrls.txt").getPath());
        Path handledUrlsPath = Paths.get(loader.getResource("handledUrls.txt").getPath());
        Path pageStoragePath = Paths.get(loader.getResource("pageStorage").getPath());
        Crawler crawler = new Crawler(pageStoragePath, handledUrlsPath, mainPath);
    }

    static class WebsiteAndUrl {
        Website site;
        URL url;

        WebsiteAndUrl(Website site, URL url) {
            this.site = site;
            this.url = url;
        }
    }
}
