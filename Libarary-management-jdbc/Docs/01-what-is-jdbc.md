# 01 — What is JDBC?

> **The bridge between your Java code and the MySQL database. Understanding how the connection is made.**

---

## What is JDBC?

**JDBC (Java Database Connectivity)** is a Java API that lets your program send SQL to a
database and get results back — all from Java code. It's built into the JDK inside the
`java.sql` package, so you don't need to install anything special for the API itself.

What you *do* need is a **JDBC Driver** — a `.jar` file from the database vendor that
handles the actual network communication.

---

## The Big Picture

```
Your Java Code  (Main.java, BookDAO.java)
        │
        │  uses java.sql interfaces
        ▼
    JDBC API  ── java.sql.Connection
               ── java.sql.Statement
               ── java.sql.PreparedStatement
               ── java.sql.ResultSet
        │
        │  implemented by
        ▼
  MySQL JDBC Driver  (mysql-connector-j JAR)
        │
        │  TCP/IP over port 3306
        ▼
  MySQL Database  (library_db)
```

Your code talks to the **JDBC API** (generic interfaces).
The **MySQL Driver** handles everything database-specific underneath.
This is why switching from MySQL to PostgreSQL only requires changing the URL and driver JAR —
your Java code stays the same.

---

## `DBConnection.java` — Line by Line

```java
public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/library_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "yourpassword";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

### Breaking down the URL:

```
jdbc:mysql://localhost:3306/library_db
│    │       │         │    │
│    │       │         │    └─── Database name (must exist in MySQL)
│    │       │         └──────── Port (MySQL default is 3306)
│    │       └────────────────── Host (your own machine = localhost)
│    └────────────────────────── Driver protocol (mysql, postgresql, etc.)
└─────────────────────────────── Always starts with "jdbc:"
```

### What `DriverManager.getConnection()` does:
1. Reads the URL prefix (`jdbc:mysql`) to find the right driver
2. Loads the MySQL driver from your classpath (the JAR in `build.gradle`)
3. Opens a TCP socket to `localhost:3306`
4. Authenticates with `USER` and `PASSWORD`
5. Returns a live `Connection` object if successful

---

## Adding the MySQL Driver in Gradle

In `build.gradle`, you add the MySQL connector as a dependency:

```groovy
dependencies {
    implementation 'com.mysql.cj:mysql-connector-j:8.3.0'
}
```

After adding this, IntelliJ downloads the JAR from Maven Central.
Without this JAR on the classpath, `DriverManager.getConnection()` throws:

```
java.sql.SQLException: No suitable driver found for jdbc:mysql://...
```

> In modern JDBC (4.0+), you do **not** need `Class.forName("com.mysql.cj.jdbc.Driver")`.
> The driver registers itself automatically when the JAR is on the classpath.

---

## What `getConnection()` Returns

It returns a `java.sql.Connection` — an open, live session with the database.
Think of it as a **phone call that's been connected** — as long as you hold the `Connection`
object open, you have a dedicated session with MySQL. When you call `conn.close()`, you hang up.

---

## ⚠️ Security Note on Credentials

In `DBConnection.java`, the password is hardcoded as a plain string:

```java
private static final String PASSWORD = "PASS@12345";
```

This is fine for learning, but **never do this in real projects** that are pushed to GitHub
or shared. In production, credentials come from:
- Environment variables: `System.getenv("DB_PASSWORD")`
- A `.env` file (not committed to git)
- A secrets manager

---

## The One Rule of Connections

**Every connection you open must eventually be closed.**

A connection holds resources on both the Java side (memory, socket) and the MySQL side
(a process slot). If you open connections and never close them, MySQL eventually runs out
of available connections and rejects new ones.

This project handles this correctly with try-with-resources:

```java
//try (Connection conn = DBConnection.getConnection();
//     ...) {
//    // connection is automatically closed when this block exits
//}
```


