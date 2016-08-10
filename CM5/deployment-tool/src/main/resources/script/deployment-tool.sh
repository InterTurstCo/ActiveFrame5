#!/usr/bin/env bash

JAVA_HOME=/home/alexander/workspaces/vendor/jdk1.8.0_91

if [ -z "$JAVA_HOME" ]; then
    java -jar deployment-tool-0.0.1-SNAPSHOT.jar
else
    $JAVA_HOME/bin/java -jar deployment-tool-0.0.1-SNAPSHOT.jar
fi