#!/usr/bin/env bash

ARGS="$@"

check_java_home() {

    if [ -z "$JAVA_HOME" ]; then
      echo "The JAVA_HOME environment variable is not set."
      echo "This is required to run andes client"
      exit 1
    fi
    if [ ! -r "$JAVA_HOME"/bin/java ]; then
      echo "The JAVA_HOME environment variable is not set correctly."
      echo "This is required to run andes client"
      exit 1
    fi
}

main() {

    check_java_home
    echo "Using JAVA_HOME:   $JAVA_HOME"
    java -jar andes-test-client.jar $ARGS
}

main