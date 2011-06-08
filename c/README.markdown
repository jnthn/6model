# README.markdown for 6model/c

This subproject will try to port 6model to the C language.  There are
similarities with the 6model/java subproject, and some differences
besides the target language.

This port will also track upstream changes, but not from another subtree
such as ../dotnet/ in the 6model repository.  Instead, the prototype is
https://github.com/perl6/nqp because of some useful advantages:

* Much of the source code is already written in C.

* The code is frequently improved and tested.

There are some small practical inconveniences.  The build tools assume
that 6model and nqp are sibling project directories.  After Rakudo/nom
replaces Rakudo/beta, the build tools will be adjusted to assume that
6model and Rakudo are sibling directories.

## Supplying the functionality missing in C

Compared to other Perl 6 runtime environments such as Parrot, CLR or
JVM, C lacks some fairly essential capabilities.  Here is how this
subproject proposes to provide what is missing:

* Hashes.  There will be a HashTable library.

* Garbage Collection.  There will be a reference counting garbage
collector, similar to the one in Perl 5, maybe improved with circular
reference detection.  It would be nice to also have a shim that allows
other garbage collectors such as libgc (Boehm) to be used instead.

* Unicode handling.  There will be UTF-8 awareness in the input/output
routines and in the byte-oriented storage.   The port will make the
imperfect assumption that codepoints are equivalent to characters.
Later there will be hooks for dynamically loadable libraries to override
that assumption (Unicode is huge and growing and implies performance
tradeoffs).

