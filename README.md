````markdown
# 🛒 JNU Marketplace

A full-stack marketplace web application developed for the Jawaharlal Nehru University (JNU) community, enabling students, faculty, and staff to securely buy and sell products within the campus ecosystem.

---

## 🚀 Live Demo

**Frontend:** https://jnu-marketplace.vercel.app

**Backend API:** https://jnu-marketplace-final.onrender.com

---

## 📌 Overview

JNU Marketplace provides a secure and user-friendly platform for buying and selling items inside the university community. Users can create listings, communicate with buyers and sellers, manage sale requests, and securely authenticate using JWT.

The project follows a modern client-server architecture built with **React**, **Spring Boot**, and **MongoDB Atlas**.

---

## ✨ Features

### Authentication & Security

- JWT-based Authentication
- Secure User Registration & Login
- Password Encryption using Spring Security
- Protected API Endpoints
- Role-based Access Control

### Marketplace

- Create, Update and Delete Listings
- Upload Multiple Product Images
- Browse Products by Category
- Advanced Search and Filtering
- Wishlist Management
- Product Detail Pages

### Communication

- Buyer-Seller Messaging
- Sale Request Management
- Email Notifications

### User Experience

- Responsive Design
- Light & Dark Theme
- Image Gallery
- Pagination
- Modern Dashboard

---

## 🛠 Tech Stack

| Category | Technologies |
|-----------|--------------|
| Frontend | React 18, TypeScript, Tailwind CSS, React Router, React Query, Axios |
| Backend | Spring Boot 3, Java 17 |
| Security | Spring Security, JWT |
| Database | MongoDB Atlas |
| File Storage | Local File Uploads |
| Documentation | Swagger/OpenAPI |
| Build Tools | Maven, npm |
| Version Control | Git & GitHub |

---

## 🏗 Architecture

```text
                 React + TypeScript
                        │
                 REST API (Axios)
                        │
              Spring Boot Backend
      ┌─────────────┼─────────────┐
      │             │             │
 Authentication  Listings   Messaging
      │             │             │
      └─────────────┼─────────────┘
                    │
              MongoDB Atlas
````

---

## 📁 Project Structure

```text
JNU-MARKETPLACE
│
├── backend
│   ├── src
│   ├── uploads
│   └── pom.xml
│
├── frontend
│   ├── public
│   ├── src
│   └── package.json
│
├── LOCAL-SETUP.md
└── README.md
```

---

## ⚙️ Getting Started

### Prerequisites

* Java 17+
* Node.js 18+
* MongoDB Atlas (or Local MongoDB)
* Maven
* Git

### Clone Repository

```bash
git clone https://github.com/Divyanshukash/JNU-MARKETPLACE.git
cd JNU-MARKETPLACE
```

---

### Backend Setup

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs at:

```
http://localhost:8080
```

---

### Frontend Setup

```bash
cd frontend
npm install
npm start
```

Frontend runs at:

```
http://localhost:3000
```

---

## 🔧 Environment Variables

### Backend (`application.properties`)

```properties
spring.data.mongodb.uri=YOUR_MONGODB_URI

jwt.secret=YOUR_SECRET

spring.mail.username=YOUR_EMAIL
spring.mail.password=YOUR_APP_PASSWORD
```

### Frontend (`.env`)

```properties
REACT_APP_API_URL=http://localhost:8080/api
```

---

## 📚 REST API

| Method | Endpoint             | Description    |
| ------ | -------------------- | -------------- |
| POST   | `/api/auth/register` | Register User  |
| POST   | `/api/auth/login`    | Login          |
| GET    | `/api/listings`      | Get Listings   |
| POST   | `/api/listings`      | Create Listing |
| PUT    | `/api/listings/{id}` | Update Listing |
| DELETE | `/api/listings/{id}` | Delete Listing |
| GET    | `/api/users/profile` | User Profile   |

Swagger Documentation:

```
http://localhost:8080/swagger-ui.html
```

---

## 📷 Screenshots

> Add screenshots here after deployment.

```
assets/
├── home.png
├── listings.png
├── details.png
├── dashboard.png
└── chat.png
```

---

## 🚀 Deployment

| Service  | Platform      |
| -------- | ------------- |
| Frontend | Vercel        |
| Backend  | Render        |
| Database | MongoDB Atlas |

---

## 🔒 Security

* JWT Authentication
* BCrypt Password Hashing
* Protected REST APIs
* Secure File Uploads
* Input Validation
* Spring Security

---

## 🧪 Testing

Backend

```bash
cd backend
mvn test
```

Frontend

```bash
cd frontend
npm test
```

---

## 🎯 Future Enhancements

* Payment Gateway Integration
* Product Reviews & Ratings
* Real-time Chat using WebSockets
* Push Notifications
* Admin Dashboard
* AI-powered Product Recommendations
* Mobile Application

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch

```bash
git checkout -b feature/new-feature
```

3. Commit your changes

```bash
git commit -m "Add new feature"
```

4. Push your branch

```bash
git push origin feature/new-feature
```

5. Open a Pull Request

---

## 👨‍💻 Author

**Divyanshu Kashyap**

GitHub: https://github.com/Divyanshukash

---

## 📄 License

This project is licensed under the MIT License.

```
```
