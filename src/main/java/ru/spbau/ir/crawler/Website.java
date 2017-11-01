package ru.spbau.ir.crawler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.panforge.robotstxt.RobotsTxt;
import java.nio.file.Path;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

class Website {
    private static final String userAgent = "AUbooks_bot";
    private final URL mainURL;
    private final List<URL> handled = new ArrayList<>();
    private RobotsTxt robots;
    private LocalDateTime updateTime = null;
    private Integer delayTime = 1000;

    public Website(URL siteUrl, Path unhandledURL) {
        mainURL = siteUrl;
        processHandledUrls(unhandledURL);
        byte[] robotsTxtContentsBytes = null;
        try {
            robotsTxtContentsBytes = Jsoup.connect(siteUrl.toString() + "/robots.txt")
                    .userAgent(userAgent)
                    .timeout(delayTime)
                    .execute()
                    .bodyAsBytes();
        } catch (IOException ignored) {
        }

        try (InputStream robotsTxtStream = new ByteArrayInputStream(robotsTxtContentsBytes)){
            robots = RobotsTxt.read(robotsTxtStream);
        } catch (IOException ignored) {
        }
        int delayTimeFromRobotsTxt = getCrawlDelay();
        delayTime = delayTimeFromRobotsTxt == 0? 500 : delayTimeFromRobotsTxt;
    }

    private int getCrawlDelay() {
        Connection connection = Jsoup.connect(mainURL.toString() + "/robots.txt")
                .userAgent(userAgent)
                .timeout(delayTime);
        String robotsTxtContents = null;
        try {
            robotsTxtContents = connection.execute().body();
        } catch (IOException ignored) {
        }
        String[] lines = robotsTxtContents.split("\\n");
            for (String line : lines) {
                if (line.startsWith("Crawl-delay: ")) {
                    String delayString = line.split(": ")[1];
                    return (int)Double.parseDouble(delayString) * 1000;
                }
            }
        return 0;
    }
    
    private void processHandledUrls(Path unhandledURL) {
        try {
            Files.lines(unhandledURL, StandardCharsets.UTF_8)
                    .filter(url -> url.startsWith(mainURL.toString()))
                    .forEach(url -> {
                        try {
                            handled.add(new URL(url));
                        } catch (MalformedURLException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    public int getDelayTime() {
        return delayTime;
    }

    public int getLastUpdated() {
        if (updateTime == null)
                return 0;
        return updateTime.getSecond();
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
            int remainTime = canUpdate();
            if (remainTime > 0)
                Thread.sleep(remainTime);
            updateTime = LocalDateTime.now();
            innerDocument = Jsoup.connect(url.toString())
                    .userAgent(userAgent)
                    .get();
        } catch (Exception ignored) {
        }
        return new Document(innerDocument);
    }

    public boolean isInnerUrl(URL url) {
        return url.toString().startsWith(mainURL.toString());
    }

    private int canUpdate() {
        if (updateTime == null)
            return 0;
        return LocalDateTime.now().getSecond() - updateTime.getSecond() + Math.toIntExact(Math.round(delayTime));
    }
}
