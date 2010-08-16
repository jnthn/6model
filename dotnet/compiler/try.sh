#!/bin/sh
# this version of try for use on Linux
cp ../runtime/bin/Debug/RakudoRuntime.dll .
cp ../runtime/bin/Debug/RakudoRuntime.pdb .
make
parrot compile.pir $1 > x.cs
gmcs x.cs /reference:RakudoRuntime.dll
echo ---
./x.exe
