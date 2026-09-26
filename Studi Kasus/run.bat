@echo off
title Kopma Mart POS - Kasir Cepat Minimarket Kampus
echo ===================================================================
echo     KOPMA MART POS (KOPERASI MAHASISWA) - MINIMARKET KAMPUS
echo ===================================================================
echo [1/2] Memeriksa compiler Java...

set JAVA_CMD=java
set JAVAC_CMD=javac

if exist "C:\Program Files\Java\jdk-21.0.12\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk-21.0.12\bin\java.exe"
    set "JAVAC_CMD=C:\Program Files\Java\jdk-21.0.12\bin\javac.exe"
)

if not exist bin mkdir bin

echo [2/2] Melakukan kompilasi source code Java...
"%JAVAC_CMD%" -d bin -encoding UTF-8 src\*.java

if %ERRORLEVEL% EQU 0 (
    echo [OK] Kompilasi berhasil! Membuka antarmuka POS Kopma Mart...
    start "" "%JAVA_CMD%" -cp bin KopmaMartPOS
) else (
    echo [ERROR] Terjadi kegagalan saat kompilasi. Pastikan JDK terpasang dengan benar.
    pause
)
