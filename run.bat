@echo off
javac --module-path lib --add-modules javafx.controls,javafx.media,javafx.swing *.java
if %errorlevel% equ 0 (
    echo Compilation successful! Running the game...
    java --module-path lib --add-modules javafx.controls,javafx.media,javafx.swing VietnamAirDefense
) else (
    echo Compilation failed.
)
