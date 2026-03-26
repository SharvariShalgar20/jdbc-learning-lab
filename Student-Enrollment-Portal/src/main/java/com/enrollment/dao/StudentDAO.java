package com.enrollment.dao;

import com.enrollment.DBConnection;
import com.enrollment.model.Student;

import java.sql.*;
import java.util.*;


public class StudentDAO {

    public void addStudent(String name, String email) throws SQLException {
        String sql = "INSERT INTO students(name, email) VALUES (?, ?)";

        try ( Connection conn = DBConnection.getConnection();
              PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1,name);
            stmt.setString(2, email);

            stmt.executeUpdate();
            System.out.println("✅ Student added successfully.");
        }
    }

    public List<Student> getAllStudents() throws SQLException{
        List<Student> result = new ArrayList<>();

        String sql = "SELECT * FROM students";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while(rs.next()) {
                result.add( new Student(rs.getInt("id"),
                                        rs.getString("name"),
                                        rs.getString("email")));
            }
        }
        return result;
    }

    public void updateStudent(int id, String newName, String newEmail) throws SQLException {
        String sql = "UPDATE students SET name=?, email=? WHERE id =?";

        try ( Connection conn = DBConnection.getConnection();
              PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setString(2, newEmail);
            stmt.setInt(3, id);

            int rows = stmt.executeUpdate();
            System.out.println(rows > 0 ? "✅ Updated." : "❌ Student not found.");
        }
    }

    public void deleteStudent(int id) throws SQLException{

        String sql = "DELETE FROM students where id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            System.out.println(rows > 0 ? "✅ Deleted." : "❌ Student not found.");
        }
    }
}
