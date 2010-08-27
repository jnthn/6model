# Makefile for 6model/java/runtime

ALL_BUILD_TARGETS = \
	Rakudo/Metamodel/Hints.class \
	Rakudo/Serialization/SerializationContext.class \
	Rakudo/Runtime/ExecutionDomain.class \
	Rakudo/Metamodel/RakudoObject.class

#	Rakudo/Metamodel/KnowHOW/KnowHOWREPR.class

OTHER_DEPENDENT_TARGETS = \
	Rakudo/Runtime/Lexpad.class \
	Rakudo/Runtime/Parameter.class \
	Rakudo/Runtime/Signature.class \
	Rakudo/Metamodel/Representation.class \
	Rakudo/Metamodel/REPRRegistry.class \
	Rakudo/Metamodel/Representations/RakudoCodeRef.class \
	Rakudo/Metamodel/SharedTable.class

all: $(ALL_BUILD_TARGETS)

# compiles ok: (listed in dependency order)
Rakudo/Metamodel/Hints.class: Rakudo/Metamodel/Hints.java
	javac Rakudo/Metamodel/Hints.java

Rakudo/Serialization/SerializationContext.class: Rakudo/Serialization/SerializationContext.java
	javac Rakudo/Serialization/SerializationContext.java

Rakudo/Runtime/ExecutionDomain.class: Rakudo/Runtime/ExecutionDomain.java
	javac Rakudo/Runtime/ExecutionDomain.java

# There is a long circular dependency from RakudoObject to SharedTable.
# Handle it by compiling all the files involved in a single step.
# Thanks jnthn++ for the suggestion.
Rakudo/Metamodel/RakudoObject.class: Rakudo/Metamodel/RakudoObject.java \
		Rakudo/Serialization/SerializationContext.class \
		Rakudo/Metamodel/Representation.java \
		Rakudo/Metamodel/REPRRegistry.java \
		Rakudo/Runtime/Lexpad.java \
		Rakudo/Runtime/Parameter.java \
		Rakudo/Runtime/Signature.java \
		Rakudo/Metamodel/Representations/RakudoCodeRef.java \
		Rakudo/Metamodel/SharedTable.java
	javac \
		Rakudo/Metamodel/RakudoObject.java \
		Rakudo/Metamodel/Representation.java \
		Rakudo/Metamodel/REPRRegistry.java \
		Rakudo/Runtime/Lexpad.java \
		Rakudo/Runtime/Parameter.java \
		Rakudo/Runtime/Signature.java \
		Rakudo/Metamodel/Representations/RakudoCodeRef.java \
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

# Dependency critical paths (1 2 and 3 are circular)
# (partial diagram - less significant dependencies omitted for simplicity)
#          
#                              Hints
#                                |
#                       SerializationContext   ExecutionDomain
#                                |
#         +-----------> RakudoObject
#         |              |       |
#         |       Parameter      |
#         |            |         |
#         |       Signature  Representation
#         |            |      |     |    |
#         | +-+---> RakudoCodeRef   |   REPRRegistry
#         | | |           |         |
#         1 2 3        Context      |
#         | | |           |         |
#         | | +--- ThreadContext    |
#         | |             |         |
#         | |        IFindMethod    |
#         | |                 |     |
#         +-+-------------- SharedTable
#                                |

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

Rakudo/Runtime/Parameter.class: Rakudo/Runtime/Parameter.java \
		Rakudo/Metamodel/RakudoObject.class
	javac Rakudo/Runtime/Parameter.java

Rakudo/Runtime/Signature.class: Rakudo/Runtime/Signature.java \
		Rakudo/Runtime/Parameter.class
	javac Rakudo/Runtime/Signature.java

# System to report which files are older than their dotnet equivalents.
# Compare the reported files manually, and refresh the java file.
TRANSLATED_SOURCE_FILES = \
	Rakudo/Metamodel/Hints.java \
	Rakudo/Serialization/SerializationContext.java \
	Rakudo/Metamodel/RakudoObject.java \
	Rakudo/Runtime/Parameter.java \
	Rakudo/Runtime/Signature.java \
	Rakudo/Metamodel/Representation.java \
	Rakudo/Metamodel/Representations/RakudoCodeRef.java
# Java source code files "depend" on files in the dotnet/ directories,
# in the sense that they are manual translations.
# The following definitions cause make to exit with an todo message if
# a dotnet/ file becomes newer than a java/ file.
# The way to clear the error is to compare the files in question, and
# then to refresh the java/ file to be newer than the dotnet/ one.
Rakudo/Metamodel/Hints.java: ../../dotnet/runtime/Metamodel/Representation.cs
	@echo "todo: Rakudo/Metamodel/Hints.java is older than ../../dotnet/runtime/Metamodel/Representation.cs"

Rakudo/Serialization/SerializationContext.java: ../../dotnet/runtime/Serialization/SerializationContext.cs
	@echo "todo: Rakudo/Serialization/SerializationContext.java is older than ../../dotnet/runtime/Serialization/SerializationContext.cs"

Rakudo/Metamodel/RakudoObject.java: ../../dotnet/runtime/Metamodel/RakudoObject.cs
	@echo "todo: Rakudo/Metamodel/RakudoObject.java is older than ../../dotnet/runtime/Metamodel/RakudoObject.cs"

Rakudo/Runtime/Parameter.java: ../../dotnet/runtime/Runtime/Signatures/Parameter.cs
	@echo "todo: Rakudo/Runtime/Parameter.java is older than ../../dotnet/runtime/Runtime/Signatures/Parameter.cs"

Rakudo/Runtime/Signature.java: ../../dotnet/runtime/Runtime/Signatures/Signature.cs
	@echo "todo: Rakudo/Runtime/Signature.java is older than ../../dotnet/runtime/Metamodel/Signatures/Signature.cs"

Rakudo/Metamodel/Representation.java: ../../dotnet/runtime/Metamodel/Representation.cs
	@echo "todo: Rakudo/Metamodel/Representation.java is older than ../../dotnet/runtime/Metamodel/Representation.cs"

Rakudo/Metamodel/Representations/RakudoCodeRef.java: ../../dotnet/runtime/Metamodel/Representations/RakudoCodeRef.cs
	@echo "todo: $@ is older than $<"

# To show java files that are older than their dotnet equivalents, run:
# make todolist
todolist: $(TRANSLATED_SOURCE_FILES)

clean:
	perl -MExtUtils::Command -e rm_f $(ALL_BUILD_TARGETS) $(OTHER_DEPENDENT_TARGETS)

help:
	@echo "In 6model/java/runtime you can make the following:"
	@echo "all       (default) compile everything"
	@echo "clean     remove compiled class files"
	@echo "todolist  show files where java version is older than dotnet"
	@echo "help      this summary"
