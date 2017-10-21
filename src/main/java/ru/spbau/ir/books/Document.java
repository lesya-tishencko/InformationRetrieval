package ru.spbau.ir.books;

import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Document {
    private final org.jsoup.nodes.Document htmlPage;
    private final String patternInFileName = "page";

    Document(org.jsoup.nodes.Document htmlPage) {
        this.htmlPage = htmlPage;
    }

    public List<URL> parse() {
        Elements links = htmlPage.select("a[href]");
        List<String> urlsStrings = links.eachAttr("abs:href");
        List<URL> urls = new ArrayList<>();
        for (String urlString : urlsStrings) {
            try {
                urls.add(new URL(urlString));
            } catch (MalformedURLException ignored) {
            }
        }
        return urls;
    }

    public void store(Path path, int suffixOfFileName) {
        File file = new File(path.toFile().getAbsolutePath() + "/" + patternInFileName + suffixOfFileName + ".html");
        boolean result = false;
        if (!file.exists()) {
            try {
                result = file.createNewFile();
            } catch (IOException ignored) {
            }
            if (!result) return;
        }

        try (DataOutputStream fout = new DataOutputStream(new FileOutputStream(file))){
            fout.writeChars(htmlPage.outerHtml());
            fout.flush();
        } catch (Exception ignored) {
        }
    }
}
