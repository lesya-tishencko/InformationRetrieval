package ru.spbau.ir.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.spbau.ir.database.DBHandler;
import ru.spbau.ir.indexer.Indexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Parser {
    private final Map<String, Book> files = new HashMap<>();
    private final Queue<Path> pathQueue = new LinkedList<>();
    private int globalId = 1;
    private boolean isIdIncremented = false;

    private Parser(Path mainPath) {
        try {
            Path path = Paths.get(mainPath.toFile().getAbsolutePath());
            List<Path> paths = Files.walk(path).collect(Collectors.toList());
            paths.stream().filter(Files::isRegularFile).forEach(path_ -> pathQueue.add(path_));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String path = System.getProperty("user.dir");
        Path pageStoragePath = Paths.get(path + "/out/production/resources/pageStorage");
        Parser parser = new Parser(pageStoragePath);
        DBHandler dbHandler = new DBHandler();
        Indexer indexer = new Indexer();
        while (!parser.done()) {
            StructuredData nextData = parser.nextStructuredData();
            if (nextData == null)
                continue;
            int id = nextData.book.getId();
            if (parser.isIdIncremented) {
                dbHandler.addBook(nextData.book);
            } else {
                dbHandler.updateBook(nextData.book);
            }
            dbHandler.addReviews(id, nextData.reviews);
            String content = nextData.book.getLastDescription();
            content += nextData.reviews.stream().collect(Collectors.joining("\n"));
            indexer.addToIndex(content, id);
        }
        indexer.storeMapToFile(Paths.get(path + "/src/main/resources/Maps/indexMap"));
        indexer.storeWordsOffsetsToFile(Paths.get(path + "/src/main/resources/Maps/indexOffsets"));
    }

    private StructuredData nextStructuredData() {
        Path path = pathQueue.remove();
        try {
            return parse(Jsoup.parse(path.toFile(), "utf-16"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean done() {
        return pathQueue.isEmpty();
    }

    private StructuredData parse(Document html) {
        Elements links = html.select("p:contains(RoyalLib.Com)");
        if (links.size() > 0) {
            return extractFromRoyalLib(html);
        }
        links = html.select("meta[content=ЛитМир]");
        if (links.size() > 0) {
            return extractFromLitMir(html);
        }
        links = html.select("h1:contains(Сервис самопубликаций Самолит)");
        if (links.size() > 0) {
            return extractFromSamoLit(html);
        }
        links = html.select("div.global-footer-company-copyright:contains(MyBook)");
        if (links.size() > 0) {
            return extractFromMyBook(html);
        }
        links = html.select("strong.name:contains(ReadRate)");
        if (links.size() > 0) {
            return extractFromReadRate(html);
        }
        return null;
    }

    private StructuredData extractFromRoyalLib(Document html) {
        String author = "";
        String name = "";
        String description = "";
        String site = "royallib.com";
        Elements links = html.select("td > b");
        for (Element element : links) {
            if (element.text().startsWith("Автор:")) {
                author = element.nextElementSibling().text();
            } else if (element.text().startsWith("Название:")) {
                name = element.parent().ownText();
            } else if (element.text().startsWith("Аннотация:")) {
                description = element.parent().ownText();
            }
        }
        if (name.isEmpty() || author.isEmpty())
            return null;
        Book book = files.getOrDefault(name + author, new Book(globalId, name, author, description, site));
        if (book.getId() == globalId) {
            globalId++;
            isIdIncremented = true;
            files.put(name + author, book);
        } else if (!book.getSite().equals(site)){
            isIdIncremented = false;
            book.update(description, site);
        } else
            return null;
        List<String> reviews = new ArrayList<>();
        links = html.select("div[id^=comment]");
        for (Element element : links) {
            reviews.add(element.text());
        }
        return new StructuredData(book, reviews);
    }

    private StructuredData extractFromLitMir(Document html) {
        String site = "litmir.me";
        String name = "";
        String author = "";
        String description = "";
        Elements links = html.select("div[itemprop=name]");
        if (links.size() > 0) {
            name = links.first().ownText();
        }
        links = html.select("span[itemprop=author]");
        if (links.size() > 0) {
            author = links.first().nextElementSibling().text();
        }
        links = html.select("div[itemprop=description]");
        if (links.size() > 0) {
            description = links.first().text();
        }
        if (name.isEmpty() || author.isEmpty())
            return null;
        Book book = files.getOrDefault(name + author, new Book(globalId, name, author, description, site));
        if (book.getId() == globalId) {
            globalId++;
            isIdIncremented = true;
            files.put(name + author, book);
        } else if (!book.getSite().equals(site)){
            isIdIncremented = false;
            book.update(description, site);
        } else
            return null;
        List<String> reviews = new ArrayList<>();
        links = html.select("span[itemprop=reviewBody]");
        for (Element element : links) {
            reviews.add(element.text());
        }
        return new StructuredData(book, reviews);
    }

    private StructuredData extractFromSamoLit(Document html) {
        String site = "samolit.com";
        String name = "";
        String author = "";
        String description = "";
        Elements links = html.select("div.book-title > h1");
        if (links.size() > 0) {
            name = links.first().ownText();
        }
        links = html.select("a.author");
        if (links.size() > 0) {
            author = links.first().text();
        }
        links = html.select("div.book-description");
        if (links.size() > 0) {
            description = links.first().ownText();
        }
        if (name.isEmpty() || author.isEmpty())
            return null;
        Book book = files.getOrDefault(name + author, new Book(globalId, name, author, description, site));
        if (book.getId() == globalId) {
            globalId++;
            isIdIncremented = true;
            files.put(name + author, book);
        } else if (!book.getSite().equals(site)){
            isIdIncremented = false;
            book.update(description, site);
        } else
            return null;
        List<String> reviews = new ArrayList<>();
        links = html.select("div.comment-text");
        for (Element element : links) {
            reviews.add(element.ownText());
        }
        return new StructuredData(book, reviews);
    }

    private StructuredData extractFromMyBook(Document html) {
        String site = "mybook.ru";
        String name = "";
        String author = "";
        String description = "";
        Elements links = html.select("div.book-page-book-name");
        if (links.size() > 0) {
            name = links.first().ownText();
        }
        links = html.select("div.book-page-author > a");
        if (links.size() > 0) {
            author = links.first().text();
        }
        links = html.select("div.definition-section[itemprop=description]");
        if (links.size() > 0) {
            description = links.first().text();
        }
        if (name.isEmpty() || author.isEmpty())
            return null;
        Book book = files.getOrDefault(name + author, new Book(globalId, name, author, description, site));
        if (book.getId() == globalId) {
            globalId++;
            isIdIncremented = true;
            files.put(name + author, book);
        } else if (!book.getSite().equals(site)){
            isIdIncremented = false;
            book.update(description, site);
        } else
            return null;
        List<String> reviews = new ArrayList<>();
        links = html.select("div.comment-content-expander-inner");
        for (Element element : links) {
            reviews.add(element.text());
        }
        links = html.select("div.comment-content > span");
        if (links.size() > 0) {
            reviews.add(links.first().text());
        }
        return new StructuredData(book, reviews);
    }

    private StructuredData extractFromReadRate(Document html) {
        String site = "readrate.com";
        String name = "";
        String author = "";
        String description = "";
        Elements links = html.select("div.header > h1[itemprop=name]");
        if (links.size() > 0) {
            name = links.first().text();
        }
        links = html.select("a[itemprop=author]");
        if (links.size() > 0) {
            author = links.first().text();
        }
        links = html.select("div.description");
        if (links.size() > 0) {
            description = links.first().text();
        }
        if (name.isEmpty() || author.isEmpty())
            return null;
        Book book = files.getOrDefault(name + author, new Book(globalId, name, author, description, site));
        if (book.getId() == globalId) {
            globalId++;
            isIdIncremented = true;
            files.put(name + author, book);
        } else if (!book.getSite().equals(site)){
            isIdIncremented = false;
            book.update(description, site);
        } else
            return null;
        List<String> reviews = new ArrayList<>();
        links = html.select("p[itemprop=reviewBody]");
        for (Element element : links) {
            reviews.add(element.text());
        }
        links = html.select("cite.content");
        if (links.size() > 0) {
            reviews.add(links.first().text());
        }
        return new StructuredData(book, reviews);
    }

    static class StructuredData {
        final Book book;
        final List<String> reviews;

        StructuredData(Book book, List<String> reviews) {
            this.book = book;
            this.reviews = reviews;
        }
    }
}
