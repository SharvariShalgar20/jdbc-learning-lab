package com.Sharvari.libraryManagement.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/library_db";

    private static final String USER = "root";
    private static final String PASSWORD = "Sharvarishalgar@2005";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
