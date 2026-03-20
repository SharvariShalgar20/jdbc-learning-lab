package com.Sharvari.libraryManagement.dao;

import com.Sharvari.libraryManagement.model.Book;
import com.Sharvari.libraryManagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public void addBook(Book book) {
        String sql = "INSERT INTO books (title, author) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
              PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(1, book.getAuthor());

            stmt.executeUpdate();
            System.out.println("Book added!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
