# Makefile for 6model/java/runtime

# Dependency critical paths (1 2 3 and 4 are circular)
# (partial diagram - less significant (eg implicit transitive) dependencies
#  omitted for simplicity)
#
#                                           Hints
#                                             |
#                                          SerializationContext
#                                             |             |
#        +------------------------> RakudoObject            |
#        |                           |  |     |             |
#        |                   Parameter Lexpad |             |
#        |                         |    |     |             |
#        |                   Signature  |   Representation  |
#        |                         |    |    |   |    |     |
#        |  +--+----------------> RakudoCodeRef  |    |     |
#        |  |  |                        |        |    |     |
#        1  2  3   ExecutionDomain   Context     |    |     |
#        |  |  |                |       |        |    |     |
#        |  |  +-------------- ThreadContext     |    |     |
#        |  |                           |        |    |     |
#        |  |          P6capture        |        |    |     |
#        |  |             |             |        |    |     |
#        |  |     MultiDispatcher       |        |    |     |
#        |  |             |             |        |    |     |
#        |  |  +-------> Ops  P6hash    |        |    |     |
#        |  |  |          |   P6int     |        |    |     |
#        |  |  4         P6opaque    IFindMethod |  REPRRegistry
#        |  |  |                |            |   |      |
#        |  |  |               CaptureHelper |   |      |
#        |  |  |                          |  |   |      |
#        +--+--+------------------------ SharedTable    |
#                                             |         |
#                                        KnowHOWREPR  CodeObjectUtility
#                                             |         |
#                                        KnowHOWBootstrapper
#                                                  |
#                                                  | P6num
#                                                  | P6str
#                                                  | |
#                                                 Init
#

ALL_BUILD_TARGETS = \
	Rakudo/Metamodel/Hints.class \
	Rakudo/Serialization/SerializationContext.class \
	Rakudo/Runtime/ExecutionDomain.class \
	Rakudo/Metamodel/RakudoObject.class \
	Rakudo/Metamodel/REPRRegistry.class \
	Rakudo/Metamodel/Representations/P6capture.class \
	Rakudo/Metamodel/Representations/P6hash.class \
	Rakudo/Metamodel/Representations/P6int.class \
	Rakudo/Metamodel/Representations/P6num.class \
	Rakudo/Metamodel/Representations/P6str.class \
	Rakudo/Metamodel/Representations/P6opaque.class \
	Rakudo/Runtime/CodeObjectUtility.class \
	Rakudo/Metamodel/KnowHOW/KnowHOWREPR.class \
	Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.class \
	Rakudo/Init.class

OTHER_DEPENDENT_TARGETS = \
	Rakudo/Runtime/CaptureHelper.class \
	Rakudo/Runtime/Lexpad.class \
	Rakudo/Runtime/Parameter.class \
	Rakudo/Runtime/Signature.class \
	Rakudo/Metamodel/Representation.class \
	Rakudo/Metamodel/Representations/P6list.class \
	Rakudo/Metamodel/Representations/RakudoCodeRef.class \
	Rakudo/Runtime/Context.class \
	Rakudo/Runtime/MultiDispatch/LexicalCandidateFinder.class \
	Rakudo/Runtime/MultiDispatch/MultiDispatcher.class \
	Rakudo/Runtime/Ops.class \
	Rakudo/Runtime/ThreadContext.class \
	Rakudo/Metamodel/IFindMethod.class \
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
# Handle it by compiling all the interdependent files in a single step.
# Thanks jnthn++ for the suggestion.
Rakudo/Metamodel/RakudoObject.class: Rakudo/Metamodel/RakudoObject.java \
		Rakudo/Serialization/SerializationContext.class \
		Rakudo/Metamodel/Representation.java \
		Rakudo/Runtime/CaptureHelper.java \
		Rakudo/Runtime/Lexpad.java \
		Rakudo/Runtime/Parameter.java \
		Rakudo/Runtime/Signature.java \
		Rakudo/Metamodel/Representations/P6list.java \
		Rakudo/Metamodel/Representations/RakudoCodeRef.java \
		Rakudo/Runtime/Context.java \
		Rakudo/Runtime/MultiDispatch/LexicalCandidateFinder.java \
		Rakudo/Runtime/MultiDispatch/MultiDispatcher.java \
		Rakudo/Runtime/Ops.java \
		Rakudo/Runtime/ThreadContext.java \
		Rakudo/Metamodel/IFindMethod.java \
		Rakudo/Metamodel/SharedTable.java
	javac \
		Rakudo/Metamodel/RakudoObject.java \
		Rakudo/Metamodel/Representation.java \
		Rakudo/Runtime/CaptureHelper.java \
		Rakudo/Runtime/Lexpad.java \
		Rakudo/Runtime/Parameter.java \
		Rakudo/Runtime/Signature.java \
		Rakudo/Metamodel/Representations/P6list.java \
		Rakudo/Metamodel/Representations/RakudoCodeRef.java \
		Rakudo/Runtime/Context.java \
		Rakudo/Runtime/MultiDispatch/LexicalCandidateFinder.java \
		Rakudo/Runtime/MultiDispatch/MultiDispatcher.java \
		Rakudo/Runtime/Ops.java \
		Rakudo/Runtime/ThreadContext.java \
		Rakudo/Metamodel/IFindMethod.java \
		Rakudo/Metamodel/SharedTable.java

