package ru.spbau.ir.books;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.net.URL;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

class Frontier {
    private static final Comparator<Crawler.WebsiteAndUrl> queueComparator = Comparator
            .comparingInt(o -> o.site.getLastUpdated() + o.site.getDelayTime());
    private final PriorityBlockingQueue<Crawler.WebsiteAndUrl> queue;

    Frontier(Path mainURLs, Path unhandled) {
        queue = new PriorityBlockingQueue<>(1, queueComparator);
        List<URL> firstURLs = new ArrayList<>();
        try {
            Files.lines(mainURLs, StandardCharsets.UTF_8).forEach(str -> {
                try {
                    firstURLs.add(new URL(str));
                } catch (MalformedURLException ignored) {
                    ignored.printStackTrace();
                }
            });
        } catch (IOException ignored) {
            ignored.printStackTrace();
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
            if (website.isInnerUrl(url))
                queue.add(new Crawler.WebsiteAndUrl(website, url));
        }
    }
}
