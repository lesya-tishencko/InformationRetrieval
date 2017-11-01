package ru.spbau.ir.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Parser {
    private final int globalId = 1;
    private final Map<Integer, String> files = new HashMap<>();
    private final Queue<Document> fileQueue = new LinkedList<>();

    public Parser(Path mainPath) {
        try {
            Files.walk(Paths.get(mainPath.toFile().getAbsolutePath()))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .map(file -> {
                        try {
                            return Jsoup.parse(file, "UTF-8");
                        } catch (IOException exception) {
                            exception.printStackTrace();
                            return null;
                        }
                    })
                    .map(fileQueue::add);
            fileQueue.stream().filter(doc -> doc == null);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public Book nextStructuredData() {
        /* use jsoup selectors */
        return null;
    }

    public String nextIndexingData() {
        /* use jsoup selectors */
        return "";
    }

    public boolean done() {
        return fileQueue.isEmpty();
    }
}
