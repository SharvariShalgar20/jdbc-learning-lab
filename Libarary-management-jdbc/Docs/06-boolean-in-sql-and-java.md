# 06 — Boolean in SQL and Java

> **How the `available` column works across MySQL and Java — and what TINYINT(1) actually is.**

---

## The `available` Column

In this project, every book has an `available` field:
- `true` → the book is on the shelf, can be issued
- `false` → the book has been issued to someone

In Java (`Book.java`):
```java
private boolean available;
```

In MySQL (`books` table):
```sql
available BOOLEAN DEFAULT TRUE
```

These look like the same type, but they're not quite — MySQL handles booleans
differently than Java.

---

## How MySQL Stores Booleans

MySQL does not have a true native boolean type. When you write `BOOLEAN` in a
`CREATE TABLE` statement, MySQL silently converts it to `TINYINT(1)` — a tiny
integer that stores only 0 or 1:

```sql
-- What you write:
available BOOLEAN DEFAULT TRUE

-- What MySQL actually stores:
available TINYINT(1) DEFAULT 1
```

| SQL value | Meaning      | Java reads it as |
|-----------|--------------|------------------|
| `1`       | true / yes   | `true`           |
| `0`       | false / no   | `false`          |

The JDBC driver (`mysql-connector-j`) handles this conversion transparently.
When you call `rs.getBoolean("available")`, the driver reads the `TINYINT` value
and returns a Java `boolean`.

---

## Setting `available` in SQL

### Setting available to `false` (issuing the book):
```sql
UPDATE books SET available = false WHERE id = ?;
```
MySQL accepts the literal keywords `true` and `false` in SQL — they're aliases for `1` and `0`.

### Equivalent ways to write the same thing:
```sql
UPDATE books SET available = false WHERE id = 1;
UPDATE books SET available = 0     WHERE id = 1;  -- same thing
UPDATE books SET available = FALSE WHERE id = 1;  -- same thing (case-insensitive)
```

---

## Reading `available` in Java

In `BookDAO.getAllBooks()`:

```java
rs.getBoolean("available")
```

The JDBC driver reads the `TINYINT(1)` value:
- `1` → returns `true`
- `0` → returns `false`

This is why the `Book` constructor can directly receive a Java `boolean`:

```java
books.add(new Book(
    rs.getInt("id"),
    rs.getString("title"),
    rs.getString("author"),
    rs.getBoolean("available")   // ← TINYINT(1) from DB becomes Java boolean
));
```

---

## Displaying `available` in `Main.java`

```java
books.forEach(b ->
    System.out.println(
        b.getId() + " | " + b.getTitle() + " | " + b.getAuthor() + " | " + b.isAvailable()
    )
);
```

`b.isAvailable()` returns `true` or `false`.
Java's `System.out.println` prints booleans as the strings `"true"` or `"false"`.

Sample output:
```
1 | Clean Code | Robert C. Martin | true
2 | Head First Java | Kathy Sierra | false
```

---

## The `DEFAULT TRUE` in the Table

```sql
CREATE TABLE books (
    id        INT          AUTO_INCREMENT PRIMARY KEY,
    title     VARCHAR(255) NOT NULL,
    author    VARCHAR(255) NOT NULL,
    available BOOLEAN      DEFAULT TRUE    ← this line
);
```

`DEFAULT TRUE` means: when a book is inserted *without* specifying `available`,
MySQL automatically sets it to `1` (true).

That's why `addBook()` in `BookDAO.java` doesn't set `available` at all:

```java
String sql = "INSERT INTO books (title, author) VALUES (?, ?)";
//                               ^^^^^  ^^^^^^
//                               available is omitted — DB uses DEFAULT TRUE
```

Every newly added book starts as available automatically. No Java code needed.

---

## Querying by Boolean Value

If you wanted to find only available books (a useful extension to this project):

```java
// SQL
String sql = "SELECT * FROM books WHERE available = true";

// Or with PreparedStatement for a filter parameter:
String sql = "SELECT * FROM books WHERE available = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setBoolean(1, true);   // ← setBoolean maps Java true → MySQL 1
ResultSet rs = ps.executeQuery();
```

---