Rakudo/Metamodel/REPRRegistry.class: Rakudo/Metamodel/REPRRegistry.java
	javac Rakudo/Metamodel/REPRRegistry.java

Rakudo/Runtime/CodeObjectUtility.class: Rakudo/Runtime/CodeObjectUtility.java \
		Rakudo/Metamodel/RakudoObject.class \
		Rakudo/Metamodel/REPRRegistry.class \
		Rakudo/Metamodel/Representation.class \
		Rakudo/Metamodel/Representations/RakudoCodeRef.class
	javac Rakudo/Runtime/CodeObjectUtility.java

Rakudo/Metamodel/KnowHOW/KnowHOWREPR.class: Rakudo/Metamodel/KnowHOW/KnowHOWREPR.java \
		Rakudo/Metamodel/Hints.class
	javac Rakudo/Metamodel/KnowHOW/KnowHOWREPR.java

Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.class: Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.java \
		Rakudo/Metamodel/IFindMethod.class \
		Rakudo/Metamodel/KnowHOW/KnowHOWREPR.class \
		Rakudo/Metamodel/RakudoObject.class \
		Rakudo/Metamodel/Representation.class \
		Rakudo/Metamodel/Representations/P6capture.class \
		Rakudo/Metamodel/Representations/P6list.class \
		Rakudo/Metamodel/Representations/RakudoCodeRef.class \
		Rakudo/Metamodel/REPRRegistry.class \
		Rakudo/Metamodel/SharedTable.class \
		Rakudo/Runtime/CodeObjectUtility.class \
		Rakudo/Runtime/Ops.class \
		Rakudo/Runtime/ThreadContext.class
	javac Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.java

Rakudo/Metamodel/Representations/P6capture.class: Rakudo/Metamodel/Representations/P6capture.java \
		Rakudo/Metamodel/Hints.class \
		Rakudo/Metamodel/RakudoObject.class \
		Rakudo/Metamodel/Representation.class \
		Rakudo/Metamodel/SharedTable.class \
		Rakudo/Serialization/SerializationContext.class
	javac Rakudo/Metamodel/Representations/P6capture.java

Rakudo/Metamodel/Representations/P6hash.class: Rakudo/Metamodel/Representations/P6hash.java \
		Rakudo/Metamodel/Hints.class \
		Rakudo/Metamodel/RakudoObject.class \
		Rakudo/Metamodel/Representation.class \
		Rakudo/Metamodel/SharedTable.class \
		Rakudo/Serialization/SerializationContext.class
	javac Rakudo/Metamodel/Representations/P6hash.java

Rakudo/Metamodel/Representations/P6int.class: Rakudo/Metamodel/Representations/P6int.java \
		Rakudo/Metamodel/Hints.class \
		Rakudo/Metamodel/RakudoObject.class \
		Rakudo/Metamodel/Representation.class \
		Rakudo/Metamodel/SharedTable.class \
		Rakudo/Serialization/SerializationContext.class
	javac Rakudo/Metamodel/Representations/P6int.java

Rakudo/Metamodel/Representations/P6num.class: Rakudo/Metamodel/Representations/P6num.java \
		Rakudo/Metamodel/Hints.class \
		Rakudo/Metamodel/RakudoObject.class \
		Rakudo/Metamodel/Representation.class \
		Rakudo/Metamodel/SharedTable.class \
		Rakudo/Runtime/ThreadContext.class
	javac Rakudo/Metamodel/Representations/P6num.java

Rakudo/Metamodel/Representations/P6str.class: Rakudo/Metamodel/Representations/P6str.java \
		Rakudo/Metamodel/Hints.class \
		Rakudo/Metamodel/RakudoObject.class \
		Rakudo/Metamodel/Representation.class \
		Rakudo/Metamodel/SharedTable.class \
		Rakudo/Runtime/ThreadContext.class
	javac Rakudo/Metamodel/Representations/P6str.java

