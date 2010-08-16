#!/bin/sh
# this version of try for use on Linux
make
parrot compile.pir $1 > x.cs
cp ../runtime/bin/Debug/RakudoRuntime.dll .
cp ../runtime/bin/Debug/RakudoRuntime.pdb .
gmcs x.cs /reference:RakudoRuntime.dll
echo ---
./x.exe
