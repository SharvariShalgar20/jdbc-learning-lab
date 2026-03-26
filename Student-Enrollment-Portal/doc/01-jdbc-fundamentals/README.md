# 01 — JDBC Fundamentals

> **What is JDBC and how does it fit into a Java application?**

---

## What is JDBC?

**JDBC (Java Database Connectivity)** is a standard Java API that lets your Java program
talk to a relational database — regardless of which database (PostgreSQL, MySQL, Oracle, etc.)
you're using. It's part of the `java.sql` package, built into the JDK.

Think of JDBC as a **universal remote control** for databases. You write Java code once;
the underlying JDBC Driver handles the database-specific communication.

---

## How It Works — The Big Picture

```
Your Java Code
     │
     │  uses java.sql interfaces
     ▼
  JDBC API  (java.sql.*)
     │
     │  talks to
     ▼
JDBC Driver  (e.g. org.postgresql:postgresql:42.7.3)
     │
     │  sends SQL over network
     ▼
  Database  (PostgreSQL / MySQL)
```

The **JDBC Driver** is the bridge. Without it, your Java code has no way to reach the database.

---

## The 4 Core JDBC Interfaces

| Interface           | What it does                                        | Created by                    |
|---------------------|-----------------------------------------------------|-------------------------------|
| `Connection`        | Represents an open connection to the DB             | `DriverManager.getConnection()`|
| `Statement`         | Executes a static SQL string                        | `connection.createStatement()` |
| `PreparedStatement` | Executes a parameterized SQL query (preferred)      | `connection.prepareStatement()`|
| `ResultSet`         | Holds the rows returned by a `SELECT` query         | `statement.executeQuery()`     |

---

## The JDBC Driver — What It Is and How to Add It

A JDBC Driver is a `.jar` file provided by the database vendor. It implements all the
`java.sql` interfaces for that specific database.

### Adding the driver in Gradle (`build.gradle`):

```groovy
dependencies {
    // PostgreSQL
    implementation 'org.postgresql:postgresql:42.7.3'

    // OR MySQL
    // implementation 'com.mysql.cj:mysql-connector-j:8.3.0'
}
```

You **do not** need to call `Class.forName(...)` in modern JDBC (4.0+). The driver
is auto-detected from the classpath via the Service Provider mechanism.

---

## A Minimal JDBC Program

```java
import java.sql.*;

public class HelloJDBC {
    public static void main(String[] args) throws SQLException {
        // 1. Open a connection
        String url = "jdbc:postgresql://localhost:5432/enrollment_db";
        Connection conn = DriverManager.getConnection(url, "postgres", "password");

        // 2. Create a statement
        Statement st = conn.createStatement();

        // 3. Execute SQL
        ResultSet rs = st.executeQuery("SELECT name FROM students");

        // 4. Read results
        while (rs.next()) {
            System.out.println(rs.getString("name"));
        }

        // 5. Close everything
        rs.close();
        st.close();
        conn.close();
    }
}
```

---

## The JDBC URL Format

The URL tells JDBC **which driver to use** and **where the database is**:

```
jdbc:postgresql://localhost:5432/enrollment_db
│    │            │         │    │
│    │            │         │    └─ Database name
│    │            │         └────── Port
│    │            └──────────────── Host
│    └───────────────────────────── Driver/protocol
└────────────────────────────────── JDBC prefix (always "jdbc:")
```

For MySQL it would be: `jdbc:mysql://localhost:3306/enrollment_db`

---

## Key JDBC Exceptions

Everything in JDBC throws `SQLException`. You either:
- `throws SQLException` on the method signature (what this project does for simplicity)
- Or wrap it in a `try-catch` block

```java
try {
    Connection conn = DriverManager.getConnection(url, user, pass);
} catch (SQLException e) {
    System.out.println("Error code: " + e.getErrorCode());
    System.out.println("SQL state: " + e.getSQLState());
    System.out.println("Message: " + e.getMessage());
}
```

---

## Where This Fits in the Project

In `DBConnection.java`:

```java
public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
}
```

Every DAO class calls `DBConnection.getConnection()` to get a fresh `Connection` object.
This is the entry point for all JDBC operations in the project.

---

## Common Mistakes

| Mistake | What happens | Fix |
|---------|--------------|-----|
| Wrong JDBC URL | `Connection refused` | Double-check port and DB name |
| Driver JAR not in classpath | `No suitable driver found` | Re-sync Gradle, check `build.gradle` |
| Wrong credentials | `password authentication failed` | Verify user/password |
| DB not running | `Connection refused` | Start PostgreSQL service |
