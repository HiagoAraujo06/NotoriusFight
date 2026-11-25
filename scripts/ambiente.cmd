@echo off
rem Prepara Java, Git e PostgreSQL apenas para a janela atual.
set "DEVTOOLS=%USERPROFILE%\DevTools"
set "JAVA_HOME=%DEVTOOLS%\jdk-21"
set "PATH=%JAVA_HOME%\bin;%DEVTOOLS%\git\cmd;%DEVTOOLS%\pgsql\bin;%PATH%"
