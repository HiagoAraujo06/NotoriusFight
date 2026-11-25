@echo off
call "%~dp0ambiente.cmd"
pg_isready -h localhost -q && echo Banco ja esta em execucao. && exit /b 0
pg_ctl -D "%DEVTOOLS%\pgsql-data" -l "%DEVTOOLS%\pgsql-data\servidor.log" -o "-c listen_addresses=localhost" -w start
