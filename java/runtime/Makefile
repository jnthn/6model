# Makefile for 6model/java/runtime

# Dependency critical paths (1 2 3 and 4 are circular)
# (partial diagram - less significant (eg implicit transitive) dependencies
#  omitted for simplicity)
#
#                                           Hints
#                                             |
#                                            SerializationContext
#                                             |                |
#        +------------------------> RakudoObject               |
#        |                           |  |     |                |
#        |                   Parameter Lexpad |                |
#        |                         |    |     |                |
#        |                   Signature  |   Representation     |
#        |                         |    |    |      |    |     |
#        |  +--+----------------> RakudoCodeRef     |    |     |
#        |  |  |                           |        |    |     |
#        1  2  3         ExecutionDomain  Context   |    |     |
#        |  |  |                      |      |      |    |     |
#        |  |  +--------------------- ThreadContext |    |     |
#        |  |                                    |  |    |     |
#        |  |              P6capture             |  |    |     |
#        |  |               |     |              |  |    |     |
#        |  |  MultiDispatcher  SignatureBinder  |  |    |     |
#        |  |             |                      |  |    |     |
#        |  |  +-------> Ops  P6hash             |  |    |     |
#        |  |  |          |   P6int              |  |    |     |
#        |  |  4         P6opaque       IFindMethod |  REPRRegistry
#        |  |  |                |             |     |      |
#        |  |  |               CaptureHelper  |     |      |
#        |  |  |                          |   |     |      |
#        +--+--+------------------------- SharedTable      |
#                                             |            |
#                                        KnowHOWREPR  CodeObjectUtility
#                                             |         |
#                                        KnowHOWBootstrapper
#                                                  |
#                                                  | P6num
#                                                  | P6str
#                                                  | |
#                                                 Init
#

CLASSES = classes/

ALL_BUILD_TARGETS = \
	$(CLASSES)Rakudo/Metamodel/Hints.class \
	$(CLASSES)Rakudo/Serialization/SerializationContext.class \
	$(CLASSES)Rakudo/Runtime/ExecutionDomain.class \
	$(CLASSES)Rakudo/Metamodel/RakudoObject.class \
	$(CLASSES)Rakudo/Metamodel/REPRRegistry.class \
	$(CLASSES)Rakudo/Metamodel/Representations/P6hash.class \
	$(CLASSES)Rakudo/Metamodel/Representations/P6int.class \
	$(CLASSES)Rakudo/Metamodel/Representations/P6num.class \
	$(CLASSES)Rakudo/Metamodel/Representations/P6str.class \
	$(CLASSES)Rakudo/Metamodel/Representations/P6opaque.class \
	$(CLASSES)Rakudo/Runtime/CodeObjectUtility.class \
	$(CLASSES)Rakudo/Runtime/SignatureBinder.class \
	$(CLASSES)Rakudo/Metamodel/KnowHOW/KnowHOWREPR.class \
	$(CLASSES)Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.class \
	$(CLASSES)Rakudo/Init.class

OTHER_DEPENDENT_TARGETS = \
	$(CLASSES)Rakudo/Runtime/CaptureHelper.class \
	$(CLASSES)Rakudo/Runtime/Lexpad.class \
	$(CLASSES)Rakudo/Runtime/Parameter.class \
	$(CLASSES)Rakudo/Runtime/Signature.class \
	$(CLASSES)Rakudo/Metamodel/Representation.class \
	$(CLASSES)Rakudo/Metamodel/Representations/P6capture.class \
	$(CLASSES)Rakudo/Metamodel/Representations/P6list.class \
	$(CLASSES)Rakudo/Metamodel/Representations/RakudoCodeRef.class \
	$(CLASSES)Rakudo/Runtime/Context.class \
	$(CLASSES)Rakudo/Runtime/MultiDispatch/LexicalCandidateFinder.class \
	$(CLASSES)Rakudo/Runtime/MultiDispatch/MultiDispatcher.class \
	$(CLASSES)Rakudo/Runtime/Ops.class \
	$(CLASSES)Rakudo/Runtime/ThreadContext.class \
	$(CLASSES)Rakudo/Metamodel/IFindMethod.class \
	$(CLASSES)Rakudo/Metamodel/SharedTable.class

