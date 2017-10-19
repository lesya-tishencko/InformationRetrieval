package ru.spbau.ir.books;

import java.io.IOException;
import java.net.URL;
import java.io.InputStream;
import com.panforge.robotstxt.RobotsTxt;
import org.jsoup.Jsoup;

public class Website {
    private RobotsTxt robots;
    private URL mainURL;
    private String userAgent = "AUcrawler Angelika&Lesya/1.0 (+l.tishencko@geoscan.aero)";

    public Website(URL siteUrl) {
        mainURL = siteUrl;
        try (InputStream robotsTxtStream = new URL(siteUrl.getPath() + "/robots.txt").openStream()) {
            RobotsTxt robotsTxt = RobotsTxt.read(robotsTxtStream);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean permitsCrawl(URL url) {
        return robots.query(userAgent, url.getPath());
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