Rakudo/Metamodel/Representations/P6opaque.class: Rakudo/Metamodel/Representations/P6opaque.java \
		Rakudo/Metamodel/Hints.class \
		Rakudo/Metamodel/RakudoObject.class \
		Rakudo/Metamodel/Representation.class \
		Rakudo/Metamodel/SharedTable.class \
		Rakudo/Runtime/CaptureHelper.class \
		Rakudo/Runtime/Ops.class \
		Rakudo/Runtime/ThreadContext.class \
		Rakudo/Serialization/SerializationContext.class
	javac Rakudo/Metamodel/Representations/P6opaque.java

Rakudo/Init.class: Rakudo/Init.java \
		Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.class \
		Rakudo/Metamodel/KnowHOW/KnowHOWREPR.class \
		Rakudo/Metamodel/RakudoObject.class \
		Rakudo/Metamodel/Representations/RakudoCodeRef.class \
		Rakudo/Metamodel/Representations/P6capture.class \
		Rakudo/Metamodel/Representations/P6hash.class \
		Rakudo/Metamodel/Representations/P6int.class \
		Rakudo/Metamodel/Representations/P6list.class \
		Rakudo/Metamodel/Representations/P6opaque.class \
		Rakudo/Metamodel/REPRRegistry.class \
		Rakudo/Runtime/CaptureHelper.class \
		Rakudo/Runtime/Context.class \
		Rakudo/Runtime/ExecutionDomain.class \
		Rakudo/Runtime/Lexpad.class \
		Rakudo/Runtime/ThreadContext.class
	javac Rakudo/Init.java

# System to report which java files are older than their dotnet
# equivalents.  The dotnet files churn a lot, so it's important to check
# by code review that the translated java files still correspond.
# To resolve, compare the reported files, and update the java ones.
TRANSLATED_SOURCE_FILES = \
	Rakudo/Init.java \
	Rakudo/Metamodel/Hints.java \
	Rakudo/Serialization/SerializationContext.java \
	Rakudo/Runtime/ExecutionDomain.java \
	Rakudo/Metamodel/RakudoObject.java \
	Rakudo/Metamodel/Representation.java \
	Rakudo/Metamodel/SharedTable.java \
	Rakudo/Metamodel/Representations/P6capture.java \
	Rakudo/Metamodel/Representations/P6hash.java \
	Rakudo/Metamodel/Representations/P6int.java \
	Rakudo/Metamodel/Representations/P6list.java \
	Rakudo/Metamodel/Representations/P6num.java \
	Rakudo/Metamodel/Representations/P6opaque.java \
	Rakudo/Metamodel/Representations/P6str.java \
	Rakudo/Metamodel/Representations/RakudoCodeRef.java \
	Rakudo/Runtime/CaptureHelper.java \
	Rakudo/Runtime/CodeObjectUtility.java \
	Rakudo/Runtime/Context.java \
	Rakudo/Runtime/Lexpad.java \
	Rakudo/Runtime/MultiDispatch/LexicalCandidateFinder.java \
	Rakudo/Runtime/MultiDispatch/MultiDispatcher.java \
	Rakudo/Runtime/Ops.java \
	Rakudo/Runtime/Parameter.java \
	Rakudo/Runtime/Signature.java \
	Rakudo/Runtime/ThreadContext.java

# Java source code files "depend" on files in the dotnet/* directories,
# in the sense that they were manually translated.
# The following definitions cause make to emit 'todo:' messages if a
# dotnet file becomes newer than a java file.
# The way to clear the error is to compare the files in question, and
# then to refresh the java file to be newer than the dotnet one.
Rakudo/Init.java: ../../dotnet/runtime/Init.cs
	@echo "todo: $@ is older than $<"

Rakudo/Metamodel/Hints.java: ../../dotnet/runtime/Metamodel/Representation.cs
	@echo "todo: Rakudo/Metamodel/Hints.java is older than ../../dotnet/runtime/Metamodel/Representation.cs"
	# There is no Hints.cs file, the Hints class is in Representation.cs.

Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.java: ../../dotnet/runtime/Metamodel/KnowHOW/KnowHOWBootstrapper.cs
	@echo "todo: $@ is older than $<"

Rakudo/Metamodel/RakudoObject.java: ../../dotnet/runtime/Metamodel/RakudoObject.cs
	@echo "todo: Rakudo/Metamodel/RakudoObject.java is older than ../../dotnet/runtime/Metamodel/RakudoObject.cs"

Rakudo/Metamodel/Representation.java: ../../dotnet/runtime/Metamodel/Representation.cs
	@echo "todo: Rakudo/Metamodel/Representation.java is older than ../../dotnet/runtime/Metamodel/Representation.cs"

Rakudo/Metamodel/Representations/P6capture.java: ../../dotnet/runtime/Metamodel/Representations/P6capture.cs
	@echo "todo: $@ is older than $<"

