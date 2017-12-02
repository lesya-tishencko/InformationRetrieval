package ru.spbau.ir.indexer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DocumentBlock implements Comparable<DocumentBlock>, Serializable {
    private final int id;
    private int frequency = 0;

    DocumentBlock(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    void increaseFrequency() {
        frequency++;
    }

    @Override
    public boolean equals(Object documentBlock) {
        return documentBlock instanceof DocumentBlock &&
                id == ((DocumentBlock) documentBlock).id;
    }

    @Override
    public int compareTo(DocumentBlock documentBlock) {
        return -Integer.compare(frequency, documentBlock.frequency);
    }
}
