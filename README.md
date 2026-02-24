# Library-Management-System
A secure, enterprise-ready Library Management System built with Java Swing and MySQL. Features Role-Based Access Control (RBAC), SHA-256 password hashing, automated 15-day return logic, and a self-initializing database.

This project was developed to demonstrate enterprise-level backend architecture, secure database management, and implementation of complex business logic within a Java Swing environment. 

## ✨ Key Features

### 🔐 Security & Database Integrity
* **Role-Based Access Control (RBAC):** Distinct dashboards and permissions for `Admin` and `User` roles. Users cannot access inventory maintenance functions.
* **Cryptographic Hashing:** Passwords are never stored in plain text. All credentials are encrypted using **SHA-256**.
* **SQL Injection Prevention:** 100% implementation of `PreparedStatement` for all database queries.
* **ACID Transactions:** Complex operations (like issuing a book) use `setAutoCommit(false)` and `conn.commit()` to ensure data integrity during multi-table updates.
* **Soft Deletion:** Books are not hard-deleted from the database to preserve transaction history; instead, they are updated to a `Retired` status.

### 💼 Business Logic Implementation
* **Automated 15-Day Return Policy:** The system automatically calculates the due date 15 days from the issue date.
* **Dynamic Fine Calculation:** Upon returning a book, the system calculates late fines ($10/day) based on `ChronoUnit.DAYS.between` logic.
* **Inventory Tracking:** The system creates unique database records for individual copies of the same book to accurately track circulation.

### 🚀 Application-Managed Setup
* **Auto-Initialization:** The application features a self-building database. On the first run, it automatically checks for the existence of tables and injects default tables and root users, making deployment seamless.
* **Modernized UI:** Utilizes the `Nimbus` Look-and-Feel with customized padding, modern typography (Segoe UI), and color-coded UX elements.

---

## 🛠️ Tech Stack
* **Language:** Java (JDK 11+)
* **GUI Framework:** Java Swing / AWT
* **Database:** MySQL
* **Driver:** MySQL Connector/J

---

## ⚙️ Getting Started

### Prerequisites
1. Ensure **Java (JDK)** is installed.
2. Ensure **MySQL Server** is installed and running locally on port `3306`.
3. Download the [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) `.jar` file and add it to your project's build path.

### Installation & Setup
1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/Library-Management-System.git](https://github.com/yourusername/Library-Management-System.git)
