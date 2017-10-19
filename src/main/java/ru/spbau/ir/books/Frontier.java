package ru.spbau.ir.books;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Frontier {
    private HashMap<Website, URL> websites;
    private Queue<URL> queue;

    Frontier(List<URL> firstURLs) {
        queue = new LinkedList<>();
        for (URL firstURL : firstURLs) {
            websites.put(new Website(), firstURL);
            queue.add(firstURL);
        }
    }

    public boolean done() {
        return queue.isEmpty();
    }

    public Crawler.WebsiteAndUrl nextSite() {
        URL current = queue.remove();

        return null;
    }

    public void addUrl(List<URL> url, Website website) {

    }

    public void releaseSite(Website website) {

    }
}
