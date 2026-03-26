# 04 — PreparedStatement

> **Why you should almost never use `Statement` and always use `PreparedStatement`.**

---

## Two Ways to Execute SQL in JDBC

### Option A — `Statement` (the naive way)

```java
String name = "Raj";
Statement st = conn.createStatement();
ResultSet rs = st.executeQuery("SELECT * FROM students WHERE name = '" + name + "'");
```

### Option B — `PreparedStatement` (the correct way)

```java
String name = "Raj";
PreparedStatement ps = conn.prepareStatement("SELECT * FROM students WHERE name = ?");
ps.setString(1, name);
ResultSet rs = ps.executeQuery();
```

The `?` is a **parameter placeholder**. The actual value is set separately via setter methods.

---

## Why `Statement` is Dangerous — SQL Injection

String concatenation lets a malicious user break out of your SQL:

```java
// User input:
String name = "' OR '1'='1";

// Your code:
String sql = "SELECT * FROM students WHERE name = '" + name + "'";

// What gets sent to the database:
// SELECT * FROM students WHERE name = '' OR '1'='1'
// ↑ This returns ALL rows — authentication bypass!
```

A more destructive example:

```java
String name = "'; DROP TABLE students; --";
// SELECT * FROM students WHERE name = ''; DROP TABLE students; --'
// ↑ Drops your entire students table
```

`PreparedStatement` makes this **impossible** because it sends the SQL structure
and the parameters separately. The DB treats parameters as pure data, never as SQL code.

---

## How PreparedStatement Works Internally

```
With Statement:
Java → sends "SELECT * FROM students WHERE name = 'Raj'" as one string → DB parses + executes

With PreparedStatement:
Java → sends "SELECT * FROM students WHERE name = ?"  (parse once, create execution plan)
Java → sends parameter value "Raj"                    (DB substitutes safely)
DB   → executes with "Raj" as literal data, not SQL
```

This separation is why injection is impossible: the `?` slot only accepts a value,
not SQL syntax.

---

## The Setter Methods

Parameters are **1-indexed** (first `?` is position 1, not 0):

```java
PreparedStatement ps = conn.prepareStatement(
    "INSERT INTO students (name, email) VALUES (?, ?)"
);

ps.setString(1, "Raj Kumar");   // position 1 → first ?
ps.setString(2, "raj@test.com"); // position 2 → second ?
```

| Method             | Java type  | SQL type          |
|--------------------|------------|-------------------|
| `setString(i, v)`  | `String`   | VARCHAR, TEXT     |
| `setInt(i, v)`     | `int`      | INT, SERIAL       |
| `setDouble(i, v)`  | `double`   | DOUBLE, NUMERIC   |
| `setBoolean(i, v)` | `boolean`  | BOOLEAN           |
| `setTimestamp(i,v)`| `Timestamp`| TIMESTAMP         |
| `setNull(i, type)` | `null`     | NULL              |

---

## executeUpdate() vs executeQuery()

```java
// For SELECT — returns a ResultSet
ResultSet rs = ps.executeQuery();

// For INSERT, UPDATE, DELETE — returns number of rows affected
int rowsAffected = ps.executeUpdate();

if (rowsAffected == 0) {
    System.out.println("No rows were changed — ID probably doesn't exist");
}
```

---

## Reusing a PreparedStatement

A `PreparedStatement` can be re-executed with different parameters — you
don't need to create a new one each time. This also gives a performance benefit
since the SQL is only parsed once:

```java
PreparedStatement ps = conn.prepareStatement("INSERT INTO students (name, email) VALUES (?, ?)");

// First student
ps.setString(1, "Raj");
ps.setString(2, "raj@test.com");
ps.executeUpdate();

// Second student — reuse the same PreparedStatement
ps.setString(1, "Priya");
ps.setString(2, "priya@test.com");
ps.executeUpdate();
```

---

## Reading Results with ResultSet

```java
PreparedStatement ps = conn.prepareStatement(
    "SELECT id, name, email FROM students WHERE id = ?"
);
ps.setInt(1, studentId);
ResultSet rs = ps.executeQuery();

if (rs.next()) {
    // rs.next() returns true if there's a row; moves cursor forward
    int id       = rs.getInt("id");
    String name  = rs.getString("name");
    String email = rs.getString("email");
    System.out.println(id + " " + name + " " + email);
} else {
    System.out.println("Student not found.");
}
```

For multiple rows use `while (rs.next())` instead of `if`.

---

## From This Project — `addStudent()`:

```java
public void addStudent(String name, String email) throws SQLException {
    String sql = "INSERT INTO students (name, email) VALUES (?, ?)";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, name);
        ps.setString(2, email);
        ps.executeUpdate();
    }
}
```

Notice the SQL string is written once with placeholders (`?`), and values are
bound with typed setter methods. This is the correct pattern for every write operation.

---

## Quick Reference: Statement vs PreparedStatement

| Feature                    | `Statement`       | `PreparedStatement`  |
|----------------------------|-------------------|----------------------|
| SQL injection safe         | ❌ No              | ✅ Yes               |
| Parameters                 | String concat     | `?` placeholders     |
| Reusable with new values   | ❌ No              | ✅ Yes               |
| Performance (repeated use) | Slower            | Faster (pre-compiled)|
| Code readability           | Gets messy        | Clean and clear      |
| **Use it?**                | **Rarely/Never**  | **Always**           |
