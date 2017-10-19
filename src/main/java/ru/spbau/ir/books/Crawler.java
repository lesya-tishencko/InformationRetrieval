package ru.spbau.ir.books;

import java.nio.file.Path;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Crawler {

    private Map<URL, Document> processedUrls = new HashMap<URL, Document>();
    private Path pathForStoring;

    public void crawlerThread(Frontier frontier) {
        while (!frontier.done()) {
            WebsiteAndUrl siteAndUrl = frontier.nextSite();
            Website site = siteAndUrl.site;
            URL url = siteAndUrl.url;
            if (!processedUrls.containsKey(url)) {
                if (site.permitsCrawl(url)) {
                    Document document = site.getDocument(url);
                    document.store(pathForStoring);
                    processedUrls.put(url, document);
                    frontier.addUrl(document.parse());
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
