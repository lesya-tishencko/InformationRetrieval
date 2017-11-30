package ru.spbau.ir.ui;

import ru.spbau.ir.database.DBHandler;
import ru.spbau.ir.indexer.Indexer;
import ru.spbau.ir.searcher.Searcher;
import ru.spbau.ir.utils.Book;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.PriorityQueue;

public class UI {
    private int windowHeight;
    private int windowWidth;
    private int horMargin;
    private int verMargin;
    public UI(DBHandler dbHandler) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        windowWidth = screen.width * 2 / 3;
        windowHeight = (int)(screen.height * 0.75);
        horMargin = windowWidth / 10;
        verMargin = windowHeight / 20;

        JFrame jframe = new JFrame("Book search");
        jframe.setLayout(null);
        int searchButtonHeight = windowWidth / 25;
        int searchButtonWidth = windowWidth / 10;

        JTextArea query = new JTextArea("Введите текст сюжета");
        query.setBounds(horMargin,
                verMargin,
                windowWidth - 2 * horMargin - searchButtonWidth - 2,
                searchButtonHeight);
        jframe.add(query);


        JButton jbutton = new JButton("Search");
        int searchButtonX = windowWidth - searchButtonWidth - horMargin;
        int searchButtonY = verMargin;
        jbutton.setBounds(searchButtonX,
                searchButtonY,
                searchButtonWidth,
                searchButtonHeight);
        jbutton.addActionListener(actionEvent -> {
            String queryText = query.getText();
            Searcher searcher = new Searcher(dbHandler);
            PriorityQueue<Searcher.BM25Ranker> list = searcher.searchByPlot(queryText);
            int bookNo = 0;
            while (!list.isEmpty()) {
                Searcher.BM25Ranker bookId = list.poll();
                Book book = dbHandler.getBook(bookId);
                JButton bookButton = new JButton(book.getAuthor() + " " + "\"" + book.getName() + "\"");
                bookButton.setBounds(
                        horMargin,
                        verMargin + searchButtonHeight * bookNo,
                        windowWidth - 2 * horMargin - searchButtonWidth - 2,
                        searchButtonHeight
                        );
                bookButton.addActionListener(actionEvent1 -> {
                    JFrame bookFrame = new JFrame("Book information");
                    jframe.setLayout(null);

                });
                bookNo++;
            }
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

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            UI ui = new UI();
        });
    }
}
