package ru.spbau.ir.indexer;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Indexer {
    private HashMap<String, PriorityQueue<DocumentBlock>> map = new HashMap<>();
    private  HashMap<String, FileOffsets> wordsOffsets;
    private File mapFile;
    private File offsetsFile;
    private SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.RUSSIAN);

    public void addToIndex(String text, int id) {
        HashMap<String, DocumentBlock> textHashMap = getMapFromText(text, id);
        textHashMap.forEach((word, documentBlock) -> {
            if (map.containsKey(word)) {
                map.get(word).add(documentBlock);
            } else {
                PriorityQueue<DocumentBlock> priorQueue = new PriorityQueue<>();
                priorQueue.add(documentBlock);
                map.put(word, priorQueue);
            }
        });
    }

    private HashMap<String, DocumentBlock> getMapFromText(String text, int id) {
        text = text.toLowerCase();
        text = removePunctuation(text);
        List<String> words = Arrays.asList(text.split(" "));
        for (int i = 0; i < words.size(); ++i) {
            String word = words.get(i);
            words.set(i, stemmer.stem(word).toString());
        }
        HashMap<String, DocumentBlock> textHashMap = new HashMap<>();

        for (int i = 0; i < words.size(); ++i) {
            String currentWord = words.get(i);
            DocumentBlock documentBlock;
            if (textHashMap.containsKey(currentWord)) {
                documentBlock = textHashMap.get(currentWord);
            } else {
                documentBlock = new DocumentBlock(id);
            }
            documentBlock.increaseFrequency();
            documentBlock.addPosition(i);
            textHashMap.put(currentWord, documentBlock);
        }
        return textHashMap;
    }

    private String removePunctuation(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isAlphabetic(c) || Character.isDigit(c) || Character.isSpaceChar(c)) {
                result.append(c);
            }
        }
        return result.toString();
    }

    private static class FileOffsets implements Serializable {
        final long start;
        final long end;

        FileOffsets(long start, long end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "start = " + start + ", end = " + end;
        }
    }

    public void storeMapToFileAndGetDictionary(Path pathToFile) {
        File file = new File(pathToFile.toFile().getAbsolutePath());
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mapFile = file;

        HashMap<String, FileOffsets> offsets = new HashMap<>();

        try (FileOutputStream fileOutputStream = new FileOutputStream(file, true);
             DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
             ObjectOutputStream outputStream = new ObjectOutputStream(dataOutputStream)) {
            map.forEach((word, documentBlocks) -> {
                try {
                    long start = dataOutputStream.size();
                    outputStream.writeChars(word);
                    outputStream.writeObject(documentBlocks);
                    outputStream.flush();
                    long end = dataOutputStream.size();
                    offsets.put(word, new FileOffsets(start, end));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fileOutputStream.close();
            dataOutputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.wordsOffsets = offsets;
    }

    public void storeWordOffsetsToFile(Path pathToFile) {
        File file = new File(pathToFile.toFile().getAbsolutePath());
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        offsetsFile = file;

        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream)) {
            outputStream.writeObject(wordsOffsets);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, FileOffsets> getWordsOffsets() {
        if (wordsOffsets == null) {
            try(FileInputStream fileInputStream = new FileInputStream(offsetsFile);
                ObjectInputStream inputStream = new ObjectInputStream(fileInputStream)) {
                wordsOffsets = (HashMap<String, FileOffsets>) inputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return wordsOffsets;
    }
}
