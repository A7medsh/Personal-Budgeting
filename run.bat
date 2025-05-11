@echo off
echo Compiling and running Personal Budgeting Application...

set JAVA_HOME=C:\Program Files\Java\jdk-24
set PATH=%JAVA_HOME%\bin;D:\downlouds\apache-maven-3.9.9-bin\apache-maven-3.9.9\bin;%PATH%

echo Ensuring FXML directories exist...
mkdir "target\classes\fxml" 2>nul
mkdir "src\main\resources\fxml" 2>nul

echo Copying FXML files to target...
xcopy /Y /S "src\main\resources\fxml\*.fxml" "target\classes\fxml\" /F

echo Building and running the application...
call mvn clean compile
call mvn javafx:run

pause 
