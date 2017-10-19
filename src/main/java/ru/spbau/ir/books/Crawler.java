package ru.spbau.ir.books;

import java.net.URL;

public class Crawler {

    public void crawlerThread(Frontier frontier) {
        while (!frontier.done()) {
            Website website = frontier.nextSite();
            URL url = website.nextURL();
            if (website.permitsCrawl(url)) {
                Document text = retrieveUrl(url);
                storeDocument(url, text);
                for (URL urlItem : text.parse()) {
                    frontier.addUrl(urlItem);
                }
            }
            frontier.releaseSite(website);
        }
    }

    private void storeDocument(URL url, Document text) {

    }

    private Document retrieveUrl(URL url) {
        return null;
    }
}
