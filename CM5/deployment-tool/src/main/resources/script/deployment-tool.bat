@echo off

set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_75

if "x%JAVA_HOME%" == "x" (
  java -jar deployment-tool-0.0.1-SNAPSHOT.jar
) else (
  "%JAVA_HOME%\bin\java" -jar deployment-tool-0.0.1-SNAPSHOT.jar
)