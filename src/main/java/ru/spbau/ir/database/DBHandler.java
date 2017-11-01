package ru.spbau.ir.database;

import ru.spbau.ir.utils.Book;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

public class DBHandler {
    private final String connectionString = "";
    private final String user = "";
    private final String password = "";

    public DBHandler() {
        Connection connection = null;
        Statement statement = null;

        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(connectionString, user, password);
            statement = connection.createStatement();
            String sql = "CREATE TABLE books (" +
                        "id int PRIMARY KEY, " +
                        "getName char(30) NOT NULL, " +
                        "getAuthor char(30) NOT NULL, " +
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

    public boolean addBook(Book book) {
        Connection connection = null;
        Statement statement = null;
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
                    name + ", " +
                    author + ", " +
                    description + ", " +
                    site + ");";

            statement.executeUpdate(sql);
            statement.close();
            connection.commit();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        System.out.println("Book's record added successfully");
        return true;
    }

    public boolean addReviews(int bookId, List<String> reviews) {
        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(connectionString, user, password);
            connection.setAutoCommit(false);
            statement = connection.createStatement();

            for (String review : reviews) {
                String sql = "INSERT INTO books (book_id, review) VALUES (" +
                        Integer.toString(bookId) + ", " +
                        review + ");";

                statement.executeUpdate(sql);
            }

            statement.close();
            connection.commit();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        System.out.println("Review's records added successfully");
        return true;
    }

    public boolean updateBook(Book book) {
        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(connectionString, user, password);
            connection.setAutoCommit(false);
            statement = connection.createStatement();

            String id = Integer.toString(book.getId());
            String description = book.getDescription();
            String site = book.getSite();
            String sql = "UPDATE books SET " +
                    "getDescription = " + description + ", " +
                    "getSite = " + site + " where " +
                    "id = " + id + ";";

            statement.executeUpdate(sql);
            statement.close();
            connection.commit();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        System.out.println("Book with id " + Integer.toString(book.getId()) + " updated successfully");
        return true;
    }
}
