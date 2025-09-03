@echo off
echo Downloading JavaFX SDK...
powershell -Command "& {$client = new-object System.Net.WebClient; $client.DownloadFile('https://download2.gluonhq.com/openjfx/21.0.1/openjfx-21.0.1_windows-x64_bin-sdk.zip', 'javafx-sdk.zip')}"

echo Extracting JavaFX SDK...
powershell -Command "& {Expand-Archive -Path 'javafx-sdk.zip' -DestinationPath 'lib' -Force}"

echo Moving JAR files...
move lib\javafx-sdk-21.0.1\lib\*.jar lib\
rmdir /s /q lib\javafx-sdk-21.0.1

echo Cleaning up...
del javafx-sdk.zip

echo Done! Now you can compile with JavaFX.
