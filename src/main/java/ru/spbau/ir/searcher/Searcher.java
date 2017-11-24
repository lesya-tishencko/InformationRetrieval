package ru.spbau.ir.searcher;

import ru.spbau.ir.database.DBHandler;
import ru.spbau.ir.indexer.DocumentBlock;
import ru.spbau.ir.indexer.Indexer;
import ru.spbau.ir.utils.Preprocessor;

import java.util.*;

public class Searcher {
    private final Preprocessor preprocessor = new Preprocessor();
    private final Indexer indexer;
    private final int N;
    private final Map<Integer, Double> documentsLength;
    private final double averageLength;
    private final double k1 = 2.0;
    private final double b = 0.75;

    public Searcher(Indexer indexer, DBHandler dbHandler) {
        this.indexer = indexer;
        this.N = N;
    }

    public PriorityQueue<BM25Ranker> searchByPlot(String query) {
        List<String> tokens = preprocessor.handleText(query);
        Map<String, PriorityQueue<DocumentBlock>> matrix = new HashMap<>();
        Set<DocumentBlock> pull = new HashSet<>();
        List<Double> idf = new ArrayList<>();
        for (String word : tokens) {
            PriorityQueue<DocumentBlock> queue = indexer.getWordQueue(word);
            pull.addAll(queue);
            matrix.put(word, queue);
            idf.add(Math.log((double)N / queue.size()));
        }


    }

    private double getFrequency(String token, PriorityQueue<DocumentBlock> queue, DocumentBlock documentBlock) {

    }

    public PriorityQueue<DocumentBlock> getSimilar(int id) {

    }

    public class BM25Ranker {
        int idDocument;
        double rank;

        BM25Ranker(int id, double rank) {
            idDocument = id;
            this.rank = rank;
        }
    }
}
