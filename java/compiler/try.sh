#!/bin/sh
# This version of try.sh for 6model/java on Unix
# cp ../runtime/RakudoRuntime.jar . # rather reference the original 
if expr "$1" = "" > /dev/null; then
    echo Usage: ./try.sh '<Perl 6 source file>'
    exit 1
fi
make || exit 2
parrot compile.pir $1 > x.java || exit 3
javac -classpath ../runtime/RakudoRuntime.jar x.java || exit 4
echo ---
java -classpath ../runtime/RakudoRuntime.jar x.class
