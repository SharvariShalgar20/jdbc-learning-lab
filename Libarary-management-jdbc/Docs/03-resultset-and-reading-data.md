# 03 — ResultSet and Reading Data

> **How SELECT results come back to Java — and how to read them row by row.**

---

## What is a ResultSet?

When you run a `SELECT` query, MySQL sends back a table of rows. In Java, that table
is wrapped in a `ResultSet` object. Think of it as a **cursor** that starts before
the first row and moves forward one row at a time as you call `rs.next()`.

```
Before any rs.next() call:
┌─────────────────────────────────────────┐
│  cursor → [ BEFORE FIRST ROW ]          │
│           ┌────┬───────────┬────────┐   │
│           │ id │   title   │ author │   │
│           ├────┼───────────┼────────┤   │
│           │  1 │ Clean Code│ Martin │   │
│           │  2 │ Java JDBC │ Raj    │   │
│           └────┴───────────┴────────┘   │
└─────────────────────────────────────────┘

After rs.next() → true:   cursor is ON row 1 — you can read it
After rs.next() → true:   cursor is ON row 2 — you can read it
After rs.next() → false:  no more rows — loop ends
```

---

## `rs.next()` — The Loop Mechanism

`rs.next()` does two things at once:
1. Moves the cursor to the next row
2. Returns `true` if that row exists, `false` if there are no more rows

```java
// This is why while(rs.next()) works perfectly as a loop condition:
while (rs.next()) {          // move forward; if a row exists, enter the loop body
    // read the current row here
}
```

For a single expected row (like fetching by ID), use `if` instead of `while`:

```java
if (rs.next()) {             // check if at least one row came back
    // read the single row
} else {
    System.out.println("Book not found.");
}
```

---

## Reading Column Values

Once you're on a row, you read columns by **name** or by **index**:

```java
// By column name (preferred — more readable, order-independent)
int     id        = rs.getInt("id");
String  title     = rs.getString("title");
String  author    = rs.getString("author");
boolean available = rs.getBoolean("available");

// By column index (1-based — works but fragile if column order changes)
int     id        = rs.getInt(1);
String  title     = rs.getString(2);
String  author    = rs.getString(3);
boolean available = rs.getBoolean(4);
```

Always prefer reading **by column name** — it makes code readable and won't break
if someone changes the column order in the SQL.

---

## From `getAllBooks()` — The Full Pattern

```java
// BookDAO.java
public List<Book> getAllBooks() {
    List<Book> books = new ArrayList<>();
    String sql = "SELECT * FROM books";

    try (Connection conn = DBConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {    // ← ResultSet opened here

        while (rs.next()) {                           // ← iterate each row
            books.add(new Book(
                rs.getInt("id"),                      // ← read int column
                rs.getString("title"),                // ← read String column
                rs.getString("author"),               // ← read String column
                rs.getBoolean("available")            // ← read boolean column
            ));
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return books;    // list is populated from DB rows
}
```

Each call to `rs.next()` advances to the next row. Each `rs.getXxx("column")` reads
a value from the current row. A new `Book` object is built from each row and added to the list.

---

## The ResultSet Getter Methods

| Method                    | Java type  | Use for MySQL type         |
|---------------------------|------------|----------------------------|
| `rs.getInt("col")`        | `int`      | INT, SMALLINT              |
| `rs.getString("col")`     | `String`   | VARCHAR, TEXT, CHAR        |
| `rs.getBoolean("col")`    | `boolean`  | TINYINT(1), BOOLEAN        |
| `rs.getDouble("col")`     | `double`   | DOUBLE, DECIMAL            |
| `rs.getLong("col")`       | `long`     | BIGINT                     |
| `rs.getDate("col")`       | `java.sql.Date` | DATE                  |
| `rs.getTimestamp("col")`  | `Timestamp` | DATETIME, TIMESTAMP       |
| `rs.getObject("col")`     | `Object`   | Any type (check for null)  |

---

## Handling NULL Values

In SQL, a column can contain `NULL` (no value). If you call `rs.getInt("col")` on a
NULL column, it returns `0` — not an exception. Similarly `rs.getBoolean` returns `false`.

To detect actual NULL:

```java
int value = rs.getInt("some_column");
if (rs.wasNull()) {
    // the column was actually NULL in the database
    System.out.println("No value set.");
}
```

In this project, `available` defaults to `true` in the DB, so NULL is never an issue —
but it's good to know this exists.

---

## Why ResultSet Must Be Closed

A `ResultSet` holds an open cursor on the database side. If you never close it,
the database keeps that cursor alive indefinitely.

In this project, `ResultSet` is inside the try-with-resources block:

```java
try (Connection conn = DBConnection.getConnection();
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery(sql)) {     // ← rs is declared here
    
    // use rs...

}   // ← rs, stmt, conn are ALL closed automatically here, in reverse order
```

The closing order matters: `rs` closes first, then `stmt`, then `conn`.
Try-with-resources handles this correctly by closing in reverse declaration order.

---

## What Happens After the Loop

After `while (rs.next())` returns `false` and the loop ends, the ResultSet is exhausted.
You cannot go back. ResultSet is **forward-only by default** — there is no `rs.previous()`.

If you need to iterate twice, you'd have to run the query again, or collect results
into a `List` first (which is exactly what `getAllBooks()` does — it builds a `List<Book>`
so the caller can iterate it as many times as needed).

---

## Displaying Results — From `Main.java`

After `getAllBooks()` returns the list, `Main.java` prints each book:

```java
List<Book> books = dao.getAllBooks();
books.forEach(b ->
    System.out.println(
        b.getId() + " | " + b.getTitle() + " | " + b.getAuthor() + " | " + b.isAvailable()
    )
);
```

Sample output:
```
1 | Clean Code | Robert C. Martin | true
2 | Java for Beginners | Raj Kumar | false
3 | Head First Java | Kathy Sierra | true
```

The `true`/`false` at the end is the `available` boolean — `false` means the book is issued.
