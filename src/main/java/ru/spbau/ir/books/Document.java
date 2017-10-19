package ru.spbau.ir.books;


import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public class Document {
    private File htmlPage;

    Document(File htmlPage) {
        this.htmlPage = htmlPage;
    }
    
    public List<URL> parse() {
        return null;
    }

    public void store(Path path) {

    }
}
