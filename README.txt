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
  for the moment. In time, it's expected to become the basis
  for Rakudo on .Net. It also has a version of the NQP grammar
  and actions with some changes that are gradually making it
  into nqp-rx on Parrot also.

* java - work by mberends++ to port the contents of dotnet over
  to the JVM. Take a look in the directory for a far more detailed
  README.

The work on the Parrot implementation of 6model is in the nom (New
Object Model) branch of the nqp-rx repository.

The things in this repository that work out well may end up being
migrated elsewhere when the time is right (for example, it may be
decided that all NQP implementations should live in the nqp-rx
repository). For now, though, development of the .Net and JVM
implemenations of NQP and the Perl 6 object model remains in here.

Note that things in here may change very rapidly - if you rely on
anything in this repo for anything then you're nuts. :-)
