# 06 — CRUD with JDBC

> **The four fundamental database operations — Create, Read, Update, Delete — and how each maps to JDBC code.**

---

## What is CRUD?

| Letter | Operation | SQL Command | JDBC Method          |
|--------|-----------|-------------|----------------------|
| C      | Create    | `INSERT`    | `executeUpdate()`    |
| R      | Read      | `SELECT`    | `executeQuery()`     |
| U      | Update    | `UPDATE`    | `executeUpdate()`    |
| D      | Delete    | `DELETE`    | `executeUpdate()`    |

> **Rule:** `executeQuery()` is for SELECT (returns rows). `executeUpdate()` is for INSERT/UPDATE/DELETE (returns a count of affected rows).

---

## C — CREATE (`INSERT`)

### SQL:
```sql
INSERT INTO students (name, email) VALUES (?, ?);
```

### Java:
```java
public void addStudent(String name, String email) throws SQLException {
    String sql = "INSERT INTO students (name, email) VALUES (?, ?)";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, name);
        ps.setString(2, email);

        int rowsInserted = ps.executeUpdate();
        // rowsInserted will be 1 if successful
        System.out.println(rowsInserted + " row(s) inserted.");
    }
}
```

### Retrieving the generated ID (SERIAL/AUTO_INCREMENT):
```java
PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
ps.setString(1, name);
ps.setString(2, email);
ps.executeUpdate();

ResultSet generatedKeys = ps.getGeneratedKeys();
if (generatedKeys.next()) {
    int newId = generatedKeys.getInt(1);
    System.out.println("New student ID: " + newId);
}
```

---

## R — READ (`SELECT`)

### Read all rows:
```sql
SELECT * FROM students;
```

```java
public List<Student> getAllStudents() throws SQLException {
    List<Student> list = new ArrayList<>();

    try (Connection conn = DBConnection.getConnection();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery("SELECT * FROM students")) {

        while (rs.next()) {    // ← iterate each row
            int    id    = rs.getInt("id");
            String name  = rs.getString("name");
            String email = rs.getString("email");
            list.add(new Student(id, name, email));
        }
    }
    return list;
}
```

### Read one row by ID:
```java
public Student getStudentById(int id) throws SQLException {
    String sql = "SELECT * FROM students WHERE id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {    // ← if() for single row, while() for multiple
            return new Student(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email")
            );
        }
    }
    return null; // not found
}
```

### ResultSet navigation:

```
Before calling rs.next():  cursor is BEFORE the first row
After rs.next() = true:    cursor is ON a row (you can read it)
After rs.next() = false:   cursor is AFTER the last row (no more rows)

Initial position
      │
      ▼
  [before row 1]
      │  rs.next() → true
      ▼
  [  Row 1     ]  ← read here
      │  rs.next() → true
      ▼
  [  Row 2     ]  ← read here
      │  rs.next() → false
      ▼
  [after last ]
```

---

## U — UPDATE

### SQL:
```sql
UPDATE students SET name = ?, email = ? WHERE id = ?;
```

### Java:
```java
public void updateStudent(int id, String newName, String newEmail) throws SQLException {
    String sql = "UPDATE students SET name = ?, email = ? WHERE id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, newName);
        ps.setString(2, newEmail);
        ps.setInt(3, id);           // WHERE clause parameter last

        int rowsUpdated = ps.executeUpdate();

        if (rowsUpdated > 0) {
            System.out.println("✅ Updated successfully.");
        } else {
            System.out.println("❌ No student found with ID: " + id);
        }
    }
}
```

**Always check `rowsUpdated`** — if 0 rows were updated, it usually means the
`WHERE id = ?` found no match. This is how you detect "record not found."

---

## D — DELETE

### SQL:
```sql
DELETE FROM students WHERE id = ?;
```

### Java:
```java
public void deleteStudent(int id) throws SQLException {
    String sql = "DELETE FROM students WHERE id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, id);

        int rowsDeleted = ps.executeUpdate();

        if (rowsDeleted > 0) {
            System.out.println("✅ Student deleted.");
        } else {
            System.out.println("❌ Student not found.");
        }
    }
}
```

> ⚠️ **Foreign Key Cascade:** In this project, deleting a student also deletes
> all their enrollment records automatically, because the FK was defined with
> `ON DELETE CASCADE`. Without cascade, deleting a student with enrollments would
> throw a FK violation error.

---

## Checking If a Record Exists

Sometimes you want to check before inserting or deleting:

```java
public boolean studentExists(int id) throws SQLException {
    String sql = "SELECT 1 FROM students WHERE id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        return rs.next(); // true if any row exists, false if not
    }
}
```

`SELECT 1` is a common pattern — it's faster than `SELECT *` when you only
need to know if a row exists, not what's in it.

---

## Reading Different Column Types

```java
rs.getInt("id")                          // INT, SERIAL
rs.getString("name")                     // VARCHAR, TEXT
rs.getDouble("price")                    // DOUBLE, NUMERIC
rs.getBoolean("is_active")              // BOOLEAN
rs.getTimestamp("enrolled_at")          // TIMESTAMP
rs.getDate("birth_date")                // DATE
rs.getLong("big_number")                // BIGINT

// Check for NULL before reading:
if (rs.getObject("optional_field") != null) {
    String val = rs.getString("optional_field");
}
```

---

## CRUD Summary Table

| Operation | SQL         | JDBC Method       | Returns               | Check for success by     |
|-----------|-------------|-------------------|-----------------------|--------------------------|
| Create    | `INSERT`    | `executeUpdate()` | `int` (rows inserted) | value == 1               |
| Read all  | `SELECT`    | `executeQuery()`  | `ResultSet`           | `while (rs.next())`      |
| Read one  | `SELECT`    | `executeQuery()`  | `ResultSet`           | `if (rs.next())`         |
| Update    | `UPDATE`    | `executeUpdate()` | `int` (rows changed)  | value > 0                |
| Delete    | `DELETE`    | `executeUpdate()` | `int` (rows deleted)  | value > 0                |
