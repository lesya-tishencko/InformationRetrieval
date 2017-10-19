package ru.spbau.ir.books;

import java.net.URL;

public class Website {

    /* politness */
    public boolean permitsCrawl(URL url) {
        return false;
    }

    public Document getDocument() {
        return null;
    }
}
