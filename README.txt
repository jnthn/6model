This repository is a playground for those of us working on
replacing Rakudo and nqp-rx's metamodel and preparing for
adding support for additional compiler backends. Specifically,
it includes:

* common - a bunch of stuff that one day may be common between
  nqp-rx implementations over more than one backend.

* dotnet - an in-progress implementation of a runtime layer for
  NQP and Perl 6, as well as a PAST to C# compiler. This is
  being hacked on by Jonathan Worthington, and also serves as
  his primary research environment for various Perl 6 features
  for the moment. Provided it proves suitable, parts of it will
  later become the foundation for Rakudo on .Net. It also has a
  version of the NQP grammar and actions with some changes that
  will perhaps make it into the main NQP (or at least have some
  bearing on it).

* java - work by mberends++ to port the contents of dotnet over
  to the JVM. Take a look in the directory for a far more detailed
  README.

* parrot - some early work on getting the new meta-model devloped
  in dotnet over to the Parrot virtual machine. This work will be
  moving to a branch in the nqp-rx repository shortly for reasons
  of developer convenience.

Nothing in here that's any good will remain in here forever; it'll
likely mostly get migrated into the nqp-rx repository when we're
ready to do that. Things in here will change very rapidly - if you
rely on anything in this repo for anything then you're nuts. :-)
