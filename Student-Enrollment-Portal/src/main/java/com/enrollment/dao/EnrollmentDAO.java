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

            String capacityCheck = """
                    SELECT c.max_capacity, COUNT(e.id) AS enrolled
                    FROM courses c LEFT JOIN enrollments e
                    ON c.id = e.course.id
                    WHERE c.id = ?
                    GROUP BY c.max_capacity
                    """;

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
            String sql = "INSERT INTO enrollments (student_id, course_id) VALUES INTO (?, ?)";

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



}
