Overview of the Java translation of 6model
==========================================

In the 6model project, the java tree contains per-statement translations
of the C# files in the dotnet tree.  C# is the "Microsoft Java" and this
subproject shows how close they are.


Source formatting guidelines
----------------------------

For clarity, or where there is doubt about the correctness of a
translation, include the original line afterwards in a comment.

Add horizontal spacing to maximize correlation of text in consecutive
lines.

With apologies to jnthn++, a case convention for the initial letter of
names is being gradually phased in.  Class names, class member names and
interfaces is start with an uppercase letter.  Local variables begin in
lowercase.


C# to Java translation guidelines (sorted alphabetically, case insensitive)
---------------------------------------------------------------------------

C# bool becomes Java boolean.

C# Dictionary becomes Java HashMap.  The HashMap is not quite as
versatile, because the value part must be a reference type, it cannot be
a value type.  Therefore C# Dictionary<string, int> becomes Java
HashMap<String, Integer> which it less convenient to use.

C# internal access modifier becomes Java protected or public.

C# Func<typelist> becomes a Java anonymous class that implements an
interface.

C# InvalidOperationException becomes Java UnsupportedOperationException.

C# lambda expressions (using => notation) become Java anonymous classes.
This is a lot of workaround writing because the anonymous classes
require an interface definition to implement, otherwise they inherit
from Object.

C# Length (of an array) becomes Java length (of an array).

C# Length (of a List) becomes Java size() (of an ArrayList).

C# List becomes Java ArrayList.

C# namespace yada { ... } becomes Java package yada; ... .
Also the Java package hierarchy must match the file system directory
hierarchy.

C# NotImplementedException becomes Java NoSuchFieldException or
NoSuchMethodException.

C# override does not become anything in Java (just delete it).

C# sealed class becomes Java final class.  Effect is not quite the same.

C# string becomes Java String.

C# using becomes Java import.  In general, avoid the * (Whatever) form,
such as java.util.* because it is often useful to know exactly what is
being imported, for example java.util.HashMap or java.util.ArrayList.
The * is a form of programmer laziness which just shifts the problem on
to the next person or compiler who reads the program.  We need clarity
here, not golf.


Lambdas and References to Functions
-----------------------------------
C# has some language features that Java currently lacks, to safely
provide what C and C++ call pointers to functions.

The C# 'Func' generic type is a parameterized type.  Variables declared
as 'Func<paramtype [,...], rettype>' store anonymous functions that take
certain parameters and return a certain result.  Call the function using
the Invoke(...) method on the Func variable.

The C# '=>' operator (also called Lambda) creates a reference to a block
of code.  Store that reference in a Func variable.

The C# 'delegate' type contains a collection of function pointers.  When
the delegate is called, each function that is pointed to gets called in
an unspecified order.  Useful for multicast notification, event handlers
and publish and subscribe architectures.

The C# implementation of 6model uses the 'Func' and '=>' combination in
KnowHOWBoostrapper and RakudoCodeRef for example.  The Java
implementation replaces the Func declaration with an IFunc_XXX interface
declaration that defines a suitably typed Invoke method, and replaces
the => with an anonymous class definition that implements that
interface.

Created by: Martin Berends (mberends in #perl6 on irc.freenode.net)

