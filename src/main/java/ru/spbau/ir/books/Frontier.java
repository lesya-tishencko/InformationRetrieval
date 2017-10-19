package ru.spbau.ir.books;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Frontier {
    private Queue<Crawler.WebsiteAndUrl> queue;

    Frontier(Path mainURLs, Path unhandled) {
        queue = new LinkedList<>();
        List<URL> firstURLs = new ArrayList<>();
        try {
            Files.lines(mainURLs, StandardCharsets.UTF_8).forEach(str -> {
                try {
                    firstURLs.add(new URL(str));
                } catch (MalformedURLException e) {
                    System.out.println(e.getMessage());
                }
            });
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        for (URL firstURL : firstURLs) {
            Website website = new Website(firstURL, unhandled);
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
