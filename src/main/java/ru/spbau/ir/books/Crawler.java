package ru.spbau.ir.books;

import javafx.scene.shape.Path;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class Crawler {

    private Set<URL> sets = new HashSet<URL>();
    private Path pathForStoring;

    public void crawlerThread(Frontier frontier) {
        while (!frontier.done()) {
            WebsiteAndUrl siteAndUrl = frontier.nextSite();
            Website site = siteAndUrl.site;
            URL url = siteAndUrl.url;
            if (!sets.contains(url)) {
                if (site.permitsCrawl(url)) {
                    Document document = site.getDocument();
                    document.store(pathForStoring);
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
