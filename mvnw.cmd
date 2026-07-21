@echo off
if exist "%~dp0mvn_dist\apache-maven-3.9.8\bin\mvn.cmd" (
    "%~dp0mvn_dist\apache-maven-3.9.8\bin\mvn.cmd" %*
) else (
    mvn %*
)
