package ru.spbau.ir.database;

import ru.spbau.ir.utils.Book;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHandler {
    private final String connectionString = "jdbc:postgresql://localhost:5432/postgres";
    private final String user = "postgres";
    private final String password = "RedDress2";

    public DBHandler() {
        Connection connection;
        Statement statement;

        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(connectionString, user, password);
            statement = connection.createStatement();
            String sql = "CREATE TABLE books (" +
                        "id int PRIMARY KEY, " +
                        "name text NOT NULL, " +
                        "author text NOT NULL, " +
                        "description text, " +
                        "site text NOT NULL, " +
                        "length int)";
            statement.executeUpdate(sql);
            sql = "CREATE TABLE reviews (" +
                    "id SERIAL PRIMARY KEY, " +
                    "book_id int REFERENCES books, " +
                    "review text, " +
                    "score real)";
            statement.executeUpdate(sql);
            statement.close();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        System.out.println("Tables created successfully");
    }

    public void addBook(Book book, int length) {
        Connection connection;
        Statement statement;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(connectionString, user, password);
            connection.setAutoCommit(false);
            statement = connection.createStatement();

            String id = Integer.toString(book.getId());
            String name = book.getName();
            String author = book.getAuthor();
            String description = book.getDescription();
            String site = book.getSite();
            String sql = "INSERT INTO books VALUES (" +
                    id + ", " +
                    "'" + name + "', " +
                    "E'" + author + "', " +
                    "E'" + description + "', " +
                    "E'" + site + "', " +
                    description.length() + length + "');";

            statement.executeUpdate(sql);
            statement.close();
            connection.commit();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        }
    }

    public void addReviews(int bookId, List<String> reviews) {
        if (reviews.size() == 0)
            return;
        Connection connection;
        Statement statement;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(connectionString, user, password);
            connection.setAutoCommit(false);
            statement = connection.createStatement();

            for (String review : reviews) {
                String sql = "INSERT INTO reviews (book_id, review) VALUES (" +
                        Integer.toString(bookId) + ", " +
                        "E'" + review + "');";

                statement.executeUpdate(sql);
            }

            statement.close();
            connection.commit();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        }
    }

    public void updateBook(Book book, int length) {
        Connection connection;
        Statement statement;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(connectionString, user, password);
            connection.setAutoCommit(false);
            statement = connection.createStatement();

            String id = Integer.toString(book.getId());
            String description = book.getDescription();
            String site = book.getSite();
            String sql = "SELECT length FROM books where id = " + id + ";";
            ResultSet resultSet = statement.executeQuery(sql);
            int prevLength = 0;
            while (resultSet.next()) {
                prevLength = resultSet.getInt(0);
            }

            sql = "UPDATE books SET " +
                    "description = " + "E'" + description + "', " +
                    "site = " + "E'" + site + "', " + "length = " + prevLength + length + "' where " +
                    "id = " + id + ";";

            statement.executeUpdate(sql);
            statement.close();
            connection.commit();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        }
    }

    public Map<Integer, Integer> getDocumentsLength() {
        Connection connection;
        Statement statement;
        Map<Integer, Integer> result = new HashMap<>();
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(connectionString, user, password);
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            String sql = "SELECT id, length FROM books;";
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                result.put(resultSet.getInt(0), resultSet.getInt(1));
            }
            statement.close();
            connection.commit();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }

    public List<Double> getBooksReviewsSentimScores(int bookId) {
        Connection connection = null;
        Statement statement = null;
        List<Double> scoresNumbers = new ArrayList<>();
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(connectionString, user, password);
            connection.setAutoCommit(false);
            statement = connection.createStatement();

            String sql = "SELECT score FROM reviews WHERE book_id = " + bookId + ";";
            ResultSet scores = statement.executeQuery(sql);
            while (scores.next()) {
                scoresNumbers.add(scores.getDouble("score"));
            }
            scores.close();
            statement.close();
            connection.commit();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
        return scoresNumbers;
    }
}
