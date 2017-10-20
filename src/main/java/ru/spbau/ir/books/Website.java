package ru.spbau.ir.books;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.panforge.robotstxt.RobotsTxt;
import java.nio.file.Path;
import org.jsoup.Jsoup;

import static java.lang.Math.max;

public class Website {
    private RobotsTxt robots;
    private final URL mainURL;
    private final List<URL> handled = new ArrayList<>();
    private final String userAgent = "AUbooks_bot";
    private int delayTime = 1000;

    public Website(URL siteUrl, Path unhandledURL) {
        mainURL = siteUrl;
        processHandledUrls(unhandledURL);
        try (InputStream robotsTxtStream = new URL(siteUrl.toString() + "/robots.txt").openStream()) {
            robots = RobotsTxt.read(robotsTxtStream);
            delayTime = max(robots.getCrawlDelay(), 1000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void processHandledUrls(Path unhandledURL) {
        try {
            Files.lines(unhandledURL, StandardCharsets.UTF_8)
                    .filter(url -> url.startsWith(mainURL.toString()))
                    .forEach(url -> {
                        try {
                            handled.add(new URL(url));
                        } catch (MalformedURLException e) {
                            System.out.println(e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public int getDelayTime() {
        return delayTime;
    }

    public boolean permitsCrawl(URL url) {
        boolean isAllow = robots.query(userAgent, url.toString());
        if (handled.size() > 0) {
            isAllow = isAllow && (url.equals(mainURL)
                    || handled.stream().filter(handUrr -> url.toString().startsWith(handUrr.toString())).count() != 0);
        }
        return isAllow;
    }

    public Document getDocument(URL url) {
        org.jsoup.nodes.Document innerDocument = null;
        try {
            innerDocument = Jsoup.connect(url.toString())
                    .userAgent(userAgent)
                    .get();
            Thread.sleep(1000);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Document(innerDocument);
    }
}
