package ru.spbau.ir.books;

import java.net.URL;

public class Website {
    public URL nextURL() {
        return null;
    }

    /* politness */
    public boolean permitsCrawl(URL url) {
        return false;
    }
}