INNER_CLASSES = \
	$(CLASSES)Rakudo/Metamodel/Representations/RakudoCodeRef$$Instance.class

JAVAC = javac -d $(CLASSES) -classpath $(CLASSES)

all: $(CLASSES) RakudoRuntime.jar

$(CLASSES):
	perl -MExtUtils::Command -e mkpath $(CLASSES)

RakudoRuntime.jar: $(ALL_BUILD_TARGETS) $(OTHER_DEPENDENT_TARGETS) $(INNER_CLASSES)
	jar cf RakudoRuntime.jar -C $(CLASSES) .
# For troubleshooting, use `jar cvf RakudoRuntime.jar` instead.
# Use `jar tvf RakudoRuntime.jar` to tell what was done.

$(CLASSES)Rakudo/Metamodel/Hints.class: Rakudo/Metamodel/Hints.java
	$(JAVAC) Rakudo/Metamodel/Hints.java

$(CLASSES)Rakudo/Serialization/SerializationContext.class: Rakudo/Serialization/SerializationContext.java
	$(JAVAC) Rakudo/Serialization/SerializationContext.java

$(CLASSES)Rakudo/Runtime/ExecutionDomain.class: Rakudo/Runtime/ExecutionDomain.java
	$(JAVAC) Rakudo/Runtime/ExecutionDomain.java

# There is a long circular dependency from RakudoObject to SharedTable.
# Handle it by compiling all the interdependent files in a single step.
# Thanks jnthn++ for the suggestion.
$(CLASSES)Rakudo/Metamodel/RakudoObject.class: Rakudo/Metamodel/RakudoObject.java \
		Rakudo/Serialization/SerializationContext.java \
		Rakudo/Metamodel/Representation.java \
		Rakudo/Runtime/CaptureHelper.java \
		Rakudo/Runtime/Lexpad.java \
		Rakudo/Runtime/Parameter.java \
		Rakudo/Runtime/Signature.java \
		Rakudo/Metamodel/Representations/P6capture.java \
		Rakudo/Metamodel/Representations/P6list.java \
		Rakudo/Metamodel/Representations/RakudoCodeRef.java \
		Rakudo/Metamodel/REPRRegistry.java \
		Rakudo/Runtime/Context.java \
		Rakudo/Runtime/MultiDispatch/LexicalCandidateFinder.java \
		Rakudo/Runtime/MultiDispatch/MultiDispatcher.java \
		Rakudo/Runtime/Ops.java \
		Rakudo/Runtime/ThreadContext.java \
		Rakudo/Metamodel/IFindMethod.java \
		Rakudo/Metamodel/SharedTable.java
	$(JAVAC) \
		Rakudo/Metamodel/RakudoObject.java \
		Rakudo/Metamodel/Representation.java \
		Rakudo/Runtime/CaptureHelper.java \
		Rakudo/Runtime/Lexpad.java \
		Rakudo/Runtime/Parameter.java \
		Rakudo/Runtime/Signature.java \
		Rakudo/Metamodel/Representations/P6capture.java \
		Rakudo/Metamodel/Representations/P6list.java \
		Rakudo/Metamodel/Representations/RakudoCodeRef.java \
		Rakudo/Metamodel/REPRRegistry.java \
		Rakudo/Runtime/Context.java \
		Rakudo/Runtime/MultiDispatch/LexicalCandidateFinder.java \
		Rakudo/Runtime/MultiDispatch/MultiDispatcher.java \
		Rakudo/Runtime/Ops.java \
		Rakudo/Runtime/ThreadContext.java \
		Rakudo/Metamodel/IFindMethod.java \
		Rakudo/Metamodel/SharedTable.java

$(CLASSES)Rakudo/Metamodel/KnowHOW/KnowHOWREPR.class: Rakudo/Metamodel/KnowHOW/KnowHOWREPR.java \
		$(CLASSES)Rakudo/Metamodel/Hints.class
	$(JAVAC) Rakudo/Metamodel/KnowHOW/KnowHOWREPR.java

