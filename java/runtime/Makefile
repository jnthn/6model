# Makefile for 6model/java/runtime

ALL_CLASSES = \
	Rakudo/Metamodel/Hints.class \
	Rakudo/Serialization/SerializationContext.class \
	Rakudo/Metamodel/RakudoObject.class \
	Rakudo/Metamodel/Representation.class \
	Rakudo/Metamodel/REPRRegistry.class

#	Rakudo/Metamodel/SharedTable.class
#	Rakudo/Metamodel/KnowHOW/KnowHOWREPR.class

all: $(ALL_CLASSES)

# compiles ok: (listed in dependency order)
Rakudo/Metamodel/Hints.class: Rakudo/Metamodel/Hints.java
	javac Rakudo/Metamodel/Hints.java

Rakudo/Serialization/SerializationContext.class: Rakudo/Serialization/SerializationContext.java
	javac Rakudo/Serialization/SerializationContext.java

# There is a long circular dependency from RakudoObject to SharedTable.
# Handle it by compiling all the files involved in a single step.
# Thanks jnthn++ for the suggestion.
Rakudo/Metamodel/RakudoObject.class: Rakudo/Metamodel/RakudoObject.java \
		Rakudo/Serialization/SerializationContext.class \
		Rakudo/Metamodel/Representation.java \
		Rakudo/Metamodel/REPRRegistry.java \
		Rakudo/Metamodel/SharedTable.java
	javac \
		Rakudo/Metamodel/RakudoObject.java \
		Rakudo/Metamodel/Representation.java \
		Rakudo/Metamodel/REPRRegistry.java \
		Rakudo/Metamodel/SharedTable.java

Rakudo/Metamodel/Representations/RakudoCodeRef.class: Rakudo/Metamodel/Representations/RakudoCodeRef.java \
		javac Rakudo/Runtime/ThreadContext.class
	javac Rakudo/Metamodel/Representations/RakudoCodeRef.java

Rakudo/Runtime/Context.class: Rakudo/Runtime/Context.java \
		Rakudo/Metamodel/Representations/RakudoCodeRef.class \
		Rakudo/Metamodel/RakudoObject.class
	javac Rakudo/Runtime/Context.java

Rakudo/Runtime/ThreadContext.class: Rakudo/Runtime/ThreadContext.java \
		Rakudo/Runtime/Context.class \
		Rakudo/Metamodel/RakudoObject.class
	javac Rakudo/Runtime/ThreadContext.java

# Dependency critical paths (1 and 2 are circular)
# (less significant dependencies omitted for simplicity)
#          
#                              Hints
#                                |
#                         Serialization
#                                |
#         +--------------> RakudoObject
#         |                      |
#         |             SerializationContext
#         |                      |
#         |                Representation
#         |                  |    |    |
#         |   +--> RakudoCodeRef  |  REPRRegistry
#         |   |         |         |
#         1   2      Context      |
#         |   |         |         |
#         |   +--- ThreadContext  |
#         |             |         |
#         |          IFindMethod  |
#         |                   |   |
#         +---------------- SharedTable
#                                 |

Rakudo/Metamodel/IFindMethod.class: Rakudo/Metamodel/IFindMethod.java
		Rakudo/Metamodel/RakudoObject.class \
		Rakudo/Runtime/ThreadContext.class
	javac Rakudo/Metamodel/IFindMethod.java

Rakudo/Metamodel/SharedTable.class: Rakudo/Metamodel/SharedTable.java \
		Rakudo/Metamodel/IFindMethod.class \
		Rakudo/Metamodel/Representation.class
	javac Rakudo/Metamodel/SharedTable.java
    # this file has two lambda expression code commented out, and needs
    # to be completed urgently
# end circular dependency

Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.class: Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.java \
	javac Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.java
	# this file has 6 lambda expressions to translate, currently suffering
	# from 55 compile time errors :(

Rakudo/Metamodel/KnowHOW/KnowHOWREPR.class: Rakudo/Metamodel/KnowHOW/KnowHOWREPR.java \
		Rakudo/Metamodel/Hints.class
	javac Rakudo/Metamodel/KnowHOW/KnowHOWREPR.java

# System to report which files are older than their dotnet equivalents.
# Compare the reported files manually, and refresh the java file.
PORTED_SOURCE_FILES = \
	Rakudo/Metamodel/Hints.java \
	Rakudo/Serialization/SerializationContext.java \
	Rakudo/Metamodel/RakudoObject.java \
	Rakudo/Metamodel/Representation.java

# Java source code files "depend" on files in the dotnet/ directories,
# in the sense that they are manual translations.  The following
# dependencies cause on C# that can only be satisfied by
# manual editing.
Rakudo/Metamodel/Hints.java: ../../dotnet/runtime/Metamodel/Representation.cs
	@echo "warning: Rakudo/Metamodel/Hints.java is older than ../../dotnet/runtime/Metamodel/Representation.cs"

Rakudo/Serialization/SerializationContext.java: ../../dotnet/runtime/Serialization/SerializationContext.cs
	@echo "warning: Rakudo/Serialization/SerializationContext.java is older than ../../dotnet/runtime/Serialization/SerializationContext.cs"

Rakudo/Metamodel/RakudoObject.java: ../../dotnet/runtime/Metamodel/RakudoObject.cs
	@echo "warning: Rakudo/Metamodel/RakudoObject.java is older than ../../dotnet/runtime/Metamodel/RakudoObject.cs"

Rakudo/Metamodel/Representation.java: ../../dotnet/runtime/Metamodel/Representation.cs
	@echo "warning: Rakudo/Metamodel/Representation.java is older than ../../dotnet/runtime/Metamodel/Representation.cs"

todoreport: $(PORTED_SOURCE_FILES)

clean:
	perl -MExtUtils::Command -e rm_f $(ALL_CLASSES)

help:
	@echo "In 6model/java/runtime you can make the following:"
	@echo "all         (default) compile everything"
	@echo "clean       remove compiled class files"
	@echo "todoreport  list files where dotnet version is newer than java"
	@echo "help        this summary"
