package ru.spbau.ir.ui;

import ru.spbau.ir.database.DBHandler;
import ru.spbau.ir.searcher.RankerByScore;
import ru.spbau.ir.searcher.Searcher;
import ru.spbau.ir.utils.Book;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.OptionalDouble;
import java.util.PriorityQueue;

public class UI {
    private static int windowHeight;
    private static int windowWidth;
    private static int horMargin;
    private static int verMargin;
    private static int standardElementHeight;
    private static int standardElementWidth;

    public static void createAndShowUI() {
        DBHandler dbHandler = new DBHandler();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        windowWidth = screen.width * 2 / 3;
        windowHeight = (int)(screen.height * 0.75);
        horMargin = windowWidth / 10;
        verMargin = windowHeight / 20;
        standardElementHeight = windowHeight / 25;
        standardElementWidth = windowWidth - 2 * horMargin;

        JFrame jframe = new JFrame("Поиск книг");
        jframe.setLayout(null);
        int searchButtonHeight = windowWidth / 25;
        int searchButtonWidth = windowWidth / 10;

        JTextArea query = new JTextArea();
        query.setBounds(horMargin,
                verMargin,
                windowWidth - 2 * horMargin - searchButtonWidth - 2,
                searchButtonHeight);
        jframe.add(query);

        JButton jbutton = new JButton("Поиск");
        int searchButtonX = windowWidth - searchButtonWidth - horMargin;
        int searchButtonY = verMargin;
        jbutton.setBounds(searchButtonX,
                searchButtonY,
                searchButtonWidth,
                searchButtonHeight);
        jbutton.addActionListener(actionEvent ->
                showRankedBooks(dbHandler, screen, jframe, searchButtonHeight, searchButtonWidth, query));
        jframe.add(jbutton);

        jframe.setSize(windowWidth,windowHeight);
        jframe.setBounds((screen.width - windowWidth) / 2,
                (screen.height - windowHeight) / 2,
                windowWidth,
                windowHeight);
        jframe.setResizable(false);
        jframe.setVisible(true);
    }

    private static void showRankedBooks(DBHandler dbHandler,
                                        Dimension screen,
                                        JFrame jframe,
                                        int searchButtonHeight,
                                        int searchButtonWidth,
                                        JTextArea query) {
        String queryText = query.getText();
        Searcher searcher = new Searcher();
        PriorityQueue<Searcher.BM25Ranker> list = searcher.searchByPlot(queryText);
        int bookNo = 0;
        while (!list.isEmpty()) {
            Searcher.BM25Ranker bookId = list.poll();
            Book book = dbHandler.getBook(bookId.getDocumentId());
            JButton bookButton = new JButton(book.getAuthor() + " " + " \"" +
                    book.getName() + "\" (" + book.getSite() + ")");
            bookButton.addActionListener(actionEvent1 -> {
                showBookInfo(dbHandler, screen, searcher, bookId, book);
            });
            jframe.setLayout(null);
            jframe.add(bookButton);
            bookButton.setBounds(
                    horMargin,
                    verMargin + searchButtonHeight * (2 + bookNo),
                    windowWidth - 2 * horMargin - searchButtonWidth - 2,
                    searchButtonHeight
            );
            bookButton.setSize(windowWidth - 2 * horMargin - searchButtonWidth - 2,
                    searchButtonHeight);
            JProgressBar pbar = new JProgressBar();
            pbar.setMinimum(0);
            pbar.setMaximum(100);
            pbar.setBounds(windowWidth - searchButtonWidth - horMargin,
                    verMargin + searchButtonHeight * (2 + bookNo),
                    searchButtonWidth,
                    searchButtonHeight);
            OptionalDouble averageScoreOpt = dbHandler
                    .getBooksReviewsSentimScores(bookId
                            .getDocumentId())
                    .stream()
                    .mapToDouble(a -> a)
                    .average();
            if (averageScoreOpt.isPresent()) {
                pbar.setValue((int)(averageScoreOpt.getAsDouble() * 100));
            }

            jframe.add(pbar);
            jframe.invalidate();
            jframe.repaint();
            bookNo++;
        }
    }

    private static void showBookInfo(DBHandler dbHandler,
                                     Dimension screen,
                                     Searcher searcher,
                                     Searcher.BM25Ranker bookId,
                                     Book book) {
        JFrame bookFrame = new JFrame("Информация о книге");
        JLabel author = new JLabel();
        author.setText(book.getAuthor());
        author.setBounds(horMargin, verMargin, windowWidth - 2 * horMargin, standardElementHeight);
        JLabel name = new JLabel();
        name.setText("\"" + book.getName() + "\"");
        name.setBounds(horMargin, verMargin + standardElementHeight + 1, windowWidth - 2 * horMargin, standardElementHeight);
        JLabel description = new JLabel("<html>" + book.getDescription() + "</html>");
        description.setBounds(horMargin,
                verMargin + standardElementHeight * 2 + 1,
                windowWidth - 2 * horMargin,
                standardElementHeight * 5);
        bookFrame.add(author);
        bookFrame.add(name);
        bookFrame.add(description);
        PriorityQueue<Searcher.BM25Ranker> docsList
                = searcher.getSimilar(bookId.getDocumentId());
        RankerByScore ranker = new RankerByScore(dbHandler);
        List<Integer> docsIds = ranker.rankByScore(docsList);
        int docNo = 0;
        for (Integer docId : docsIds) {
            Book currBook = dbHandler.getBook(docId);
            JLabel bookLabel = new JLabel(
                    currBook.getAuthor() + " " + "\"" + currBook.getName() + "\"");
            bookLabel.setBounds(horMargin,
                    verMargin + standardElementHeight * (8 + docNo) + docNo + 1,
                    windowWidth - 2 * horMargin,
                    standardElementHeight);
            bookFrame.add(bookLabel);
            docNo++;
        }
        bookFrame.setSize(windowWidth, windowHeight);
        bookFrame.setBounds((screen.width - windowWidth) / 2,
                (screen.height - windowHeight) / 2,
                windowWidth,
                windowHeight);
        bookFrame.setResizable(false);
        bookFrame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(UI::createAndShowUI);
    }
}
