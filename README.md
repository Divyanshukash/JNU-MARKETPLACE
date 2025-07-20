# Marketplace - Buy & Sell Platform

A comprehensive marketplace application designed for campus and local communities, enabling students, faculty, and residents to buy and sell items and services within their ecosystem.

## 🚀 Features

### Core Functionality
- **User Authentication & Profiles**: Secure registration and login
- **Item/Service Listings**: Create, manage, and browse listings with multiple images
- **Advanced Search & Filters**: Find items by category, price range, location, and more
- **Messaging System**: In-app chat system for buyer-seller communication
- **Wishlist & Favorites**: Save items for later purchase

### Security Features
- JWT-based authentication
- Encrypted data transmission

### UI/UX Features
- Responsive design for all devices
- Modern, intuitive interface
- **Dark/Light theme toggle**
- Image gallery and zoom functionality
- Advanced filtering and sorting

## 🛠 Tech Stack

### Frontend
- **React 18** with TypeScript
- **Tailwind CSS** for styling
- **React Router** for navigation
- **React Query** for state management
- **Axios** for API communication
- **React Hook Form** for form handling


### Backend
- **Spring Boot 3.x** with Java 17
- **Spring Security** for authentication
- **Spring Data MongoDB** for database operations
- **JWT** for token-based authentication
- **JavaMail** for email notifications
- **Swagger/OpenAPI** for API documentation

### Database
- **MongoDB** for data storage
- **MongoDB Atlas** for cloud hosting (optional)

### DevOps & Tools
- **Maven** for dependency management
- **Git** for version control
- **Postman** for API testing

## 📁 Project Structure

```
jnu-marketplace/
├── frontend/                 # React frontend application
│   ├── public/
│   ├── src/
│   │   ├── components/      # Reusable UI components
│   │   ├── pages/          # Page components
│   │   ├── hooks/          # Custom React hooks
│   │   ├── services/       # API services
│   │   ├── utils/          # Utility functions
│   │   ├── types/          # TypeScript type definitions
│   │   ├── contexts/       # React contexts
│   │   └── styles/         # CSS and styling
│   ├── package.json
│   ├── tsconfig.json
│   ├── tailwind.config.js
│   ├── postcss.config.js
├── backend/                 # Spring Boot backend application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/jnu/marketplace/
│   │   │   │       ├── config/        # Configuration classes
│   │   │   │       ├── controller/    # REST controllers
│   │   │   │       ├── dto/           # Data Transfer Objects
│   │   │   │       ├── model/         # Entity models
│   │   │   │       ├── repository/    # Data access layer
│   │   │   │       ├── service/       # Business logic
│   │   │   │       ├── security/      # Security configuration
│   │   │   │       └── util/          # Utility classes
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   └── pom.xml
├── docs/                   # Documentation
├── LOCAL-SETUP.md          # Local setup instructions
├── setup.bat               # Windows setup script (local only)
└── README.md
```

## 🚀 Quick Start

### Prerequisites
- **Java 17** - Download from https://adoptium.net/temurin/releases/
- **Node.js 18+** - Download from https://nodejs.org/
- **MongoDB** - Download from https://www.mongodb.com/try/download/community
- **Git** - Download from https://git-scm.com/

### Local Setup (Recommended)

#### For Windows (Local Development):
```cmd
# Clone the repository
git clone <repository-url>
cd jnu-marketplace

# Run the local setup script
run-local.bat
```

#### Access the application:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- API Documentation: http://localhost:8080/swagger-ui.html

## 🔧 Configuration

### Environment Variables

#### Backend (.env file)
```env
# Database Configuration
MONGODB_URI=mongodb://localhost:27017/jnu_marketplace

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-here-change-in-production
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Email Configuration
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password



# File Upload Configuration
UPLOAD_DIR=./uploads
MAX_FILE_SIZE=10485760

# Server Configuration
SERVER_PORT=8080
```

#### Frontend (.env file)
```env
REACT_APP_API_URL=http://localhost:8080/api

```

## 📚 API Documentation

Once the backend is running, access the API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs`

### Key API Endpoints

#### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login


#### Users
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `GET /api/users/wishlist` - Get user wishlist

#### Listings
- `GET /api/listings` - Get all listings
- `POST /api/listings` - Create new listing
- `GET /api/listings/{id}` - Get listing by ID
- `PUT /api/listings/{id}` - Update listing
- `DELETE /api/listings/{id}` - Delete listing
- `POST /api/listings/search` - Search listings

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 🚀 Deployment

For deployment, you can use any cloud VM or server. Install Java, Node.js, and MongoDB on your server, then follow the manual setup steps above. Make sure to:
- Use strong secrets and production database credentials
- Set up a reverse proxy (e.g., Nginx) for SSL and domain routing
- Build the frontend with `npm run build` and serve it with a static server or Nginx
- Run the backend with `java -jar target/marketplace-1.0.0.jar` (after building with Maven)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow the existing code style
- Add tests for new features
- Update documentation
- Ensure all tests pass

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue on GitHub
- Contact: support@jnu-marketplace.com
- Documentation: [Wiki](https://github.com/jnu-marketplace/docs) 