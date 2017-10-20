package ru.spbau.ir.books;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Crawler {

    private final Map<URL, Path> processedUrls = new HashMap<>();
    private final Path pathForStoring;
    private final Path handledURLs;
    private final Path mainURLs;
    private Frontier frontier;

    public Crawler(Path forStoring, Path handledURLs, Path mainUrls) {
        pathForStoring = forStoring;
        this.handledURLs = handledURLs;
        this.mainURLs = mainUrls;
        frontier = new Frontier(mainUrls, handledURLs);
    }

    public void crawlerThread() {
        int documentsCounter = 1;
        while (!frontier.done()) {
            WebsiteAndUrl siteAndUrl = frontier.nextSite();
            Website site = siteAndUrl.site;
            URL url = siteAndUrl.url;
            if (!processedUrls.containsKey(url)) {
                if (site.permitsCrawl(url)) {
                    Document document = site.getDocument(url);
                    document.store(pathForStoring, documentsCounter);
                    documentsCounter++;
                    processedUrls.put(url, pathForStoring);
                    frontier.addUrl(document.parse(), site);
                }
            }
        }
    }

    public static void main(String[] args) {
        Path mainPath = Paths.get("mainUrls.txt");
        Path handledUrlsPath = Paths.get("handledUrls.txt");
        Path pageStorageDir = Paths.get("pageStorage");
        Crawler crawler = new Crawler(pageStorageDir, handledUrlsPath, mainPath);
        crawler.crawlerThread();
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
