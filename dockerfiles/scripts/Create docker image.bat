@echo off
setlocal enabledelayedexpansion

:askIsMac
set /p isMac="Are you using a Mac? (y/n): "

if /i "%isMac%"=="y" (
    set isMac=true
) else if /i "%isMac%"=="n" (
    set isMac=false
) else (
    echo Invalid input. Please enter y or n.
    goto askIsMac
)

set /p docker_username="Enter your Docker username: "

set turma=42d

set group_number=10

set image_tag=%docker_username%/img-ls-2324-2-%turma%-g%group_number%

echo Image tag: %image_tag%

pause

if "%isMac%"=="true" (
    docker build -t %image_tag% --platform linux/amd64 .
) else (
    docker build -t %image_tag% .
)

docker push %image_tag%

echo Docker image built with tag: %image_tag%

pause