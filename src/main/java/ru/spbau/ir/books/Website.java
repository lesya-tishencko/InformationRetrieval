package ru.spbau.ir.books;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import com.panforge.robotstxt.RobotsTxt;
import java.nio.file.Path;
import org.jsoup.Jsoup;

public class Website {
    private RobotsTxt robots;
    private final URL mainURL;
    private final Set<URL> unhandled;
    private final String userAgent = "AUcrawler Angelika&Lesya/1.0 (+l.tishencko@geoscan.aero)";

    public Website(URL siteUrl, Path unhandledURL) {
        mainURL = siteUrl;
        unhandled = processUnhandledUrls(unhandledURL);
        try (InputStream robotsTxtStream = new URL(siteUrl.getPath() + "/robots.txt").openStream()) {
            RobotsTxt robotsTxt = RobotsTxt.read(robotsTxtStream);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private Set<URL> processUnhandledUrls(Path unhandledURL) {
        Set<URL> urls = new HashSet<>();
        try {
            Files.lines(unhandledURL, StandardCharsets.UTF_8)
                    .filter(url -> url.startsWith(mainURL.getPath()))
                    .forEach(url -> {
                        try {
                            urls.add(new URL(url));
                        } catch (MalformedURLException e) {
                            System.out.println(e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return urls;
    }

    public boolean permitsCrawl(URL url) {
        return robots.query(userAgent, url.getPath()) && !unhandled.contains(url);
    }

    public Document getDocument(URL url) {
        org.jsoup.nodes.Document innerDocument = null;
        try {
            innerDocument = Jsoup.connect(url.getPath())
                    .data("query", "Java")
                    .userAgent(userAgent)
                    .get();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return new Document(innerDocument);
    }
}
