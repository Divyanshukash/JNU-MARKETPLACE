@echo off
echo 🛑 Stopping JNU Marketplace servers...
echo.

echo 🔍 Finding Java processes (backend)...
tasklist /FI "IMAGENAME eq java.exe" 2>NUL | find /I /N "java.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo 🚫 Stopping backend server...
    taskkill /F /IM java.exe
    echo ✅ Backend stopped
) else (
    echo ℹ️  No backend server running
)

echo.
echo 🔍 Finding Node.js processes (frontend)...
tasklist /FI "IMAGENAME eq node.exe" 2>NUL | find /I /N "node.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo 🚫 Stopping frontend server...
    taskkill /F /IM node.exe
    echo ✅ Frontend stopped
) else (
    echo ℹ️  No frontend server running
)

echo.
echo ✅ All servers stopped!
pause 