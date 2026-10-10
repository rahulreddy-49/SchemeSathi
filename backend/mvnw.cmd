@echo off
@setlocal

@REM SchemeSathi Maven Runner for Windows
@set MAVEN_BIN="%~dp0..\.maven\apache-maven-3.9.6\bin\mvn.cmd"

@if exist %MAVEN_BIN% (
    call %MAVEN_BIN% %*
) else (
    mvn %*
)
