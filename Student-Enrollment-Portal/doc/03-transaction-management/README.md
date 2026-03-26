# 03 — Transaction Management

> **The most important concept in this project. How to make multiple SQL operations succeed or fail together.**

---

## What is a Transaction?

A **transaction** is a group of SQL operations treated as a single unit of work.
Either **all of them succeed**, or **none of them do**.

This is captured by the acronym **ACID**:

| Letter | Property       | Meaning                                                        |
|--------|----------------|----------------------------------------------------------------|
| A      | Atomicity      | All operations happen, or none do. No partial states.          |
| C      | Consistency    | The database moves from one valid state to another.            |
| I      | Isolation      | Concurrent transactions don't interfere with each other.       |
| D      | Durability     | Once committed, the data survives crashes.                     |

---

## The Problem This Solves

Imagine enrolling a student involves two steps:

```
Step 1: Check if course has capacity
Step 2: Insert into enrollments table
```

What if Step 1 passes (course has space), but Step 2 fails (DB error, duplicate key, etc.)?
Without transactions, Step 1 already happened but Step 2 didn't. The system is in an
**inconsistent state** — the course shows 1 empty seat, but no enrollment was recorded.

Transactions prevent this by grouping both steps: if Step 2 fails, the database
**rolls back** everything as if Step 1 never ran.

---

## The Three Key Methods

| Method                         | What it does                                               |
|--------------------------------|------------------------------------------------------------|
| `conn.setAutoCommit(false)`    | Disables auto-commit; starts a manual transaction          |
| `conn.commit()`                | Saves all changes made since the last commit               |
| `conn.rollback()`              | Undoes all changes made since the last commit              |

---

## Default Behavior: AutoCommit ON

By default, JDBC runs every SQL statement in its own automatic transaction:

```java
// AutoCommit is ON by default
conn.executeUpdate("INSERT INTO students ...");  // ← auto-committed immediately
conn.executeUpdate("INSERT INTO enrollments ..."); // ← auto-committed immediately
```

Each statement commits the moment it runs. There's no way to undo the first if
the second fails.

---

## How to Use Transactions Manually

### The Pattern (used in `EnrollmentDAO.java`):

```java
Connection conn = null;
try {
    conn = DBConnection.getConnection();
    conn.setAutoCommit(false);      // 🔴 START: disable auto-commit

    // --- All your SQL operations go here ---
    // Step 1
    PreparedStatement ps1 = conn.prepareStatement("SELECT ...");
    ResultSet rs = ps1.executeQuery();
    // ... check capacity ...

    // Step 2
    PreparedStatement ps2 = conn.prepareStatement("INSERT INTO enrollments ...");
    ps2.executeUpdate();
    // --- End of SQL operations ---

    conn.commit();                  // ✅ SUCCESS: save everything
    System.out.println("Enrolled!");

} catch (SQLException e) {
    if (conn != null) {
        conn.rollback();            // ❌ FAILURE: undo everything
        System.out.println("Rolled back: " + e.getMessage());
    }
} finally {
    if (conn != null) {
        conn.setAutoCommit(true);   // 🔄 RESET: always restore default
        conn.close();
    }
}
```

---

## Visualizing commit vs rollback

### Happy Path (commit):
```
setAutoCommit(false)
        │
        ▼
  [Check capacity]  ──── passes ───►  [Insert enrollment]  ──── success ───►  commit()
                                                                                  │
                                                                   Changes saved to DB ✅
```

### Failure Path (rollback):
```
setAutoCommit(false)
        │
        ▼
  [Check capacity]  ──── passes ───►  [Insert enrollment]  ──── FAILS ────►  rollback()
                                              │                                    │
                                         (throws SQLException)          All changes undone ❌
                                                                         DB unchanged ✅
```

---

## Savepoints (Advanced — Good to Know)

If you have 5 steps and only want to roll back to step 3 (not the beginning):

```java
conn.setAutoCommit(false);

// Step 1, 2
Savepoint sp = conn.setSavepoint("after_step2");

try {
    // Step 3, 4, 5
    conn.commit();
} catch (SQLException e) {
    conn.rollback(sp);  // Only rolls back to step 2, not the very beginning
}
```

This project doesn't use savepoints (2 steps is simple enough), but it's the
natural next level.

---

## What Happens if You Forget to Call commit()?

If you call `setAutoCommit(false)` and then close the connection without calling
`commit()`, the changes are **automatically rolled back**. This is a safety net —
uncommitted work is never accidentally saved.

---

## Why `setAutoCommit(true)` in `finally`?

This project opens a new `Connection` for each operation (no connection pool).
Even so, resetting `autoCommit` is good practice — it's the polite, correct way
to return a connection to its expected default state. If you ever add a connection
pool later, skipping this reset will cause bugs in subsequent operations that
reuse the same connection object.

---

## The Enrollment Transaction in Plain English

```java
conn.setAutoCommit(false);

// "Lock in" the following two operations as one unit:

// 1. Read current capacity — if full, throw an exception
// 2. Insert the enrollment record

// If both succeeded:
conn.commit();     // "Yes, this really happened"

// If anything went wrong:
conn.rollback();   // "Pretend none of this happened"
```

---

## Key Takeaways

- AutoCommit ON (default) = every statement is its own transaction
- AutoCommit OFF = you control when the transaction ends
- Always `rollback()` in `catch`, always `setAutoCommit(true)` + `close()` in `finally`
- Transactions are the only way to guarantee **atomicity** across multiple SQL statements
