@echo off
REM Script para preparar despliegue en Railway
echo Preparando Messismo para Railway...

echo.
echo PASO 1: Verificando configuracion...

REM Verificar que Git está configurado
git status >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: No estas en un repositorio Git
    echo Asegurate de estar en la carpeta del proyecto
    pause
    exit /b 1
)

echo Repositorio Git OK

REM Verificar archivos Railway
if exist "backend\railway.json" (
    echo Backend railway.json configurado
) else (
    echo ERROR: Falta backend\railway.json
    pause
    exit /b 1
)

if exist "frontend\railway.json" (
    echo Frontend railway.json configurado  
) else (
    echo ERROR: Falta frontend\railway.json
    pause
    exit /b 1
)

if exist "backend\src\main\resources\application-railway.properties" (
    echo Configuracion Railway para backend OK
) else (
    echo ERROR: Falta application-railway.properties
    pause
    exit /b 1
)

echo.
echo PASO 2: Haciendo commit de cambios...

REM Add y commit todos los cambios
git add .
git status

echo.
set /p commit_message="Mensaje de commit (Enter para usar default): "
if "%commit_message%"=="" set commit_message=Configure for Railway deployment

git commit -m "%commit_message%"

if %ERRORLEVEL% NEQ 0 (
    echo No hay cambios para hacer commit o ya estan commiteados
) else (
    echo Commit realizado
)

echo.
echo PASO 3: Subiendo a GitHub...

git push origin main

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Error subiendo a GitHub
    echo Asegurate de tener permisos de push
    pause
    exit /b 1
)

echo Codigo subido a GitHub

echo.
echo LISTO PARA RAILWAY!
echo ==================

echo.
echo PROXIMOS PASOS:
echo.
echo 1. Ir a: https://railway.app
echo 2. Sign up with GitHub
echo 3. New Project - Deploy from GitHub repo
echo 4. Seleccionar: 2023-messismo
echo 5. Railway detectara automaticamente tu app
echo.
echo Variables de entorno a configurar en Railway:
echo.
echo Backend:
echo   JWT_SECRET=tu-secreto-jwt-de-32-caracteres
echo   SPRING_PROFILES_ACTIVE=railway
echo.
echo Frontend:
echo   REACT_APP_API_URL_PROD=https://tu-backend.up.railway.app
echo.
echo Una vez desplegado:
echo   - Admin: admin@mail.com / Password1  
echo   - Tu app estara en: https://tu-frontend.up.railway.app
echo.

pause