# 🚀 JNU Marketplace - Local Setup

## 📋 Prerequisites

Before running the project, install these tools:

### 1. **Java 17**
- Download from: https://adoptium.net/temurin/releases/
- Choose "Windows x64 MSI Installer"
- Install and restart your computer

### 2. **Node.js 18+**
- Download from: https://nodejs.org/
- Choose "LTS" version
- Install and restart your computer

### 3. **MongoDB**
- Download from: https://www.mongodb.com/try/download/community
- Choose "Windows x64" version
- Install MongoDB Compass (GUI) when prompted

### 4. **Git**
- Download from: https://git-scm.com/
- Install with default settings

## 🚀 Quick Start

### **Step 1: Start MongoDB**
1. Open MongoDB Compass
2. Click "Connect" (it will connect to localhost:27017)
3. Keep MongoDB Compass running

### **Step 2: Run the Project**
```cmd
# Navigate to your project folder
cd "JNU WEB APP"

# Run the local setup script
run-local.bat
```

### **Step 3: Access the Application**
- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080/api
- **API Docs:** http://localhost:8080/api/swagger-ui.html

## 🛑 Stop the Application
```cmd
# Stop all servers
stop-local.bat
```

## 🔧 Manual Setup (if script fails)

### **Backend Only:**
```cmd
cd backend
mvn clean install
mvn spring-boot:run
```

### **Frontend Only:**
```cmd
cd frontend
npm install
npm start
```

## ❓ Troubleshooting

### **Java not found:**
- Install Java 17 from https://adoptium.net/
- Restart your computer
- Open new command prompt

### **Node.js not found:**
- Install Node.js from https://nodejs.org/
- Restart your computer
- Open new command prompt

### **MongoDB connection failed:**
- Start MongoDB Compass
- Make sure it's connected to localhost:27017
- Or start MongoDB service manually

### **Port already in use:**
- Close other applications using ports 3000 or 8080
- Or change ports in application.properties

## 📞 Support

If you encounter issues:
1. Check that all prerequisites are installed
2. Make sure MongoDB is running
3. Try running backend and frontend separately
4. Check the console output for error messages 