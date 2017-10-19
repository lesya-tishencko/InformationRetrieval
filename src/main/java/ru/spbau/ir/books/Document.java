package ru.spbau.ir.books;


import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Document {
    private org.jsoup.nodes.Document htmlPage;

    Document(org.jsoup.nodes.Document htmlPage) {
        this.htmlPage = htmlPage;
    }

    public List<URL> parse() {
        Elements links = htmlPage.select("a[href");
        List<String> urlsStrings = links.eachAttr("abs:href");
        List<URL> urls = new ArrayList<>();
        for (String urlString : urlsStrings) {
            try {
                urls.add(new URL(urlString));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    public void store(Path path) {

    }
}
