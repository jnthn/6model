Overview of the Java translation of 6model
==========================================

In the 6model project, the java tree contains per-statement translations
of the C# files in the dotnet tree.  C# is the "Microsoft Java" and this
subproject shows how close they are.


Status
------

The Metamodel is fully translated.  The setting loader (in Init.java)
needs to be rewritten in terms of ClassLoader.  The compiler needs to be
translated.


Source formatting guidelines
----------------------------

For clarity, or where there is doubt about the correctness of a
translation, include the original line afterwards in a comment.

Use horizontal spacing to maximize correlation of text in consecutive
lines.

With apologies to jnthn++, a widely used case convention for the initial
letter of names is being gradually phased in.  Class names, class member
names and interfaces start with an uppercase letter.  Local variables
begin in lowercase.


6model specific translations
----------------------------

The RakudoObject interface defines properties STable and SC (meaning
SharedTable and SerializationContext).  Java does not have properties,
these become private data members and get accessor methods getSTable(),
setSTable(), getSC() and setSC().


C# to Java translation guidelines (in case insensitive alphabetical order)
--------------------------------------------------------------------------

C# abstract class becomes Java interface.

C# Add(item) (to a List) becomes Java add(item) (to an ArrayList).

C# AddRange(...) (to a List) becomes Java XXX TODO ???

C# bool becomes Java boolean.

C# Console.Write becomes Java System.out.print.

C# const becomes Java static final.

C# Count (of a List) becomes Java size() (of an ArrayList).

C# Dictionary becomes Java HashMap.  The HashMap is not quite as
versatile, because the value part must be a reference type, it cannot be
a value type.  Therefore C# Dictionary<string, int> becomes Java
HashMap<String, Integer> which it less convenient to use.  Also C#
myHash[key] becomes Java myHash.get(key).
See http://www.tutorialspoint.com/java/java_hashmap_class.htm

C# foreach ( <type> name in <Iterable> ) becomes Java
for ( <type> name : <Iterable> ).

C# Func<typelist> (see Lambdas etc below) becomes a Java anonymous class
that implements an interface.

C# internal access modifier becomes Java protected or public.

C# InvalidOperationException becomes Java UnsupportedOperationException.

C# lambda expressions (using => notation) become Java anonymous classes.
This is a lot of workaround writing because the anonymous classes
require an interface definition to implement, otherwise they inherit
from Object.

C# Length (of an array) becomes Java length (of an array).

C# Length (of a List) becomes Java size() (of an ArrayList).

C# List (in System.Collections.Generic) becomes Java ArrayList (in java.util).

C# Min(a,b) (in System.Math) becomes Java min(a,b) in java.lang.Math.

C# namespace yada { ... } becomes Java package yada; ... .
Also the Java package hierarchy must match the file system directory
hierarchy.

C# NotImplementedException becomes Java NoSuchFieldException or
NoSuchMethodException.

C# override does not become anything in Java (just delete it).

C# out parameters become... 

C# sealed class becomes Java final class.  Effect is not quite the same.

C# static becomes Java final.

C# string becomes Java String.

C# using becomes Java import.  In general, avoid the * (Whatever) form,
such as java.util.* because it is often useful to know exactly what is
being imported, for example java.util.HashMap or java.util.ArrayList.
The * is a form of programmer laziness which just shifts the problem on
to the next person or compiler who reads the program.  We need clarity
here, not golf.

C# var becomes Java <typename> because Java does not do implicit typing.
You have to work out the type yourself and declare it :-(

Lambdas and References to Functions
-----------------------------------
C# has some language features that Java currently lacks, to safely
provide what C and C++ call pointers to functions.

The C# 'Func' generic type is a parameterized type.  A variable declared
as 'Func<paramtype [,...], rettype>' is a kind of delegate that
encapsulates an anonymous function that takes specified parameters and
returns a specified result.  Call the function using the Invoke(...)
method on the Func variable.

The C# '=>' operator (also called Lambda) creates a reference to a block
of code.  Store that reference in a Func variable.

The C# 'delegate' type contains a collection of function pointers.  When
the delegate is invoked, each function that is pointed to gets called in
an unspecified order.  Useful for multicast notification, event handlers
and publish and subscribe architectures.

The C# implementation of 6model uses the 'Func' and '=>' combination in
KnowHOWBootstrapper and RakudoCodeRef for example.  The Java
implementation replaces the Func declaration with an IFunc_XXX interface
declaration that defines a suitably typed Invoke method, and replaces
the => with an anonymous class definition that implements that
interface.

See: http://msdn.microsoft.com/en-us/library/bb549151.aspx and
http://dotnetperls.com/func


See Also
--------

IKVM (http://sourceforge.net/apps/mediawiki/ikvm/index.php?title=FAQ)
runs Java code on Mono and .NET CLR.

Compare Java and C#: http://discuss.joelonsoftware.com/default.asp?joel.3.456095.37

Compare JVM and CLR: http://benjismith.net/index.php/2006/06/23/biz-idea-08-dotjnet-bytecode-translator/

Compare JVM and CLR code security: http://onjava.com/pub/a/onjava/2003/11/26/javavsdotnet.html?page=2&x-maxdepth=0

Created by: Martin Berends (mberends in #perl6 on irc.freenode.net)

