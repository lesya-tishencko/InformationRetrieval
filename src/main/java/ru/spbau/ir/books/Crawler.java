package ru.spbau.ir.books;

import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Crawler {

    private final Map<URL, Path> processedUrls = new HashMap<>();
    private final Path pathForStoring;
    private final Path unhandledUrls;
    private final Path mainUrls;
    private Frontier frontier;

    public Crawler(Path forStoring, Path unhandledUrls, Path mainUrls) {
        pathForStoring = forStoring;
        this.unhandledUrls = unhandledUrls;
        this.mainUrls = mainUrls;
        frontier = new Frontier(mainUrls, unhandledUrls);
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

    static class WebsiteAndUrl {
        Website site;
        URL url;

        WebsiteAndUrl(Website site, URL url) {
            this.site = site;
            this.url = url;
        }
    }
}
