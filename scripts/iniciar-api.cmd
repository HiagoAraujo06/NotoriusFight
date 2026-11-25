@echo off
call "%~dp0ambiente.cmd"
call "%~dp0iniciar-banco.cmd" || exit /b 1
cd /d "%~dp0.."
call mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