$(CLASSES)Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.class: Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.java \
		$(CLASSES)Rakudo/Metamodel/IFindMethod.class \
		$(CLASSES)Rakudo/Metamodel/KnowHOW/KnowHOWREPR.class \
		$(CLASSES)Rakudo/Metamodel/RakudoObject.class \
		$(CLASSES)Rakudo/Metamodel/Representation.class \
		$(CLASSES)Rakudo/Metamodel/Representations/P6capture.class \
		$(CLASSES)Rakudo/Metamodel/Representations/P6list.class \
		$(CLASSES)Rakudo/Metamodel/Representations/RakudoCodeRef.class \
		$(CLASSES)Rakudo/Metamodel/REPRRegistry.class \
		$(CLASSES)Rakudo/Metamodel/SharedTable.class \
		$(CLASSES)Rakudo/Runtime/CodeObjectUtility.class \
		$(CLASSES)Rakudo/Runtime/Ops.class \
		$(CLASSES)Rakudo/Runtime/ThreadContext.class
	$(JAVAC) Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.java

$(CLASSES)Rakudo/Metamodel/Representations/P6capture.class: Rakudo/Metamodel/Representations/P6capture.java \
		$(CLASSES)Rakudo/Metamodel/Hints.class \
		$(CLASSES)Rakudo/Metamodel/RakudoObject.class \
		$(CLASSES)Rakudo/Metamodel/Representation.class \
		$(CLASSES)Rakudo/Metamodel/SharedTable.class \
		$(CLASSES)Rakudo/Serialization/SerializationContext.class
	$(JAVAC) Rakudo/Metamodel/Representations/P6capture.java

$(CLASSES)Rakudo/Metamodel/Representations/P6hash.class: Rakudo/Metamodel/Representations/P6hash.java \
		$(CLASSES)Rakudo/Metamodel/Hints.class \
		$(CLASSES)Rakudo/Metamodel/RakudoObject.class \
		$(CLASSES)Rakudo/Metamodel/Representation.class \
		$(CLASSES)Rakudo/Metamodel/SharedTable.class \
		$(CLASSES)Rakudo/Serialization/SerializationContext.class
	$(JAVAC) Rakudo/Metamodel/Representations/P6hash.java

$(CLASSES)Rakudo/Metamodel/Representations/P6int.class: Rakudo/Metamodel/Representations/P6int.java \
		$(CLASSES)Rakudo/Metamodel/Hints.class \
		$(CLASSES)Rakudo/Metamodel/RakudoObject.class \
		$(CLASSES)Rakudo/Metamodel/Representation.class \
		$(CLASSES)Rakudo/Metamodel/SharedTable.class \
		$(CLASSES)Rakudo/Serialization/SerializationContext.class
	$(JAVAC) Rakudo/Metamodel/Representations/P6int.java

$(CLASSES)Rakudo/Metamodel/Representations/P6num.class: Rakudo/Metamodel/Representations/P6num.java \
		$(CLASSES)Rakudo/Metamodel/Hints.class \
		$(CLASSES)Rakudo/Metamodel/RakudoObject.class \
		$(CLASSES)Rakudo/Metamodel/Representation.class \
		$(CLASSES)Rakudo/Metamodel/SharedTable.class \
		$(CLASSES)Rakudo/Runtime/ThreadContext.class
	$(JAVAC) Rakudo/Metamodel/Representations/P6num.java

$(CLASSES)Rakudo/Metamodel/Representations/P6str.class: Rakudo/Metamodel/Representations/P6str.java \
		$(CLASSES)Rakudo/Metamodel/Hints.class \
		$(CLASSES)Rakudo/Metamodel/RakudoObject.class \
		$(CLASSES)Rakudo/Metamodel/Representation.class \
		$(CLASSES)Rakudo/Metamodel/SharedTable.class \
		$(CLASSES)Rakudo/Runtime/ThreadContext.class
	$(JAVAC) Rakudo/Metamodel/Representations/P6str.java

