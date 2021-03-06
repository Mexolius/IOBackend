@echo off

@rem Find java installation
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
"%JAVA_EXE%" -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

@echo .
@echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
@echo .
@echo Please set the JAVA_HOME variable in your environment to match the
@echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist %JAVA_EXE% goto execute

@echo.
@echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
@echo.
@echo Please set the JAVA_HOME variable in your environment to match the
@echo location of your Java installation.

goto fail

:execute
if not exist .\lib (
    @echo ERROR: JAR files not found. Make sure you have a lib directory
    @echo directly below your current one containing all the JAR files
    goto fail
)
"%JAVA_EXE%" -cp lib/^* io.ktor.server.netty.EngineMain %*
:fail
exit /b 1
