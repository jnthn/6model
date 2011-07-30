#!/bin/sh
# TODO
# - Add a --help option and provide for more options in general
export COMPILER=GCC
set opts_gcc=-Wall -DCC=$COMPILER
cc $opts_gcc -o tools/build/Configure tools/build/Configure.c
tools/build/Configure tools/build/Makefile.in Makefile
