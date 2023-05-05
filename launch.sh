#!/bin/bash

main=$1

# Check if the option it is specified: so launch the terminal main
if [ -n "$main" ] && [ $main == "-it" ]; then
  mvn compile
  mvn exec:java -Dexec.mainClass="fr.uparis.beryllium.TerminalApplication" -Dfile.encoding=UTF-8
# else launch the web main
else
  mvn spring-boot:run
fi


exit 0
    