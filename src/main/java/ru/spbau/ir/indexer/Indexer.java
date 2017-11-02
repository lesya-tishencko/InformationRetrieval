package ru.spbau.ir.indexer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class Indexer {

    private HashMap<String, PriorityQueue<DocumentBlock>> map = new HashMap<>();
    private HashMap<String, FileOffsets> wordsOffsets;
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
        text = removeStopWords(text);

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

    private String removeStopWords(String text) {
        CharArraySet stopWords = RussianAnalyzer.getDefaultStopSet();
        TokenStream tokenStream = new StandardAnalyzer().tokenStream("", new StringReader(text));
        tokenStream = new StopFilter(tokenStream, stopWords);
        StringBuilder stringBuilder = new StringBuilder();
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        try {
            tokenStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while (tokenStream.incrementToken()) {
                String term = charTermAttribute.toString();
                stringBuilder.append(term + " ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public void storeMapToFile(Path pathToFile) {
        Gson gson = new Gson();
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
                    int start = dataOutputStream.size();
                    dataOutputStream.writeChars(gson.toJson(documentBlocks));
                    dataOutputStream.flush();
                    int end = dataOutputStream.size();
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

    public void storeWordsOffsetsToFile(Path pathToFile) {
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
            try (FileInputStream fileInputStream = new FileInputStream(offsetsFile);
                 ObjectInputStream inputStream = new ObjectInputStream(fileInputStream)) {
                wordsOffsets = (HashMap<String, FileOffsets>) inputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return wordsOffsets;
    }

    public PriorityQueue<DocumentBlock> getWordQueue(String word) {
        PriorityQueue<DocumentBlock> priorQueue = null;
        getWordsOffsets();
        FileOffsets offsets;
        if (wordsOffsets.containsKey(word)) {
            offsets = wordsOffsets.get(word);
        } else {
            return null;
        }
        byte[] objectBuffer = new byte[offsets.end - offsets.start];
        try (FileInputStream fileInputStream = new FileInputStream(mapFile)) {
            long actualStart = fileInputStream.skip(offsets.start);
            if (actualStart != offsets.start) {
                throw new IOException("Incorrect number of bytes was skipped");
            }
            int read = fileInputStream.read(objectBuffer);
            if (read != objectBuffer.length) {
                throw new IOException("Number of read bytes is incorrect");
            }
            Gson gson = new Gson();
            Type type = new TypeToken<PriorityQueue<DocumentBlock>>() {
            }.getType();
            priorQueue = gson.fromJson(new String(objectBuffer, StandardCharsets.UTF_16), type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return priorQueue;
    }

    private static class FileOffsets implements Serializable {
        final int start;
        final int end;

        FileOffsets(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "start = " + start + ", end = " + end;
        }
    }
}
