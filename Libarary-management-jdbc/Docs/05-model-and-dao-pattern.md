# 05 — Model and DAO Pattern

> **Why `Book.java` and `BookDAO.java` are two separate files — and why that matters.**

---

## The Problem Without Separation

Imagine writing everything in `Main.java`:

```java
// Main.java doing everything — messy, hard to maintain
Scanner sc = new Scanner(System.in);
Connection conn = DriverManager.getConnection("jdbc:mysql://...", "root", "pass");
PreparedStatement ps = conn.prepareStatement("INSERT INTO books (title, author) VALUES (?, ?)");
ps.setString(1, sc.nextLine());
ps.setString(2, sc.nextLine());
ps.executeUpdate();

Statement st = conn.createStatement();
ResultSet rs = st.executeQuery("SELECT * FROM books");
while (rs.next()) {
    System.out.println(rs.getInt("id") + rs.getString("title") + ...);
}
```

This works for 10 lines. At 200 lines it becomes unreadable. At 500 lines it becomes
unmaintainable. And if you want to reuse the "get all books" logic somewhere else
in the app, you copy-paste it — now you have two copies to update when something changes.

---

## The Solution: Two Patterns Working Together

### Pattern 1 — The Model (`Book.java`)
A plain Java class that **represents one row** from the `books` table in memory.

### Pattern 2 — The DAO (`BookDAO.java`)
A class whose only job is **talking to the database**. All SQL lives here.
The rest of the app never touches SQL directly.

---

## `Book.java` — The Model (Plain Old Java Object)

```java
public class Book {
    private int id;
    private String title;
    private String author;
    private boolean available;

    // Constructor for creating a new book (no ID yet — DB assigns it)
    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    // Constructor for a book that already exists in the DB (has an ID)
    public Book(int id, String title, String author, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.available = available;
    }

    // Getters only — no setters (immutable after creation)
    public int getId()          { return id; }
    public String getTitle()    { return title; }
    public String getAuthor()   { return author; }
    public boolean isAvailable(){ return available; }
}
```

**What this class is:**
- A container for book data — four fields matching the four columns in the `books` table
- No SQL. No `Connection`. No Scanner. No printing.
- Just data + getters.

**Two constructors — why?**

```java
// When the user adds a new book, we don't have an ID yet (MySQL will generate one):
new Book("Clean Code", "Robert C. Martin")

// When we read from the DB, we have all four values:
new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getBoolean("available"))
```

---

## `BookDAO.java` — The DAO (Data Access Object)

```java
public class BookDAO {
    public void addBook(Book book)      { /* INSERT SQL here */ }
    public List<Book> getAllBooks()     { /* SELECT SQL here */ }
    public void issueBook(int id)      { /* UPDATE SQL here */ }
}
```

**What this class is:**
- The single place where all database operations for `books` live
- Takes simple inputs (a `Book` object, an `int` ID)
- Returns simple outputs (`List<Book>`, or nothing for writes)
- `Main.java` calls these methods — it never writes SQL itself

---

## `Main.java` — The Controller

```java
BookDAO dao = new BookDAO();

// Case 1: Add
dao.addBook(new Book(title, author));   // Main doesn't know about SQL

// Case 2: View
List<Book> books = dao.getAllBooks();   // Main gets plain Java objects back
books.forEach(b -> System.out.println(b.getId() + " | " + b.getTitle() + ...));

// Case 3: Issue
dao.issueBook(id);                      // Main doesn't know about UPDATE
```

`Main.java` only knows about `Book` objects and `BookDAO` methods.
It never sees a `Connection`, a `PreparedStatement`, or a SQL string.

---

## The Flow of Data

```
User types "Clean Code" into Scanner
              │
              ▼
        Main.java
        Creates: new Book("Clean Code", "Martin")
              │
              ▼
        BookDAO.addBook(book)
        Runs: INSERT INTO books (title, author) VALUES (?, ?)
        Binds: stmt.setString(1, book.getTitle())
              │
              ▼
        MySQL stores the row
```

```
MySQL has rows in books table
              │
              ▼
        BookDAO.getAllBooks()
        Runs: SELECT * FROM books
        Reads: ResultSet → builds List<Book>
              │
              ▼
        Main.java
        Receives: List<Book>
        Prints: b.getId() + b.getTitle() + ...
              │
              ▼
        User sees the book list
```

---

## Why This Separation Matters

| Concern | Handled by | What it knows about |
|---------|-----------|---------------------|
| User interaction | `Main.java` | Scanner, menus, printing |
| Data structure | `Book.java` | Fields: id, title, author, available |
| Database access | `BookDAO.java` | SQL, Connection, ResultSet |
| DB connection | `DBConnection.java` | URL, credentials, `getConnection()` |

Each file has **one job**. If MySQL changes to PostgreSQL, only `DBConnection.java`
and `BookDAO.java` change. If you add a new field `year` to `Book`, only `Book.java`
and `BookDAO.java` change. `Main.java` is untouched.

This principle is called **Separation of Concerns** — one of the most important
ideas in software design.

---

## DAO Naming Convention

By convention, DAO classes are named after the entity they manage:
- `BookDAO` manages `books` table / `Book` model
- `StudentDAO` manages `students` table / `Student` model
- `EnrollmentDAO` manages `enrollments` table / `Enrollment` model

If you add a `Member` feature to this library system, you'd create:
- `Member.java` (model)
- `MemberDAO.java` (data access)
