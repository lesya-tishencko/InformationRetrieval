package ru.spbau.ir.ui;

import ru.spbau.ir.database.DBHandler;
import ru.spbau.ir.searcher.RankerByScore;
import ru.spbau.ir.searcher.Searcher;
import ru.spbau.ir.utils.Book;
import ru.spbau.ir.utils.Preprocessor;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.OptionalDouble;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UI {
    private static int windowHeight;
    private static int windowWidth;
    private static int horMargin;
    private static int verMargin;
    private static int standardElementHeight;
    private static int standardElementWidth;
    private static boolean gotFirstInfo = false;

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
        JCheckBox checkBox = new JCheckBox();
        checkBox.setBounds(horMargin,
                verMargin + 2 * standardElementHeight + 2,
                standardElementHeight,
                standardElementHeight);
        JLabel checkBoxQuestion = new JLabel("Учитывать отзывы при ранжировании");
        checkBoxQuestion.setBounds(horMargin + standardElementHeight,
                verMargin + 2 * standardElementHeight - 4,
                windowWidth - 2 * horMargin - searchButtonWidth - 2,
                searchButtonHeight
        );
        jframe.add(checkBox);
        jframe.add(checkBoxQuestion);
        jbutton.addActionListener(actionEvent -> {
            jframe.getContentPane().removeAll();
            jframe.add(query);
            jframe.add(jbutton);
            jframe.add(checkBox);
            jframe.add(checkBoxQuestion);
            showRankedBooks(dbHandler, screen, jframe, checkBox, searchButtonHeight, searchButtonWidth, query);
        });
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
                                        JCheckBox checkBox,
                                        int searchButtonHeight,
                                        int searchButtonWidth,
                                        JTextArea query) {
        String queryText = query.getText();
        Searcher searcher = new Searcher();
        PriorityQueue<Searcher.BM25Ranker> list = searcher.searchByPlot(queryText);

        if (checkBox.isSelected()) {
            RankerByScore ranker = new RankerByScore(dbHandler);
            list = ranker.rankByScore(list);
            JLabel scoreLabel = new JLabel("Оценка");
            scoreLabel.setBounds(windowWidth - searchButtonWidth - horMargin,
                    verMargin + 2 * standardElementHeight - 4,
                    searchButtonWidth,
                    searchButtonHeight);
            jframe.add(scoreLabel);
        }
        JPanel rankedBooksPanel = new JPanel();
        int bookNo = 0;
        while (!list.isEmpty()) {
            Searcher.BM25Ranker bookId = list.poll();
            Book book = dbHandler.getBook(bookId.getDocumentId());
            if (book == null) {
                continue;
            }
            JButton bookButton = new JButton(book.getAuthor() + " " + " \"" +
                    book.getName() + "\"");
            bookButton.addActionListener(actionEvent1 -> {
                showBookInfo(dbHandler, screen, searcher, book, queryText);
            });
            rankedBooksPanel.add(bookButton);
            bookButton.setBounds(
                    0,
                    standardElementHeight * bookNo + bookNo,
                    windowWidth - 2 * horMargin - searchButtonWidth - 2,
                    standardElementHeight
            );
            if (checkBox.isSelected()) {
                JProgressBar pbar = new JProgressBar();
                pbar.setMinimum(0);
                pbar.setMaximum(100);
                pbar.setBounds(windowWidth - 2 * horMargin - searchButtonWidth,
                        standardElementHeight * bookNo + bookNo,
                        searchButtonWidth,
                        standardElementHeight);
                OptionalDouble averageScoreOpt = dbHandler
                        .getBooksReviewsSentimScores(bookId
                                .getDocumentId())
                        .stream()
                        .mapToDouble(a -> a)
                        .average();
                if (averageScoreOpt.isPresent()) {
                    pbar.setValue((int) (averageScoreOpt.getAsDouble() * 100));
                }

                rankedBooksPanel.add(pbar);
            }
            bookNo++;
        }
        rankedBooksPanel.setBounds(
                horMargin,
                verMargin + 3 * standardElementHeight + 2,
                windowWidth - 2 * horMargin,
                bookNo * searchButtonHeight + bookNo
        );
        rankedBooksPanel.setLayout(new BoxLayout(rankedBooksPanel, BoxLayout.Y_AXIS));
        jframe.getContentPane().add(rankedBooksPanel);
        jframe.invalidate();
        jframe.repaint();
        gotFirstInfo = false;
    }

    private static void showBookInfo(DBHandler dbHandler,
                                     Dimension screen,
                                     Searcher searcher,
                                     Book book,
                                     String queryText) {
        JFrame bookFrame = new JFrame("Информация о книге");
        bookFrame.setLayout(null);
        JLabel author = new JLabel();
        author.setText(book.getAuthor());
        author.setBounds(horMargin, verMargin, windowWidth - 2 * horMargin, standardElementHeight);
        JLabel name = new JLabel();
        name.setText("\"" + book.getName() + "\"");
        name.setBounds(horMargin,
                verMargin + standardElementHeight + 1,
                windowWidth - 2 * horMargin,
                standardElementHeight);
        JLabel site = new JLabel("Описание согласно сайту " + book.getSite() + ":");
        site.setBounds(horMargin,
                verMargin + 2 * standardElementHeight + 2,
                windowWidth - 2 * horMargin,
                standardElementHeight);
        bookFrame.add(site);
        Preprocessor prepr = new Preprocessor();
        List<String> queryTokens = prepr.handleText(queryText);
        String descriptionText = book.getDescription();
        if (!gotFirstInfo) {
            for (String word : queryTokens) {
                if (word.length() > 3) {
                    Pattern p = Pattern.compile(word, Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(descriptionText);
                    descriptionText = m.replaceAll("<font color='red'>" + word + "</font>");
                }
            }
        }
        JLabel description = new JLabel("<html>" + descriptionText + "</html>");
        int descHeight = (description.getPreferredSize().height * description.getPreferredSize().width)
                / (windowWidth - 2 * horMargin) + 10;
        description.setBounds(horMargin,
                verMargin + standardElementHeight * 3 + 3,
                windowWidth - 2 * horMargin,
                descHeight);
        bookFrame.add(author);
        bookFrame.add(name);
        bookFrame.add(description);
        PriorityQueue<Searcher.BM25Ranker> docsList
                = searcher.getSimilar(book.getId());
        RankerByScore ranker = new RankerByScore(dbHandler);
        PriorityQueue<Searcher.BM25Ranker> docsIds = ranker.rankByScore(docsList);
        JLabel infoLabel = new JLabel("Похожие книги, получившие лучшие оценки читателей:");

        int similarYPos = description.getBounds().y + description.getBounds().height + 10;
        infoLabel.setBounds(horMargin,
                similarYPos,
                windowWidth - 2 * horMargin,
                standardElementHeight);
        bookFrame.add(infoLabel);
        JPanel similarPanel = new JPanel();
        int docNo = 0;
        while (!docsIds.isEmpty()){
            Book currBook = dbHandler.getBook(docsIds.poll().getDocumentId());
            if (currBook == null) {
                continue;
            }
            JButton bookLabel = new JButton(currBook.getAuthor() + " " + "\"" + currBook.getName() + "\"");
            bookLabel.setBounds(0,
                    standardElementHeight * (docNo) + docNo,
                    windowWidth - 2 * horMargin,
                    standardElementHeight);
            bookLabel.addActionListener(actionEvent1 -> {
                showBookInfo(dbHandler, screen, searcher, currBook, description.getText());
            });
            similarPanel.add(bookLabel);
            docNo++;
        }
        similarPanel.setLayout(new BoxLayout(similarPanel, BoxLayout.Y_AXIS));
        similarPanel.setBounds(horMargin,
                similarYPos + standardElementHeight + 5,
                windowWidth - 2 * horMargin,
                standardElementHeight * docNo + docNo);
        JScrollPane scrollPane = new JScrollPane(similarPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(horMargin,
                similarYPos + standardElementHeight + 5,
                windowWidth - 2 * horMargin,
                windowHeight - similarYPos - standardElementHeight - verMargin);
        bookFrame.add(scrollPane);
        bookFrame.setSize(windowWidth, windowHeight);
        bookFrame.setBounds((screen.width - windowWidth) / 2,
                (screen.height - windowHeight) / 2,
                windowWidth,
                windowHeight);
        bookFrame.setResizable(false);
        bookFrame.setVisible(true);
        gotFirstInfo = true;
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(UI::createAndShowUI);
    }
}
