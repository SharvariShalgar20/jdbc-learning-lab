-- 1. Create the Database
CREATE DATABASE IF NOT EXISTS enrollment_db;

-- 2. Select the Database (Equivalent to \c)
USE enrollment_db;

-- 3. Create Students Table
CREATE TABLE students (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          email VARCHAR(100) UNIQUE NOT NULL
);

-- 4. Create Courses Table
CREATE TABLE courses (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         title VARCHAR(100) NOT NULL,
                         credits INT NOT NULL,
                         max_capacity INT NOT NULL DEFAULT 30
);

-- 5. Create Enrollments Table (The Junction Table)
CREATE TABLE enrollments (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             student_id INT NOT NULL,
                             course_id INT NOT NULL,
                             enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
                             CONSTRAINT fk_student FOREIGN KEY (student_id)
                                 REFERENCES students(id) ON DELETE CASCADE,

                             CONSTRAINT fk_course FOREIGN KEY (course_id)
                                 REFERENCES courses(id) ON DELETE CASCADE,

    -- Prevent duplicate enrollment (Composite Unique Constraint)
                             UNIQUE(student_id, course_id)
);