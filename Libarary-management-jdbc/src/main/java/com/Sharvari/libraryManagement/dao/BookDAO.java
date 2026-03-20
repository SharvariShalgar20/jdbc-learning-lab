package com.Sharvari.libraryManagement.dao;

import com.Sharvari.libraryManagement.model.Book;
import com.Sharvari.libraryManagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    // add a Book
    public void addBook(Book book) {
        String sql = "INSERT INTO books (title, author) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
              PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());

            stmt.executeUpdate();
            System.out.println("Book added!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //view all books
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();

        String sql = "SELECT * FROM books";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)){

            while(rs.next()){
                books.add(new Book(rs.getInt("id"),
                                   rs.getString("title"),
                                   rs.getString("author"),
                                   rs.getBoolean("available")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return books;
    }

    //Issue Book -> update
    public void issueBook(int id) {
        String sql = "UPDATE books SET available = false WHERE id = ?";

        try ( Connection conn = DBConnection.getConnection();
              PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

            System.out.println("Book issued!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
