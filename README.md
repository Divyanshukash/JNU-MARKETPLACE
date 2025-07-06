# JNU Marketplace - Buy & Sell Platform

A comprehensive marketplace application designed exclusively for the Jawaharlal Nehru University (JNU) community, enabling students, faculty, and staff to buy and sell items and services within the campus ecosystem.

## 🚀 Features

### Core Functionality
- **User Authentication & Profiles**: Secure registration with JNU email verification
- **Item/Service Listings**: Create, manage, and browse listings with multiple images
- **Advanced Search & Filters**: Find items by category, price range, location, and more
- **Real-time Messaging**: In-app chat system for buyer-seller communication
- **Secure Payments**: Multiple payment options (Stripe, PayPal, UPI)
- **Wishlist & Favorites**: Save items for later purchase
- **Transaction History**: Complete record of all buying/selling activities
- **Real-time Notifications**: Instant updates on messages, offers, and transactions

### Security Features
- JWT-based authentication
- JNU email domain verification
- Encrypted data transmission
- Secure payment processing
- Rate limiting and DDoS protection

### UI/UX Features
- Responsive design for all devices
- Modern, intuitive interface
- Dark/Light theme support
- Real-time availability updates
- Image gallery and zoom functionality
- Advanced filtering and sorting

## 🛠 Tech Stack

### Frontend
- **React 18** with TypeScript
- **Tailwind CSS** for styling
- **React Router** for navigation
- **React Query** for state management
- **Socket.io** for real-time features
- **Axios** for API communication
- **React Hook Form** for form handling
- **React Dropzone** for file uploads

### Backend
- **Spring Boot 3.x** with Java 17
- **Spring Security** for authentication
- **Spring Data MongoDB** for database operations
- **Spring WebSocket** for real-time messaging
- **JWT** for token-based authentication
- **Stripe API** for payment processing
- **JavaMail** for email notifications
- **Swagger/OpenAPI** for API documentation

### Database
- **MongoDB** for data storage
- **MongoDB Atlas** for cloud hosting

### DevOps & Tools
- **Maven** for dependency management
- **Docker** for containerization
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
│   ├── Dockerfile
│   └── nginx.conf
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
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── static/
│   │   └── test/           # Test files
│   ├── pom.xml
│   └── Dockerfile
├── docs/                   # Documentation
├── docker-compose.yml      # Docker configuration
├── setup.sh               # Linux/Mac setup script
├── setup.bat              # Windows setup script
└── README.md
```

## 🚀 Quick Start

### Prerequisites
- Docker and Docker Compose installed
- Git

### Option 1: Automated Setup (Recommended)

#### For Linux/Mac:
```bash
# Clone the repository
git clone <repository-url>
cd jnu-marketplace

# Make setup script executable and run it
chmod +x setup.sh
./setup.sh
```

#### For Windows:
```cmd
# Clone the repository
git clone <repository-url>
cd jnu-marketplace

# Run the setup script
setup.bat
```

### Option 2: Manual Setup

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd jnu-marketplace
   ```

2. **Create necessary directories:**
   ```bash
   mkdir -p backend/uploads nginx/ssl
   ```

3. **Configure environment variables:**
   - Copy `backend/application.properties.example` to `backend/application.properties`
   - Update the configuration with your database and API keys

4. **Build and run with Docker Compose:**
   ```bash
   docker-compose up --build -d
   ```

5. **Access the application:**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - API Documentation: http://localhost:8080/swagger-ui.html

## 🔧 Configuration

### Environment Variables

#### Backend (.env file)
```env
# Database Configuration
MONGODB_URI=mongodb://admin:password123@localhost:27017/jnu_marketplace?authSource=admin

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-here-change-in-production
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Email Configuration
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password

# Stripe Configuration
STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key
STRIPE_PUBLISHABLE_KEY=pk_test_your_stripe_publishable_key

# File Upload Configuration
UPLOAD_DIR=./uploads
MAX_FILE_SIZE=10485760

# Server Configuration
SERVER_PORT=8080
```

#### Frontend (.env file)
```env
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_STRIPE_PUBLISHABLE_KEY=pk_test_your_stripe_publishable_key
```

### JNU Email Verification
The application automatically verifies JNU email domains:
- `@jnu.ac.in`
- `@mail.jnu.ac.in`
- `@students.jnu.ac.in`

## 📚 API Documentation

Once the backend is running, access the API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs`

### Key API Endpoints

#### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/verify-email` - Email verification

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

### Integration Tests
```bash
docker-compose -f docker-compose.test.yml up --build
```

## 🚀 Deployment

### Production Deployment

1. **Update environment variables for production:**
   - Use strong JWT secrets
   - Configure production MongoDB
   - Set up production email service
   - Configure Stripe production keys

2. **Build production images:**
   ```bash
   docker-compose -f docker-compose.prod.yml up --build -d
   ```

3. **Set up reverse proxy (Nginx):**
   - Configure SSL certificates
   - Set up domain routing
   - Enable compression and caching

### Cloud Deployment

#### AWS Deployment
```bash
# Deploy to AWS ECS
aws ecs create-cluster --cluster-name jnu-marketplace
aws ecs register-task-definition --cli-input-json file://task-definition.json
aws ecs create-service --cluster jnu-marketplace --service-name marketplace-service --task-definition marketplace:1
```

#### Azure Deployment
```bash
# Deploy to Azure Container Instances
az container create --resource-group myResourceGroup --name jnu-marketplace --image marketplace:latest --dns-name-label jnu-marketplace --ports 80 443
```

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

## 🔮 Roadmap

### Phase 1 (Current)
- [x] User authentication and registration
- [x] Basic listing management
- [x] Search and filtering
- [x] User profiles
- [x] Basic messaging system

### Phase 2 (Next)
- [ ] Advanced payment integration
- [ ] Real-time notifications
- [ ] Mobile app (React Native)
- [ ] Advanced analytics dashboard
- [ ] Integration with JNU student portal

### Phase 3 (Future)
- [ ] AI-powered recommendations
- [ ] Multi-language support
- [ ] Advanced reporting features
- [ ] Social features (following, reviews)
- [ ] Marketplace analytics

## 🐛 Known Issues

- Frontend TypeScript compilation errors (due to missing node_modules)
- Some backend services need completion (messaging, notifications)
- Payment integration needs Stripe keys configuration

## 🔧 Troubleshooting

### Common Issues

1. **Docker containers not starting:**
   ```bash
   docker-compose logs
   docker system prune -a
   docker-compose up --build
   ```

2. **MongoDB connection issues:**
   - Check if MongoDB container is running
   - Verify connection string in application.properties
   - Check network connectivity

3. **Frontend not loading:**
   - Check if backend API is accessible
   - Verify REACT_APP_API_URL environment variable
   - Check browser console for errors

4. **Email not sending:**
   - Verify SMTP configuration
   - Check email credentials
   - Ensure port 587 is not blocked

### Performance Optimization

1. **Database Indexing:**
   ```javascript
   // Add indexes for better performance
   db.listings.createIndex({ "title": "text", "description": "text" })
   db.listings.createIndex({ "category": 1, "status": 1 })
   db.users.createIndex({ "email": 1 })
   ```

2. **Caching:**
   - Enable Redis for session storage
   - Implement response caching
   - Use CDN for static assets

3. **Monitoring:**
   - Set up application monitoring
   - Configure error tracking
   - Monitor database performance 