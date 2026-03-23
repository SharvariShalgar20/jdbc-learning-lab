#  JDBC Learning Docs — Library Management System

Notes built while developing the **Library Management System** — a beginner JDBC project
that covers the most fundamental concepts of connecting Java to a MySQL database.

---

##  What This Project Does

A console-based app to manage a library's book inventory:

| Feature        | Operation | SQL Used                              |
|----------------|-----------|---------------------------------------|
| Add a book     | CREATE    | `INSERT INTO books (...) VALUES (?, ?)` |
| View all books | READ      | `SELECT * FROM books`                 |
| Issue a book   | UPDATE    | `UPDATE books SET available = false`  |

One table. Three operations. Clean and focused — the perfect first JDBC project.

---

## 📁 Folder Structure

```
docs/
├── README.md                                ← You are here (index)
│
├── 01-what-is-jdbc.md                            ← JDBC overview, DriverManager, MySQL URL
│
├── 02-statement-vs-preparedstatement.md           ← When to use which, and why PreparedStatement wins
│
├── 03-resultset-and-reading-data.md             ← Iterating rows, reading columns by name
│
├── 04-crud-operations.md                   ← INSERT, SELECT, UPDATE in JDBC with real code
│
├── 05-model-and-dao-pattern.md              ← Why Book.java and BookDAO.java are separate
│
└── 06-boolean-in-sql-and-java.md            ← How TINYINT(1)/BOOLEAN maps to Java boolean
```

---

## 🗂️ Project File Map

```
com.Sharvari.libraryManagement/
├── Main.java                  → Console menu, Scanner input, calls BookDAO
├── model/
│   └── Book.java              → Plain Java class representing one book row
├── dao/
│   └── BookDAO.java           → All SQL logic: addBook, getAllBooks, issueBook
└── util/
    └── DBConnection.java      → Central place to open a MySQL connection
```

---

## 🗺️ How Concepts Connect

```
DBConnection.java  (Topic 01)
      │
      │  getConnection() → gives a Connection object
      ▼
BookDAO.java
      ├── addBook()     → PreparedStatement  (Topic 02) → INSERT  (Topic 04)
      ├── getAllBooks()  → Statement          (Topic 02) → SELECT  (Topic 04)
      │                       └── ResultSet  (Topic 03) → Book.java (Topic 05)
      │                                           └── boolean available (Topic 06)
      └── issueBook()   → PreparedStatement  (Topic 02) → UPDATE  (Topic 04)
```
