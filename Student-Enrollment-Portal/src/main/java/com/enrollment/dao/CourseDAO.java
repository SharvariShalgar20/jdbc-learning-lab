package com.enrollment.dao;

import com.enrollment.DBConnection;
import com.enrollment.model.Course;

import java.sql.Connection;
import java.sql.*;
import java.util.*;

public class CourseDAO {

    public void addCourse(String title, int credits, int max_capacity) throws SQLException {
        String sql = "INSERT INTO courses(title, credits, max_capacity) VALUES (?, ?, ?)";

        try( Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setInt(2, credits);
            stmt.setInt(3, max_capacity);

            int rows = stmt.executeUpdate();
            System.out.println(rows + " ✅ Course added.");
        }
    }

    public List<Course> getAllCourses() throws SQLException{
        List<Course> result = new ArrayList<>();
        String sql = "SELECT * FROM courses";

        try(Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while(rs.next()) {
                result.add(new Course(rs.getInt("id"),
                                      rs.getString("title"),
                                      rs.getInt("credits"),
                                      rs.getInt("max_capacity")));
            }
        }
        return result;
    }
}
