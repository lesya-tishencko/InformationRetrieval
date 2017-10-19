package ru.spbau.ir.books;

import java.net.URL;
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
        while (!frontier.done()) {
            WebsiteAndUrl siteAndUrl = frontier.nextSite();
            Website site = siteAndUrl.site;
            URL url = siteAndUrl.url;
            if (!processedUrls.containsKey(url)) {
                if (site.permitsCrawl(url)) {
                    Document document = site.getDocument(url);
                    document.store(pathForStoring);
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
