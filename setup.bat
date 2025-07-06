@echo off
echo 🚀 Setting up JNU Marketplace Application...

REM Check if Docker is installed
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker is not installed. Please install Docker Desktop first.
    pause
    exit /b 1
)

REM Check if Docker Compose is installed
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker Compose is not installed. Please install Docker Compose first.
    pause
    exit /b 1
)

echo ✅ Docker and Docker Compose are installed

REM Create necessary directories
echo 📁 Creating necessary directories...
if not exist "backend\uploads" mkdir backend\uploads
if not exist "nginx\ssl" mkdir nginx\ssl

REM Set up environment variables
echo 🔧 Setting up environment variables...

REM Backend environment
(
echo # Database Configuration
echo MONGODB_URI=mongodb://admin:password123@localhost:27017/jnu_marketplace?authSource=admin
echo.
echo # JWT Configuration
echo JWT_SECRET=your-super-secret-jwt-key-here-change-in-production
echo JWT_EXPIRATION=86400000
echo JWT_REFRESH_EXPIRATION=604800000
echo.
echo # Email Configuration
echo EMAIL_HOST=smtp.gmail.com
echo EMAIL_PORT=587
echo EMAIL_USERNAME=your-email@gmail.com
echo EMAIL_PASSWORD=your-app-password
echo.
echo # Stripe Configuration
echo STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key
echo STRIPE_PUBLISHABLE_KEY=pk_test_your_stripe_publishable_key
echo.
echo # File Upload Configuration
echo UPLOAD_DIR=./uploads
echo MAX_FILE_SIZE=10485760
echo.
echo # Server Configuration
echo SERVER_PORT=8080
) > backend\.env

REM Frontend environment
(
echo REACT_APP_API_URL=http://localhost:8080/api
echo REACT_APP_STRIPE_PUBLISHABLE_KEY=pk_test_your_stripe_publishable_key
) > frontend\.env

echo ✅ Environment files created

REM Build and start the application
echo 🏗️ Building and starting the application...
docker-compose up --build -d

echo ⏳ Waiting for services to start...
timeout /t 30 /nobreak >nul

REM Check if services are running
echo 🔍 Checking service status...
docker-compose ps | findstr "Up" >nul
if errorlevel 1 (
    echo ❌ Some services failed to start. Check logs with: docker-compose logs
    pause
    exit /b 1
) else (
    echo ✅ All services are running!
    echo.
    echo 🌐 Application URLs:
    echo    Frontend: http://localhost:3000
    echo    Backend API: http://localhost:8080
    echo    API Documentation: http://localhost:8080/swagger-ui.html
    echo    MongoDB: localhost:27017
    echo.
    echo 📝 Next steps:
    echo    1. Open http://localhost:3000 in your browser
    echo    2. Register with your JNU email address
    echo    3. Start buying and selling!
    echo.
    echo 🛠️ Useful commands:
    echo    View logs: docker-compose logs -f
    echo    Stop services: docker-compose down
    echo    Restart services: docker-compose restart
    echo    Update application: docker-compose up --build -d
)

pause 