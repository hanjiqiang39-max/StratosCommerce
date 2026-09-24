@echo off
setlocal
set "ROOT=%~dp0.."
cd /d "%ROOT%"

echo [1/2] Compile all modules...
call mvn -B -DskipTests compile
if errorlevel 1 (
  echo Maven compile failed.
  exit /b 1
)

echo [2/2] Start each service in a new window. Close a window to stop that service.
echo Prerequisite: Nacos 8848, Redis 6379, MySQL, RocketMQ, Seata 8091.

call :launch stratos-auth
call :launch stratos-user
call :launch stratos-product
call :launch stratos-inventory
call :launch stratos-promotion
call :launch stratos-payment
call :launch stratos-order
call :launch stratos-logistics
call :launch stratos-message
call :launch stratos-notification
call :launch stratos-system
call :launch stratos-search
timeout /t 8 /nobreak >nul
call :launch stratos-gateway

echo All start commands have been issued. Watch the new windows.
exit /b 0

:launch
start "%~1" cmd /k cd /d "%ROOT%" ^&^& mvn -pl %~1 spring-boot:run -DskipTests
exit /b 0
