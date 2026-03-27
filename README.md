# JDBC Learning Lab

Welcome to the **JDBC Learning Lab**! This repository is a collection of Java applications designed to demonstrate **Java Database Connectivity (JDBC)** concepts. It serves as a practical resource for learning how to interact with relational databases using Java, covering everything from basic CRUD operations to project-based implementations.

## 📂 Project Structure

The repository is organized into distinct modules, each focusing on a specific application of JDBC:

* **[Student-Enrollment-Portal](https://github.com/SharvariShalgar20/jdbc-learning-lab/tree/main/Student-Enrollment-Portal)**: A comprehensive system for managing student data, course registrations, and enrollment logic.
* **[Libarary-management-jdbc](https://github.com/SharvariShalgar20/jdbc-learning-lab/tree/main/Libarary-management-jdbc)**: A backend-focused project to handle book inventories, member management, and transaction history.
* **Database Configs**: Includes `.gitignore` settings to keep environment-specific configurations (like IDE files) out of the version history.

## 🛠️ Tech Stack

* **Language**: Java
* **Database**: MySQL 
* **API**: JDBC (Java Database Connectivity)
* **IDE**: IntelliJ IDEA (Configuration files included in `.idea`)

## 🚀 Getting Started

### Prerequisites
* **JDK 8** or higher installed.
* A relational database (e.g., MySQL) running locally or on a server.
* The appropriate **JDBC Driver** (e.g., Connector/J for MySQL) added to your project dependencies.

### Setup
1.  **Clone the repository**:
    ```bash
    git clone [https://github.com/SharvariShalgar20/jdbc-learning-lab.git](https://github.com/SharvariShalgar20/jdbc-learning-lab.git)
    ```
2.  **Database Configuration**:
    * Locate the connection logic in the respective project folders.
    * Update the database URL, username, and password to match your local environment.
3.  **Run the Application**:
    * Open the project in your preferred IDE.
    * Compile and run the main driver classes (e.g., `Main.java` or specific Controller classes).

## 📑 Features Covered
* Establishing database connections.
* Executing **Static Queries** (Statement) and **Parameterized Queries** (PreparedStatement).
* Handling **ResultSet** for data retrieval.
* Transaction management and Batch processing.
* Proper Exception Handling for SQL errors.

## 🤝 Contributing
Contributions are welcome! If you have a new JDBC-based project or an optimization for the existing ones, feel free to fork the repo and submit a pull request.
