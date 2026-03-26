# 02 — Foreign Keys and Relationships

> **How tables relate to each other and how the database enforces those relationships.**

---

## What is a Foreign Key?

A **Foreign Key (FK)** is a column in one table that references the **Primary Key** of another table.
It's the database's way of enforcing a relationship — you cannot insert a value in the FK column
that doesn't exist in the referenced table.

---

## The Three Tables in This Project

```
students                  courses
┌────┬──────┬───────┐     ┌────┬────────────┬─────────┬──────────────┐
│ id │ name │ email │     │ id │   title    │ credits │ max_capacity │
├────┼──────┼───────┤     ├────┼────────────┼─────────┼──────────────┤
│  1 │ Raj  │ r@..  │     │  1 │ Java Basics│    3    │      30      │
│  2 │ Priya│ p@..  │     │  2 │ SQL 101    │    2    │      20      │
└────┴──────┴───────┘     └────┴────────────┴─────────┴──────────────┘

                enrollments
                ┌────┬────────────┬───────────┐
                │ id │ student_id │ course_id │  ← Both are Foreign Keys
                ├────┼────────────┼───────────┤
                │  1 │     1      │     1     │  Raj enrolled in Java Basics
                │  2 │     2      │     1     │  Priya enrolled in Java Basics
                │  3 │     1      │     2     │  Raj enrolled in SQL 101
                └────┴────────────┴───────────┘
```

`enrollments` is called a **junction table** (or join table). Its job is to represent a
**many-to-many relationship**: one student can enroll in many courses, and one course
can have many students.

---

## The SQL That Creates These Relationships

```sql
CREATE TABLE enrollments (
    id          SERIAL PRIMARY KEY,
    student_id  INT NOT NULL,
    course_id   INT NOT NULL,

    -- Foreign Key declarations:
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES courses(id)  ON DELETE CASCADE,

    -- Prevent the same student enrolling in the same course twice:
    UNIQUE(student_id, course_id)
);
```

### What `ON DELETE CASCADE` means:
If a student is deleted from `students`, all their rows in `enrollments` are
automatically deleted too. Without this, the database would throw an error
when you tried to delete a student who has enrollments.

---

## What the FK Actually Enforces

The database will **reject** these operations automatically:

```sql
-- ❌ FAILS: student_id 99 doesn't exist in students table
INSERT INTO enrollments (student_id, course_id) VALUES (99, 1);
-- ERROR: insert or update on table "enrollments" violates foreign key constraint

-- ❌ FAILS: trying to delete a course that students are enrolled in
-- (without CASCADE)
DELETE FROM courses WHERE id = 1;
-- ERROR: update or delete on table "courses" violates foreign key constraint
```

You don't have to write any Java code for this — the database enforces it at the SQL level.

---

## JOIN Queries — Reading Related Data

Because data is spread across tables, you use `JOIN` to bring it together.

### The enrollment view query from this project:

```sql
SELECT s.name   AS student,
       c.title  AS course,
       e.enrolled_at
FROM   enrollments e
JOIN   students  s ON e.student_id = s.id
JOIN   courses   c ON e.course_id  = c.id
ORDER  BY e.enrolled_at DESC;
```

**How to read it:**
- Start from `enrollments` (the junction table)
- For each row, look up the matching student via `student_id → students.id`
- For each row, look up the matching course via `course_id → courses.id`
- Return `name`, `title`, and `enrolled_at` as a combined result

---

## Types of Relationships

| Relationship   | Example in this project                  | How it's modeled            |
|----------------|------------------------------------------|-----------------------------|
| One-to-Many    | One student has many enrollments         | FK in the "many" table      |
| Many-to-Many   | Students ↔ Courses                       | Junction table (enrollments)|

---

## Reading FK Results in Java

```java
ResultSet rs = st.executeQuery(
    "SELECT s.name, c.title FROM enrollments e " +
    "JOIN students s ON e.student_id = s.id " +
    "JOIN courses c ON e.course_id = c.id"
);

while (rs.next()) {
    String student = rs.getString("name");  // from students table
    String course  = rs.getString("title"); // from courses table
    System.out.println(student + " → " + course);
}
```

Column aliases (`AS student`, `AS course`) help when two tables have columns with
the same name. Use `rs.getString("alias_name")` to retrieve them.

---

## The `UNIQUE` Constraint

```sql
UNIQUE(student_id, course_id)
```

This is a **composite unique constraint** — the *combination* of both columns must be
unique. So the same student cannot enroll in the same course twice. If they try,
the database throws a `SQLException` with a unique constraint violation message,
which our `rollback()` in the enrollment transaction catches.

---

## Summary

| Concept                  | Purpose                                              |
|--------------------------|------------------------------------------------------|
| Foreign Key              | Links two tables; database enforces referential integrity |
| Junction Table           | Models many-to-many relationships                   |
| `ON DELETE CASCADE`      | Auto-cleans child rows when parent is deleted        |
| `UNIQUE(col1, col2)`     | Prevents duplicate combinations                     |
| `JOIN`                   | Combines data from related tables in a query        |
