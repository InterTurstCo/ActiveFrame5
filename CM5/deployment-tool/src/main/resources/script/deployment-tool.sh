#!/usr/bin/env bash

JAVA=/home/alexander/workspaces/vendor/jdk1.8.0_91

if [ -z "$JAVA" ]; then
    JAVA=java
else
    JAVA=$JAVA/bin/java
fi

$JAVA -jar deployment-tool-0.5.21-11-SNAPSHOT-jar-with-dependencies.jar