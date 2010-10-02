#!/bin/sh
# This version of try.sh is for 6model/java on Unix.
if expr "$1" = "" > /dev/null; then
    echo Usage: ./try.sh '<Perl 6 source file>'
    exit 1
fi
make || exit 2
parrot compile.pir $1 > RakudoOutput.java || exit 3
javac -classpath ../runtime/RakudoRuntime.jar RakudoOutput.java || exit 4
echo ---
java -classpath ../runtime/RakudoRuntime.jar RakudoOutput.class
