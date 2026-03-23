# 02 — Statement vs PreparedStatement

> **This project uses both. Understanding why each is used where it is — and why PreparedStatement is almost always the right choice.**

---

## The Two Ways to Execute SQL in JDBC

### `Statement` — for fixed SQL with no user input

```java
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery("SELECT * FROM books");
```

The SQL string is complete and static — no variables, no user input, nothing changes.

### `PreparedStatement` — for SQL with parameters

```java
//PreparedStatement stmt = conn.prepareStatement("INSERT INTO books (title, author) VALUES (?, ?)");
//stmt.setString(1, book.getTitle());
//stmt.setString(2, book.getAuthor());
//stmt.executeUpdate();
```

The `?` marks are **placeholders**. You fill them in with setter methods before executing.

---

## How This Project Uses Both

### `getAllBooks()` → uses `Statement`

```java
// BookDAO.java
//String sql = "SELECT * FROM books";
//Statement stmt = conn.createStatement();
//ResultSet rs = stmt.executeQuery(sql);
```

**Why `Statement` here?** The SQL is completely fixed — no user-supplied values.
`SELECT * FROM books` always looks exactly like that. Using `Statement` is fine
when there's nothing variable in the query.

---

### `addBook()` → uses `PreparedStatement`

```java
// BookDAO.java
//String sql = "INSERT INTO books (title, author) VALUES (?, ?)";
//PreparedStatement stmt = conn.prepareStatement(sql);
//stmt.setString(1, book.getTitle());   // replaces first  ?
//stmt.setString(2, book.getAuthor());  // replaces second ?
//stmt.executeUpdate();
```

**Why `PreparedStatement` here?** The title and author come from the user typing
into the Scanner. User input is untrusted — it could accidentally (or deliberately)
contain characters that break your SQL.

---

### `issueBook()` → uses `PreparedStatement`

```java
// BookDAO.java
//String sql = "UPDATE books SET available = false WHERE id = ?";
//PreparedStatement stmt = conn.prepareStatement(sql);
//stmt.setInt(1, id);   // the book ID typed by the user
//stmt.executeUpdate();
```

**Why `PreparedStatement` here?** The `id` comes from `Scanner` input. Same reason
as above — any value that comes from outside your code belongs in a `?` placeholder.

---

## How `PreparedStatement` Works Internally

```
Statement approach:
Java → sends one combined string → "INSERT INTO books VALUES ('Java 101', 'Raj')"
MySQL → parses SQL + data together → risk: user can inject SQL

PreparedStatement approach:
Java → sends SQL template  → "INSERT INTO books VALUES (?, ?)"   (MySQL parses this)
Java → sends parameter 1   → "Java 101"                          (pure data)
Java → sends parameter 2   → "Raj"                               (pure data)
MySQL → substitutes safely → no way for data to become SQL code
```

---

## The Setter Methods — Matching Java Types to SQL Types

Parameters are **1-indexed** (first `?` = position 1):

```java
//PreparedStatement ps = conn.prepareStatement(
//    "INSERT INTO books (title, author, available) VALUES (?, ?, ?)"
//);
//
//ps.setString(1, "Clean Code");   // ? 1 → VARCHAR
//ps.setString(2, "Robert Martin");// ? 2 → VARCHAR
//ps.setBoolean(3, true);          // ? 3 → TINYINT(1) / BOOLEAN
```

| Setter method       | Java type   | Maps to MySQL type          |
|---------------------|-------------|-----------------------------|
| `setString(i, v)`   | `String`    | VARCHAR, TEXT               |
| `setInt(i, v)`      | `int`       | INT                         |
| `setBoolean(i, v)`  | `boolean`   | TINYINT(1), BOOLEAN         |
| `setDouble(i, v)`   | `double`    | DOUBLE, DECIMAL             |
| `setLong(i, v)`     | `long`      | BIGINT                      |
| `setNull(i, type)`  | `null`      | NULL                        |

---

## `executeQuery()` vs `executeUpdate()`

This is a common point of confusion:

| Method              | Use for               | Returns                            |
|---------------------|-----------------------|------------------------------------|
| `executeQuery()`    | `SELECT`              | `ResultSet` (the rows)             |
| `executeUpdate()`   | `INSERT`, `UPDATE`, `DELETE` | `int` (number of rows affected) |

In this project:

```java
//// getAllBooks() — SELECT → executeQuery()
//ResultSet rs = stmt.executeQuery(sql);
//
//// addBook() — INSERT → executeUpdate()
//stmt.executeUpdate();
//
//// issueBook() — UPDATE → executeUpdate()
//stmt.executeUpdate();
```

Calling `executeUpdate()` on a SELECT (or vice versa) will either throw an exception
or return meaningless results. Always match the method to the SQL command.

---

## Quick Decision Guide

```
Does the SQL contain any value that comes from outside your code?
(user input, a variable, a method parameter)
        │
        ├── YES → PreparedStatement with ? placeholders
        │
        └── NO  → Statement is fine (but PreparedStatement also works — no harm)
```

