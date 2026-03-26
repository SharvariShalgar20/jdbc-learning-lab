# 05 — Connection Management

> **How to open, use, and close database connections safely in Java.**

---

## What is a Connection?

A `Connection` object represents **an open, live socket** between your Java application
and the database server. It's expensive to create (network handshake, authentication,
session setup), and it holds resources on both ends.

This means two things:
1. You must **always close it** when you're done
2. You shouldn't create more connections than necessary

---

## The `DBConnection` Utility Class

In this project, `DBConnection.java` centralizes how connections are obtained:

```java
public class DBConnection {
    private static final String URL  = "jdbc:postgresql://localhost:5432/enrollment_db";
    private static final String USER = "postgres";
    private static final String PASS = "yourpassword";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

Every DAO calls `DBConnection.getConnection()` to get a fresh connection.
This is a simple pattern — suitable for learning projects and small apps.

---

## Closing Connections — The Three Resources

Every JDBC operation uses up to three objects that must all be closed:

```
Connection  →  creates  →  Statement / PreparedStatement  →  creates  →  ResultSet
    ↑                               ↑                                         ↑
  close()                        close()                                   close()
```

Closing a `Connection` automatically closes its `Statement` objects.
Closing a `Statement` automatically closes its `ResultSet`.
But relying on this implicit cascade is risky — always close explicitly.

---

## The Old Way vs The Correct Way

### ❌ Old way (pre-Java 7) — verbose and error-prone

```java
Connection conn = null;
PreparedStatement ps = null;
ResultSet rs = null;
try {
    conn = DBConnection.getConnection();
    ps = conn.prepareStatement("SELECT * FROM students");
    rs = ps.executeQuery();
    while (rs.next()) { /* ... */ }
} catch (SQLException e) {
    e.printStackTrace();
} finally {
    // Must close in reverse order, each with its own try-catch
    try { if (rs   != null) rs.close();   } catch (SQLException e) {}
    try { if (ps   != null) ps.close();   } catch (SQLException e) {}
    try { if (conn != null) conn.close(); } catch (SQLException e) {}
}
```

If you forget the `finally` block, connections are leaked. They accumulate until
the database runs out of available connections.

### ✅ Correct way — try-with-resources (Java 7+)

```java
try (Connection conn = DBConnection.getConnection();
     PreparedStatement ps = conn.prepareStatement("SELECT * FROM students");
     ResultSet rs = ps.executeQuery()) {

    while (rs.next()) { /* ... */ }

} catch (SQLException e) {
    e.printStackTrace();
}
// conn, ps, rs are ALL automatically closed here — even if an exception occurs
```

`Connection`, `PreparedStatement`, and `ResultSet` all implement `AutoCloseable`,
which is what enables try-with-resources. Java guarantees `.close()` is called on
each resource in reverse order when the block exits — whether normally or via exception.

---

## From This Project — `getAllStudents()`:

```java
public List<Student> getAllStudents() throws SQLException {
    List<Student> list = new ArrayList<>();
    String sql = "SELECT * FROM students";

    try (Connection conn = DBConnection.getConnection();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {

        while (rs.next()) {
            list.add(new Student(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email")
            ));
        }
    }
    return list;
}
```

The `try` block opens three resources; all three are closed automatically at the `}`.

---

## Connection Per Operation vs Connection Per Request

This project uses **connection-per-operation**: a new `Connection` is opened and
closed for every single DAO method call. This is simple but not ideal for
production systems.

```
addStudent()    → open connection → execute → close connection
getAllStudents() → open connection → execute → close connection
deleteStudent() → open connection → execute → close connection
```

### Why this is fine for learning:
- Simple to understand and debug
- No shared state between operations
- No risk of stale connections

### Why production apps use Connection Pooling instead:
Opening a connection takes ~20–100ms (network + auth). Under load, creating a
new connection per request would be too slow. A **connection pool** (HikariCP,
c3p0) pre-creates a set of connections and reuses them.

---

## Connection Lifecycle in the Enrollment Transaction

The enrollment operation (`EnrollmentDAO.enrollStudent()`) is the one place
where a connection is held open across multiple statements — intentionally,
because they share a transaction:

```java
Connection conn = null;
try {
    conn = DBConnection.getConnection();
    conn.setAutoCommit(false);   // Transaction starts

    // Step 1: capacity check (conn is still open)
    // Step 2: insert enrollment (same conn — same transaction)

    conn.commit();
} catch (SQLException e) {
    conn.rollback();             // Uses same conn to undo changes
} finally {
    conn.setAutoCommit(true);
    conn.close();                // Now we close
}
```

Both SQL statements must share the **same connection** because a transaction
is scoped to a connection. You cannot begin a transaction on `conn1` and commit
it on `conn2`.

---

## Common Connection Mistakes

| Mistake | Symptom | Fix |
|---------|---------|-----|
| Not closing connection | DB runs out of connections after many requests | Use try-with-resources |
| Closing connection before reading ResultSet | `ResultSet closed` exception | Read all rows before closing |
| Using different connections in one transaction | Transaction doesn't work | Pass the same `conn` object |
| Not resetting `autoCommit` | Next caller's simple queries are not auto-committed | Always reset in `finally` |
