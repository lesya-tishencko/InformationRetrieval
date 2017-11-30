package ru.spbau.ir.searcher;

import ru.spbau.ir.database.DBHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.PriorityQueue;

public class RankerByScore {
    DBHandler dbHandler;
    public RankerByScore(DBHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    List<Integer> rankByScore(PriorityQueue<Searcher.BM25Ranker> docs) {
        class IdScore implements Comparable {
            int id;
            double score;

            IdScore(int id, double score) {
                this.id = id;
                this.score = score;
            }

            @Override
            public int compareTo(Object o) {
                if (o instanceof IdScore) {
                    return -Double.compare(((IdScore) o).score, score);
                }
                return 0;
            }
        }
        PriorityQueue<IdScore> scoreQueue = new PriorityQueue<>();
        for (Searcher.BM25Ranker doc : docs) {
            List<Double> scores = dbHandler.getBooksReviewsSentimScores(doc.idDocument);
            double averageScore = 0;
            if (scores != null && !scores.isEmpty()) {
                OptionalDouble averageScoreOpt = scores.stream().mapToDouble(a -> a).average();
                if (averageScoreOpt.isPresent()) {
                    averageScore = averageScoreOpt.getAsDouble();
                }
            }
            scoreQueue.add(new IdScore(doc.idDocument, averageScore));
        }
        List<Integer> resultIds = new ArrayList<>();
        while (!scoreQueue.isEmpty()) {
            resultIds.add(scoreQueue.poll().id);
        }
        return resultIds;
   }
}
