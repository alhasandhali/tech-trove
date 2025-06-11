# 🛒 TechTrove - E-commerce Platform

TechTrove is a full-featured e-commerce web application developed using **Spring Boot**, **MySQL**, and **Thymeleaf**. It is designed to showcase and sell tech products like **Laptops**, **Mobiles**, and **Cameras** with a clean user experience and powerful admin panel.

---

## ✨ Features

### 🧑‍💻 Customer Features
- Browse products by category (Laptops, Mobiles, Cameras)
- Product search and filter
- Product details page
- Add to cart
- Secure registration & login
- User dashboard for order history

### 🛠️ Admin Features
- Admin login & dashboard
- CRUD operations for Products
- Manage Categories (Laptop, Mobile, Camera)
- Order management (optional)
- Image upload support

---

## 🧱 Tech Stack

| Layer       | Technology             |
|------------|------------------------|
| Backend     | Spring Boot, Spring MVC |
| Frontend    | Thymeleaf, HTML, CSS, Bootstrap |
| Database    | MySQL                  |
| ORM         | Spring Data JPA (Hibernate) |
| Security    | Spring Security        |
| Build Tool  | Maven                  |

---

## 📁 Project Structure

TechTrove/
├── src/
│ ├── main/
│ │ ├── java/com/techtrove/
│ │ │ ├── controller/
│ │ │ ├── entity/
│ │ │ ├── repository/
│ │ │ ├── service/
│ │ │ └── TechTroveApplication.java
│ │ └── resources/
│ │ ├── templates/
│ │ ├── static/
│ │ └── application.properties
├── pom.xml
└── README.md


---

## ⚙️ Installation & Run Locally

### Prerequisites
- Java 17+
- Maven
- MySQL

### Steps

```bash
# 1. Clone the repo
git clone https://github.com/your-username/TechTrove.git
cd TechTrove

# 2. Configure MySQL in src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/techtrove_db
spring.datasource.username=root
spring.datasource.password=yourpassword

# 3. Create the database manually or let Spring Boot auto-create
CREATE DATABASE techtrove_db;

# 4. Run the project
./mvnw spring-boot:run
