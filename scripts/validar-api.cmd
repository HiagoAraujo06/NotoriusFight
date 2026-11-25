@echo off
rem Requer a API em execucao (iniciar-api.cmd).
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0validar-api.ps1"
