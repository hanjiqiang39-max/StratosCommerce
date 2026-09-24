@echo off
setlocal
echo Stopping listeners on StratosCommerce service ports...
for %%P in (8080 8081 8082 8083 8084 8085 8086 8087 8088 8089 8090 8092 8093) do (
  for /f "tokens=5" %%I in ('netstat -ano ^| findstr /R /C:":%%P .*LISTENING"') do (
    echo kill PID %%I port %%P
    taskkill /F /PID %%I >nul 2>&1
  )
)
echo Done.
exit /b 0
