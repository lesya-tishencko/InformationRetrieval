package ru.spbau.ir.books;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Frontier {
    private Queue<Crawler.WebsiteAndUrl> queue;

    Frontier(List<URL> firstURLs) {
        queue = new LinkedList<>();
        for (URL firstURL : firstURLs) {
            Website website = new Website();
            queue.add(new Crawler.WebsiteAndUrl(website, firstURL));
        }
    }

    public boolean done() {
        return queue.isEmpty();
    }

    public Crawler.WebsiteAndUrl nextSite() {
        return queue.remove();
    }

    public void addUrl(List<URL> urls, Website website) {
        for (URL url : urls) {
            queue.add(new Crawler.WebsiteAndUrl(website, url));
        }
    }
}
