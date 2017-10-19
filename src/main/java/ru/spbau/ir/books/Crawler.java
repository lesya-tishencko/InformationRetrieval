package ru.spbau.ir.books;

import javafx.scene.shape.Path;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Crawler {

    private Map<URL, Document> maps = new HashMap<URL, Document>();
    private Path pathForStoring;

    public void crawlerThread(Frontier frontier) {
        while (!frontier.done()) {
            WebsiteAndUrl siteAndUrl = frontier.nextSite();
            Website site = siteAndUrl.site;
            URL url = siteAndUrl.url;
            if (!maps.containsKey(url)) {
                if (site.permitsCrawl(url)) {
                    Document document = site.getDocument();
                    document.store(pathForStoring);
                    maps.put(url, document);
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
