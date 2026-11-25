@echo off
call "%~dp0ambiente.cmd"
pg_ctl -D "%DEVTOOLS%\pgsql-data" -m fast stop
