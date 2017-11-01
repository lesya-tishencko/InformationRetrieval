package ru.spbau.ir.indexer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class DocumentBlock implements Comparable<DocumentBlock>, Serializable {
    private final int id;
    private int frequency = 0;
    private final List<Integer> positions = new ArrayList<>();

    DocumentBlock(int id) {
        this.id = id;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    int getId() {
        return id;
    }

    int getFrequency() {
        return frequency;
    }

    void increaseFrequency() {
        frequency++;
    }

    List<Integer> getPositions() {
        return positions;
    }

    boolean addPosition(int position) {
        return positions.add(position);
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
