package ru.spbau.ir.books;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Frontier {
    Queue<URL> queue;

    Frontier(URL firstUrl) {
        queue = new LinkedList<>();
        queue.add(firstUrl);
    }

    public boolean done() {
        return queue.isEmpty();
    }

    public Crawler.WebsiteAndUrl nextSite() {
        URL current = queue.remove();
        /* обработка url */
        return null;
    }

    public void addUrl(List<URL> url) {

    }

    public void releaseSite(Website website) {

    }
}
