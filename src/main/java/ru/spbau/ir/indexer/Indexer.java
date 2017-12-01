package ru.spbau.ir.indexer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ru.spbau.ir.utils.Preprocessor;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class Indexer {

    private final HashMap<String, PriorityQueue<DocumentBlock>> map = new HashMap<>();
    private HashMap<String, FileOffsets> wordsOffsets;
    private File mapFile;
    private File offsetsFile;
    private Preprocessor preprocessor = new Preprocessor();

    public Indexer(Path mapFilePath, Path offsetsFilePath) {
        mapFile = mapFilePath.toFile();
        offsetsFile = offsetsFilePath.toFile();
    }

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
        text = preprocessor.removePunctuation(text);
        text = preprocessor.removeStopWords(text);

        List<String> words = Arrays.asList(text.split(" "));
        for (int i = 0; i < words.size(); ++i) {
            String word = words.get(i);
            words.set(i, preprocessor.stammer(word));
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

    public void storeMapToFile() {
        Gson gson = new Gson();
        if (!mapFile.exists()) {
            try {
                mapFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HashMap<String, FileOffsets> offsets = new HashMap<>();

        try (FileOutputStream fileOutputStream = new FileOutputStream(mapFile, true);
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

    public void storeWordsOffsetsToFile() {
        if (!offsetsFile.exists()) {
            try {
                offsetsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(offsetsFile);
             ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream)) {
            outputStream.writeObject(wordsOffsets);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, FileOffsets> getWordsOffsets() {
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
