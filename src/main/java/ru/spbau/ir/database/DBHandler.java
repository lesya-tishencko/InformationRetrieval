package ru.spbau.ir.database;

import ru.spbau.ir.utils.Book;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

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
                        "getName text NOT NULL, " +
                        "getAuthor text NOT NULL, " +
                        "getDescription text, " +
                        "getSite text NOT NULL)";
            statement.executeUpdate(sql);
            sql = "CREATE TABLE reviews (" +
                    "id SERIAL PRIMARY KEY, " +
                    "book_id int REFERENCES books, " +
                    "review text)";
            statement.executeUpdate(sql);
            statement.close();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        System.out.println("Tables created successfully");
    }

    public void addBook(Book book) {
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
                    "E'" + site + "');";

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

    public void updateBook(Book book) {
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
            String sql = "UPDATE books SET " +
                    "getDescription = " + "E'" + description + "', " +
                    "getSite = " + "E'" + site + "' where " +
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
}