Rakudo/Metamodel/Representations/P6hash.java: ../../dotnet/runtime/Metamodel/Representations/P6hash.cs
	@echo "todo: $@ is older than $<"

Rakudo/Metamodel/Representations/P6int.java: ../../dotnet/runtime/Metamodel/Representations/P6int.cs
	@echo "todo: $@ is older than $<"

Rakudo/Metamodel/Representations/P6list.java: ../../dotnet/runtime/Metamodel/Representations/P6list.cs
	@echo "todo: $@ is older than $<"

Rakudo/Metamodel/Representations/P6num.java: ../../dotnet/runtime/Metamodel/Representations/P6num.cs
	@echo "todo: $@ is older than $<"

Rakudo/Metamodel/Representations/P6opaque.java: ../../dotnet/runtime/Metamodel/Representations/P6opaque.cs
	@echo "todo: $@ is older than $<"

Rakudo/Metamodel/Representations/P6str.java: ../../dotnet/runtime/Metamodel/Representations/P6str.cs
	@echo "todo: $@ is older than $<"

Rakudo/Metamodel/Representations/RakudoCodeRef.java: ../../dotnet/runtime/Metamodel/Representations/RakudoCodeRef.cs
	@echo "todo: $@ is older than $<"

Rakudo/Metamodel/SharedTable.java: ../../dotnet/runtime/Metamodel/SharedTable.cs
	@echo "todo: Rakudo/Metamodel/SharedTable.java is older than ../../dotnet/runtime/Metamodel/SharedTable.cs"

Rakudo/Runtime/CaptureHelper.java: ../../dotnet/runtime/Runtime/CaptureHelper.cs
	@echo "todo: Rakudo/Runtime/CaptureHelper.java is older than ../../dotnet/runtime/Runtime/Signatures/CaptureHelper.cs"

Rakudo/Runtime/CodeObjectUtility.java: ../../dotnet/runtime/Runtime/CodeObjectUtility.cs
	@echo "todo: $@ is older than $<"

Rakudo/Runtime/Context.java: ../../dotnet/runtime/Runtime/Context.cs
	@echo "todo: Rakudo/Runtime/Context.java is older than ../../dotnet/runtime/Runtime/Context.cs"

Rakudo/Runtime/ExecutionDomain.java: ../../dotnet/runtime/Runtime/ExecutionDomain.cs
	@echo "todo: Rakudo/Runtime/ExecutionDomain.java is older than ../../dotnet/runtime/Runtime/ExecutionDomain.cs"

Rakudo/Runtime/Lexpad.java: ../../dotnet/runtime/Runtime/Lexpad.cs
	@echo "todo: Rakudo/Runtime/Lexpad.java is older than ../../dotnet/runtime/Runtime/Signatures/Lexpad.cs"

Rakudo/Runtime/MultiDispatch/LexicalCandidateFinder.java: ../../dotnet/runtime/Runtime/MultiDispatch/LexicalCandidateFinder.cs
	@echo "todo: Rakudo/Runtime/MultiDispatch/LexicalCandidateFinder.java is older than ../../dotnet/runtime/Runtime/MultiDispatch/LexicalCandidateFinder.cs"

Rakudo/Runtime/MultiDispatch/MultiDispatcher.java: ../../dotnet/runtime/Runtime/MultiDispatch/MultiDispatcher.cs
	@echo "todo: Rakudo/Runtime/MultiDispatch/MultiDispatcher.java is older than ../../dotnet/runtime/Runtime/MultiDispatch/MultiDispatcher.cs"

Rakudo/Runtime/Ops.java: ../../dotnet/runtime/Runtime/Ops.cs
	@echo "todo: Rakudo/Runtime/Ops.java is older than ../../dotnet/runtime/Runtime/Ops.cs"

Rakudo/Runtime/Parameter.java: ../../dotnet/runtime/Runtime/Signatures/Parameter.cs
	@echo "todo: Rakudo/Runtime/Parameter.java is older than ../../dotnet/runtime/Runtime/Signatures/Parameter.cs"

Rakudo/Runtime/Signature.java: ../../dotnet/runtime/Runtime/Signatures/Signature.cs
	@echo "todo: Rakudo/Runtime/Signature.java is older than ../../dotnet/runtime/Metamodel/Signatures/Signature.cs"

Rakudo/Runtime/ThreadContext.java: ../../dotnet/runtime/Runtime/ThreadContext.cs
	@echo "todo: Rakudo/Runtime/ThreadContext.java is older than ../../dotnet/runtime/Runtime/ThreadContext.cs"

Rakudo/Serialization/SerializationContext.java: ../../dotnet/runtime/Serialization/SerializationContext.cs
	@echo "todo: Rakudo/Serialization/SerializationContext.java is older than ../../dotnet/runtime/Serialization/SerializationContext.cs"

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
