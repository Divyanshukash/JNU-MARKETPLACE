@echo off
echo 🚀 Starting JNU Marketplace (Local Mode)
echo.

echo 📋 Checking prerequisites...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java is not installed. Please install Java 17 from https://adoptium.net/
    pause
    exit /b 1
) else (
    echo ✅ Java is installed
)

REM Check if Node.js is installed
node --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Node.js is not installed. Please install Node.js from https://nodejs.org/
    pause
    exit /b 1
) else (
    echo ✅ Node.js is installed
)

REM Check if MongoDB is running
echo 🔍 Checking if MongoDB is running...
netstat -an | findstr :27017 >nul
if errorlevel 1 (
    echo ⚠️  MongoDB is not running on port 27017
    echo 📝 Please start MongoDB manually or install it from https://www.mongodb.com/try/download/community
    echo.
    echo 💡 To start MongoDB manually:
    echo    1. Open MongoDB Compass or
    echo    2. Run: "C:\Program Files\MongoDB\Server\6.0\bin\mongod.exe"
    echo.
    pause
) else (
    echo ✅ MongoDB is running
)

echo.
echo 🏗️ Building backend...
cd backend
call mvn clean install -DskipTests
if errorlevel 1 (
    echo ❌ Backend build failed
    pause
    exit /b 1
)

echo.
echo 🚀 Starting backend server...
start "Backend Server" cmd /k "mvn spring-boot:run"

echo.
echo ⏳ Waiting for backend to start...
timeout /t 10 /nobreak >nul

echo.
echo 🏗️ Installing frontend dependencies...
cd ..\frontend
call npm install
if errorlevel 1 (
    echo ❌ Frontend dependencies installation failed
    pause
    exit /b 1
)

echo.
echo 🚀 Starting frontend server...
start "Frontend Server" cmd /k "npm start"

echo.
echo ✅ JNU Marketplace is starting up!
echo.
echo 🌐 Application URLs:
echo    Frontend: http://localhost:3000
echo    Backend API: http://localhost:8080/api
echo    API Documentation: http://localhost:8080/api/swagger-ui.html
echo.
echo 📝 Next steps:
echo    1. Wait for both servers to fully start
echo    2. Open http://localhost:3000 in your browser
echo    3. Register with your JNU email address
echo    4. Start buying and selling!
echo.
echo 🛑 To stop the servers, close the command windows
pause 