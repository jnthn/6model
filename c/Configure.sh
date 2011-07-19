#!/bin/sh
# TODO
# - Add a --help option and provide for more options in general
cc -Wall -o tools/build/Configure tools/build/Configure.c
tools/build/Configure tools/build/Makefile.in Makefile