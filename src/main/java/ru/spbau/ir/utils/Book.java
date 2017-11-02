package ru.spbau.ir.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Book {
    private final int id;
    private final String name;
    private final String author;
    private final List<String> descriptions = new ArrayList<>();
    private final List<String> sites = new ArrayList<>();

    public Book(int id, String name, String author, String description, String site) {
        this.id = id;
        this.name = name;
        this.author = author;
        descriptions.add(description);
        sites.add(site);
    }

    public void update(String description, String site) {
        descriptions.add(description);
        sites.add(site);
    }

    public String getLastDescription() {
        return descriptions.get(descriptions.size() - 1);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return descriptions.stream().collect(Collectors.joining("\n\n"));
    }

    public String getSite() {
        return sites.stream().collect(Collectors.joining(", "));
    }
}
