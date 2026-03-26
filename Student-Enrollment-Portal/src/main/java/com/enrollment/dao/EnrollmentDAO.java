package com.enrollment.dao;

import com.enrollment.DBConnection;

import java.sql.*;

public class EnrollmentDAO {

    public void enrollStudent(int studentId, int courseId) throws SQLException {
        Connection conn = null;

        //check max_capacity and enrolled
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String capacityCheck = "SELECT c.max_capacity, COUNT(e.id) AS enrolled " +
                    "FROM courses c LEFT JOIN enrollments e " +
                    "ON c.id = e.course_id " +
                    "WHERE c.id = ? " +
                    "GROUP BY c.max_capacity";

            try (PreparedStatement stmt = conn.prepareStatement(capacityCheck)) {
                stmt.setInt(1, courseId);
                ResultSet rs = stmt.executeQuery();

                if(rs.next()) {
                    int max = rs.getInt("max_capacity");
                    int count = rs.getInt("enrolled");

                    if(count >= max) {
                        throw new SQLException("Course is full! (" + count + "/" + max + ")");
                    }
                }
            }

            //Insert enrollemnt
            String sql = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, courseId);
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Student enrolled successfully!");
        } catch (SQLException e) {
            if(conn != null) {
                conn.rollback(); // ❌ Something failed — ROLLBACK everything
                System.out.println("❌ Enrollment failed. Transaction rolled back.");
                System.out.println("   Reason: " + e.getMessage());
            }
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Reset to default
                conn.close();
            }
        }
    }

    public void viewEnrollment() throws SQLException {
        String sql = "SELECT s.name AS student, c.title AS course, e.enrolled_at " +
                "FROM enrollments e " +
                "JOIN students s ON e.student_id = s.id " +
                "JOIN courses c ON e.course_id = c.id " +
                "ORDER BY e.enrolled_at DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- All Enrollments ---");
            boolean isthere = false;

            while(rs.next()) {
                isthere = true;
                System.out.printf("Student: %-20s | Course: %-30s | At: %s%n",
                        rs.getString("student"),
                        rs.getString("course"),
                        rs.getTimestamp("enrolled_at"));
            }

            if (!isthere) System.out.println("No enrollments yet.");
        }
    }


    public void dropEnrollment(int student_id, int course_id) throws SQLException{
        String sql = "DELETE FROM enrollments WHERE student_id = ? AND course_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, student_id);
            stmt.setInt(2, course_id);

            int row = stmt.executeUpdate();
            System.out.println(row > 0 ? "✅ Dropped." : "❌ Enrollment not found.");
        }
    }

}
