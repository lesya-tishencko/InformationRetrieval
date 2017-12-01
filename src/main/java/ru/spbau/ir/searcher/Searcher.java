package ru.spbau.ir.searcher;

import ru.spbau.ir.database.DBHandler;
import ru.spbau.ir.indexer.DocumentBlock;
import ru.spbau.ir.indexer.Indexer;
import ru.spbau.ir.utils.Preprocessor;

import java.nio.file.Paths;
import java.util.*;

public class Searcher {
    private final Preprocessor preprocessor = new Preprocessor();
    private final Indexer indexer = new Indexer(Paths.get(System.getProperty("user.dir") +
            "/src/main/resources/Maps/indexMap"),
            Paths.get(System.getProperty("user.dir") + "/src/main/resources/Maps/indexOffsets"));
    private final int N;
    private Map<Integer, Integer> documentsLength;
    private final double averageLength;
    private final double k1 = 2.0;
    private final double b = 0.75;
    private final DBHandler dbHandler = new DBHandler();

    public Searcher() {
        documentsLength = dbHandler.getDocumentsLength();
        N = documentsLength.size();
        averageLength = documentsLength.values().stream().reduce(0, (acc, next) -> acc + next) / N;
    }

    public PriorityQueue<BM25Ranker> searchByPlot(String query) {
        List<String> tokens = preprocessor.handleText(query);
        List<PriorityQueue<DocumentBlock>> matrix = new ArrayList<>();
        Set<Integer> pull = new HashSet<>();
        List<Double> idf = new ArrayList<>();
        for (String word : tokens) {
            PriorityQueue<DocumentBlock> queue = indexer.getWordQueue(word);
            queue.stream().forEach(documentBlock -> pull.add(documentBlock.getId()));
            matrix.add(queue);
            idf.add(Math.log((double)N / queue.size()));
        }

        PriorityQueue<BM25Ranker> rankerPriorityQueue = new PriorityQueue<>();
        for (Integer idDocument : pull) {
            double sum = 0.0;
            for (int i = 0; i < tokens.size(); i++) {
                double frequency = (double)getFrequency(tokens.get(i), matrix.get(i), idDocument);
                sum += idf.get(i) * frequency * (k1 + 1) / (frequency + k1 * (1 - b + b * documentsLength.get(idDocument) / averageLength));
            }
            rankerPriorityQueue.add(new BM25Ranker(idDocument, sum));
        }

        PriorityQueue<BM25Ranker> result = new PriorityQueue<>();
        for (int i = 0; i < Math.min(100, rankerPriorityQueue.size()); i++) {
            result.add(rankerPriorityQueue.poll());
        }
        return result;
    }

    private Integer getFrequency(String token, PriorityQueue<DocumentBlock> queue, Integer id) {
        Map<Integer, Integer> frequency = new HashMap<>();
        queue.stream().forEach(documentBlock -> frequency.put(documentBlock.getId(), documentBlock.getFrequency()));
        if (!frequency.containsKey(id)) {
            return 0;
        }
        return frequency.get(id);
    }

    public PriorityQueue<BM25Ranker> getSimilar(int id) {
        String annotations = dbHandler.getDescription(id);
        return searchByPlot(annotations);
    }

    public class BM25Ranker implements Comparable<BM25Ranker> {
        int idDocument;
        double rank;

        BM25Ranker(int id, double rank) {
            idDocument = id;
            this.rank = rank;
        }

        public int getDocumentId() {
            return idDocument;
        }

        @Override
        public int compareTo(BM25Ranker o) {
            return -1 * Double.compare(rank, o.rank);
        }
    }
}
