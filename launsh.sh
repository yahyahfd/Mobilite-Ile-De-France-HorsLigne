#!/bin/bash

main=$1

# Check if the option it is specified: so launch the terminal main
if [ -n "$main" ] && [ $main == "-it" ]; then
  cd src/main/java
  javac fr/uparis/beryllium/TerminalApplication.java
  java fr.uparis.beryllium.TerminalApplication
  rm -rf fr/uparis/beryllium/*.class
# else launch the web main
else
  mvn spring-boot:run
fi


exit 0
    