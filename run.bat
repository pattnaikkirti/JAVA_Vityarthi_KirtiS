@echo off
cd /d "%~dp0"
javac -encoding UTF-8 -cp "lib\sqlite-jdbc-3.46.1.3.jar" -d out src/com/expensetracker/model/*.java src/com/expensetracker/exception/*.java src/com/expensetracker/util/*.java src/com/expensetracker/dao/*.java src/com/expensetracker/service/*.java src/com/expensetracker/main/*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)
java -cp "out;lib\sqlite-jdbc-3.46.1.3.jar" com.expensetracker.main.Main
pause
