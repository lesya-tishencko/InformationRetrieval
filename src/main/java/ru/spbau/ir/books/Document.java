package ru.spbau.ir.books;


import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.*;
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
    String pattern = "page";
    int counter = 1;

    public void store(Path path) {
        File file = new File(path.toFile().getAbsolutePath() + "/" + pattern + counter + ".html");
        boolean result = false;
        if (!file.exists()) {
            try {
                result = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!result) return;
        }
        DataOutputStream fout = null;
        try {
            fout = new DataOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fout.writeChars(htmlPage.outerHtml());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