$(CLASSES)Rakudo/Metamodel/Representations/P6opaque.class: Rakudo/Metamodel/Representations/P6opaque.java \
		$(CLASSES)Rakudo/Metamodel/Hints.class \
		$(CLASSES)Rakudo/Metamodel/RakudoObject.class \
		$(CLASSES)Rakudo/Metamodel/Representation.class \
		$(CLASSES)Rakudo/Metamodel/SharedTable.class \
		$(CLASSES)Rakudo/Runtime/CaptureHelper.class \
		$(CLASSES)Rakudo/Runtime/Ops.class \
		$(CLASSES)Rakudo/Runtime/ThreadContext.class \
		$(CLASSES)Rakudo/Serialization/SerializationContext.class
	$(JAVAC) Rakudo/Metamodel/Representations/P6opaque.java

$(CLASSES)Rakudo/Runtime/CodeObjectUtility.class: Rakudo/Runtime/CodeObjectUtility.java \
		$(CLASSES)Rakudo/Metamodel/RakudoObject.class \
		$(CLASSES)Rakudo/Metamodel/REPRRegistry.class \
		$(CLASSES)Rakudo/Metamodel/Representation.class \
		$(CLASSES)Rakudo/Metamodel/Representations/RakudoCodeRef.class
	$(JAVAC) Rakudo/Runtime/CodeObjectUtility.java

$(CLASSES)Rakudo/Runtime/SignatureBinder.class: Rakudo/Runtime/SignatureBinder.java \
		$(CLASSES)Rakudo/Metamodel/RakudoObject.class \
		$(CLASSES)Rakudo/Metamodel/Representations/P6capture.class \
		$(CLASSES)Rakudo/Runtime/Context.class
	$(JAVAC) Rakudo/Runtime/SignatureBinder.java

$(CLASSES)Rakudo/Init.class: Rakudo/Init.java \
		$(CLASSES)Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.class \
		$(CLASSES)Rakudo/Metamodel/KnowHOW/KnowHOWREPR.class \
		$(CLASSES)Rakudo/Metamodel/RakudoObject.class \
		$(CLASSES)Rakudo/Metamodel/Representations/RakudoCodeRef.class \
		$(CLASSES)Rakudo/Metamodel/Representations/P6capture.class \
		$(CLASSES)Rakudo/Metamodel/Representations/P6hash.class \
		$(CLASSES)Rakudo/Metamodel/Representations/P6int.class \
		$(CLASSES)Rakudo/Metamodel/Representations/P6list.class \
		$(CLASSES)Rakudo/Metamodel/Representations/P6opaque.class \
		$(CLASSES)Rakudo/Metamodel/REPRRegistry.class \
		$(CLASSES)Rakudo/Runtime/CaptureHelper.class \
		$(CLASSES)Rakudo/Runtime/Context.class \
		$(CLASSES)Rakudo/Runtime/ExecutionDomain.class \
		$(CLASSES)Rakudo/Runtime/Lexpad.class \
		$(CLASSES)Rakudo/Runtime/ThreadContext.class
	$(JAVAC) Rakudo/Init.java

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

Rakudo/Runtime/SignatureBinder.java: ../../dotnet/runtime/Runtime/Signatures/SignatureBinder.cs
	@echo "todo: $@ is older than $<"

Rakudo/Runtime/ThreadContext.java: ../../dotnet/runtime/Runtime/ThreadContext.cs
	@echo "todo: Rakudo/Runtime/ThreadContext.java is older than ../../dotnet/runtime/Runtime/ThreadContext.cs"

Rakudo/Serialization/SerializationContext.java: ../../dotnet/runtime/Serialization/SerializationContext.cs
	@echo "todo: Rakudo/Serialization/SerializationContext.java is older than ../../dotnet/runtime/Serialization/SerializationContext.cs"

# To show java files that are older than their dotnet equivalents, run:
# make todolist
todolist: $(TRANSLATED_SOURCE_FILES)

clean:
	perl -MExtUtils::Command -e rm_f RakudoRuntime.jar \
		$(ALL_BUILD_TARGETS) $(OTHER_DEPENDENT_TARGETS)
	perl -MExtUtils::Command -e rm_rf $(CLASSES)

help:
	@echo "In 6model/java/runtime you can make the following:"
	@echo "all       (default) compile everything"
	@echo "clean     remove compiled class files"
	@echo "todolist  show files where java version is older than dotnet"
	@echo "help      this summary"
