#!/usr/bin/env bash

JAVA_HOME=/home/alexander/workspaces/vendor/jdk1.8.0_91

if [ -z "$JAVA_HOME" ]; then
    JAVA_HOME=java
else
    JAVA_HOME=$JAVA_HOME/bin/java
fi

$JAVA_HOME -jar deployment-tool-0.5.21-11-SNAPSHOT-jar-with-dependencies.jar