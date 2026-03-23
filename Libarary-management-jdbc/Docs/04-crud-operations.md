# 04 — CRUD Operations

> **The three SQL operations this project implements — INSERT, SELECT, UPDATE — and exactly how each maps to JDBC code.**

---

## CRUD in This Project

This project implements three of the four CRUD operations:

| Letter | Operation | Implemented? | Method in BookDAO    |
|--------|-----------|--------------|----------------------|
| C      | CREATE    | ✅ Yes        | `addBook()`          |
| R      | READ      | ✅ Yes        | `getAllBooks()`       |
| U      | UPDATE    | ✅ Yes        | `issueBook()`        |
| D      | DELETE    | ❌ Not yet    | — (can extend later) |

This is intentional for a first project — DELETE adds edge cases (what if the ID doesn't exist?)
that distract from the core learning.

---

## C — CREATE: `addBook()`

### What it does:
Takes a `Book` object and inserts a new row into the `books` table.

### The SQL:
```sql
INSERT INTO books (title, author) VALUES (?, ?);
```

> Notice: `id` and `available` are **not** in the column list.
> `id` is `AUTO_INCREMENT` — MySQL generates it automatically.
> `available` has a `DEFAULT TRUE` — MySQL sets it to true automatically.
> You only provide what you know; the DB fills in the rest.

### The JDBC code:
```java
public void addBook(Book book) {
    String sql = "INSERT INTO books (title, author) VALUES (?, ?)";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, book.getTitle());   // bind title to first  ?
        stmt.setString(2, book.getAuthor());  // bind author to second ?

        stmt.executeUpdate();                 // returns int (rows affected), ignored here
        System.out.println("Book added!");

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
```

### The `books` table that supports this:
```sql
CREATE TABLE books (
    id        INT          AUTO_INCREMENT PRIMARY KEY,
    title     VARCHAR(255) NOT NULL,
    author    VARCHAR(255) NOT NULL,
    available BOOLEAN      DEFAULT TRUE
);
```

---

## R — READ: `getAllBooks()`

### What it does:
Fetches every row from the `books` table and returns them as a `List<Book>`.

### The SQL:
```sql
SELECT * FROM books;
```

`SELECT *` means "give me all columns". For a small learning project this is fine.
In production you'd name specific columns (`SELECT id, title, author, available FROM books`)
to avoid surprises if someone adds a column later.

### The JDBC code:
```java
public List<Book> getAllBooks() {
    List<Book> books = new ArrayList<>();
    String sql = "SELECT * FROM books";

    try (Connection conn = DBConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {    // executeQuery for SELECT

        while (rs.next()) {
            books.add(new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getBoolean("available")
            ));
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return books;
}
```

### Key points:
- Uses `Statement` (not `PreparedStatement`) because the SQL has no parameters
- Uses `executeQuery()` — the only method that returns a `ResultSet`
- Builds a `Book` object per row and collects them into a `List`

---

## U — UPDATE: `issueBook()`

### What it does:
Marks a book as unavailable (issued) by setting `available = false` for a given ID.

### The SQL:
```sql
UPDATE books SET available = false WHERE id = ?;
```

This is a **targeted update** — the `WHERE` clause ensures only one specific row
is changed. Without `WHERE`, every book in the table would be marked as issued.

### The JDBC code:
```java
public void issueBook(int id) {
    String sql = "UPDATE books SET available = false WHERE id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, id);        // bind the book ID to the WHERE clause
        stmt.executeUpdate();      // UPDATE uses executeUpdate(), not executeQuery()
        System.out.println("Book issued!");

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
```

### What you could improve here:
The current code prints "Book issued!" even if the `id` doesn't exist. That's because
`executeUpdate()` returns the number of rows affected, but the return value is ignored.

A better version:
```java
int rowsAffected = stmt.executeUpdate();
if (rowsAffected > 0) {
    System.out.println("Book issued!");
} else {
    System.out.println("No book found with ID: " + id);
}
```

---

## D — DELETE: The Missing Operation (How to Add It)

DELETE is not in the project yet, but here's how it would look — using the same
pattern as `issueBook()`:

```java
public void deleteBook(int id) {
    String sql = "DELETE FROM books WHERE id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, id);
        int rowsAffected = stmt.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("Book deleted.");
        } else {
            System.out.println("No book with ID " + id + " was found.");
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
```

Adding this to `BookDAO.java` and wiring it to a new menu option in `Main.java`
would complete the full CRUD set.

---

## Side-by-Side Comparison

```java
// CREATE
String sql = "INSERT INTO books (title, author) VALUES (?, ?)";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, title);
ps.setString(2, author);
ps.executeUpdate();           // ← executeUpdate for write operations

// READ
String sql = "SELECT * FROM books";
Statement st = conn.createStatement();
ResultSet rs = st.executeQuery(sql);  // ← executeQuery for SELECT
while (rs.next()) { /* read row */ }

// UPDATE
String sql = "UPDATE books SET available = false WHERE id = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setInt(1, id);
ps.executeUpdate();           // ← executeUpdate for write operations

// DELETE (pattern)
String sql = "DELETE FROM books WHERE id = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setInt(1, id);
ps.executeUpdate();           // ← executeUpdate for write operations
```

The pattern is consistent: writes use `executeUpdate()`, reads use `executeQuery()`.
