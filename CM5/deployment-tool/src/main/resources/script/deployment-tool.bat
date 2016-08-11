@echo off

set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_75

if "x%JAVA_HOME%" == "x" (
  set JAVA_HOME=java
) else (
  set JAVA_HOME="%JAVA_HOME%\bin\java"
)

"%JAVA_HOME%" -jar deployment-tool-0.5.21-11-SNAPSHOT-jar-with-dependencies.jar