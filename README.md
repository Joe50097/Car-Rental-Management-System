# 🚗 Car Rental Management System

A Java-based desktop application connected to a MySQL database that simulates the operations of a car rental company.  

This project was developed as part of a university course (for the courseProgramming 3) and demonstrates **Object-Oriented Programming (OOP), JDBC database connectivity, and CRUD operations**.

---

## 🚀 Features

### 🔐 User Authentication
- User **Sign Up** and **Login**
- Account information stored securely in the database

### 👤 Account Management
- Add, update, and manage customer accounts
- Store personal details linked to rentals and payments

### 🚘 Car Management
- Add and manage cars in the system
- View available cars
- Store car details in the database

### 📅 Rental System
- Create and manage car rentals
- Track which customer rented which car
- Store rental history

### 💳 Payment Handling
- Manage customer payments
- Store payment and card details in the database

---

## 🧰 Requirements

- **Java 8 or higher**
- **MySQL / MariaDB** (or any compatible SQL database)
- MySQL Workbench (recommended)
- Any Java IDE (IntelliJ, Eclipse, VS Code)

---

## 🗄 Database Setup

The project includes SQL files to create the required database tables.

### ⚠️ Important  
The database (schema) name is expected to be:

```
project-programming-3
```

If you choose a different name, you **must update it inside `DBConnection.java`**.

### Steps:

1. Open **MySQL Workbench** (or your SQL tool)
2. Create the database:

```sql
CREATE DATABASE `project-programming-3`;
USE `project-programming-3`;
```

3. Import the provided SQL files into this database.  
These files will create all necessary tables for authentication, cars, rentals, accounts, and payments.

---

## ▶️ How to Run

1. Clone the repository

```bash
git clone https://github.com/Joe50097/Car-Rental-Management-System.git
cd Car-Rental-Management-System
```

2. Open the project in your IDE

3. Update database connection details in **`DBConnection.java`**:

```java
url = "jdbc:mysql://localhost:3306/project-programming-3";
username = "your_mysql_username";
password = "your_mysql_password";
```

4. Run the main class (such as `Home`) to start the application.

---

## 🛠️ Tech Stack
- **JavaFX** – Graphical User Interface  
- **MySQL** – Relational Database  
- **JDBC** – Database Connectivity

---

## 📁 Project Structure

```
Car-Rental-Management-System/
│
├── src/                         → Java source code
│   ├── DBConnection.java
│   ├── Login.java
│   ├── Signup.java
│   ├── Dashboard.java
│   ├── ManageCars.java
│   ├── ManageRentals.java
│   ├── ManagePayments.java
│   ├── ManageAccounts.java
│   ├── model/                   → Model classes (Car, Rental, Account, etc.)
│   └── images/                  → Application images and UI assets
│       ├── background.png
│       └── logo.png
│
├── database/
│   └── project-programming-3/   → SQL files for database schema and tables
│       ├── project-programming-3_authentication.sql
│       ├── project-programming-3_cars.sql
│       ├── project-programming-3_rentals.sql
│       ├── project-programming-3_payments.sql
│       └── project-programming-3_payment_card_details.sql
│
└── README.md                    → Project documentation
```

---

## 🧠 Concepts Demonstrated

- Object-Oriented Programming (OOP)
- Java Swing GUI
- JDBC Database Connectivity
- SQL Table Relationships
- CRUD Operations
- Authentication Systems

---

## 📜 Project License

This project is licensed under the **MIT License**.  
You are free to use, modify, and distribute this project for educational purposes.

---

## 🌟 Support the Project

If you found this project useful or interesting, please consider giving it a ⭐ (star) on GitHub! Your support helps the project grow and lets others know that it's a valuable resource.

You can star the project by clicking the **Star** button at the top of the repository page!
