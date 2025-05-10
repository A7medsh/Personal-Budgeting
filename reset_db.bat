@echo off
echo Resetting the database...

if exist budget.db (
    echo Deleting existing database...
    del budget.db
    echo Database deleted successfully.
) else (
    echo No existing database found.
)

echo The database will be recreated when you run the application again.
echo.
echo Press any key to exit...
pause 