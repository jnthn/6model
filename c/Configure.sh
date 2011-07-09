#!/bin/sh
cc -Wall -o tools/build/Configure tools/build/Configure.c
tools/build/Configure tools/build/Makefile.in Makefile
