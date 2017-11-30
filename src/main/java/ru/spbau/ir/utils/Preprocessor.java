package ru.spbau.ir.utils;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

public class Preprocessor {
    private final SnowballStemmer stemmer;

    public Preprocessor() {
        stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.RUSSIAN);
    }

    public List<String> handleText(String text) {
        String handledText = removePunctuation(text);
        handledText = removeStopWords(text);
        List<String> words = Arrays.asList(text.split(" "));
        for (int i = 0; i < words.size(); ++i) {
            String word = words.get(i);
            words.set(i, stemmer.stem(word).toString());
        }
        return words;
    }

    public String removePunctuation(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isAlphabetic(c) || Character.isDigit(c) || Character.isSpaceChar(c)) {
                result.append(c);
            }
        }
        return result.toString();
    }

    public String removeStopWords(String text) {
        /*CharArraySet stopWords = RussianAnalyzer.getDefaultStopSet();
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
        return stringBuilder.toString();*/
        return null;
    }

    public String stammer(String word) {
        return stemmer.stem(word).toString();
    }
}
