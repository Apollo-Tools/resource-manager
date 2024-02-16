@echo off
:: parse parameters
:parse
if "%~1"=="" goto endparse
if "%~1"=="-w" set website=%~2 & shift
if "%~1"=="-c" set count=%~2 & shift
shift
goto parse
:endparse

:: ping the website $count times
for /f "delims=" %%a in ('ping -n %count% %website%') do set result=%%a
echo %result% | find "Mittelwert = " > nul

:: compose output
if %errorlevel% == 0 (
    REM Extract the average round-trip time from the output
    for /f "tokens=8 delims==ms " %%a in ('echo %result%') do echo %%a
    exit /b 0
) else (
    echo Error: The website %website% is not reachable.
    exit /b 1
)
