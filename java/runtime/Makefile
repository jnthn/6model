# Makefile for 6model/java/runtime

ALL_CLASSES = \
	Rakudo/Metamodel/Hints.class \
	Rakudo/Metamodel/REPRRegistry.class \
	Rakudo/Serialization/SerializationContext.class \
	Rakudo/Metamodel/RakudoObject.class \
	Rakudo/Metamodel/Representation.class \
	Rakudo/Metamodel/SharedTable.class \
	Rakudo/Metamodel/KnowHOW/KnowHOWREPR.class

all: $(ALL_CLASSES)

# compiles ok: (listed in dependency order)
Rakudo/Metamodel/Hints.class: Rakudo/Metamodel/Hints.java
	javac Rakudo/Metamodel/Hints.java

#Rakudo/Metamodel/IBoxableRepresentation.class: Rakudo/Metamodel/IBoxableRepresentation.java
#	javac Rakudo/Metamodel/IBoxableRepresentation.java

Rakudo/Metamodel/REPRRegistry.class: Rakudo/Metamodel/REPRRegistry.java
	javac Rakudo/Metamodel/REPRRegistry.java

Rakudo/Serialization/SerializationContext.class: Rakudo/Serialization/SerializationContext.java
	javac Rakudo/Serialization/SerializationContext.java

Rakudo/Metamodel/RakudoObject.class: Rakudo/Metamodel/RakudoObject.java \
		Rakudo/Serialization/SerializationContext.class
	javac Rakudo/Metamodel/RakudoObject.java

Rakudo/Metamodel/Representation.class: Rakudo/Metamodel/Representation.java \
		Rakudo/Metamodel/RakudoObject.class
	javac Rakudo/Metamodel/Representation.java

Rakudo/Metamodel/SharedTable.class: Rakudo/Metamodel/SharedTable.java \
		Rakudo/Metamodel/Representation.class
	javac Rakudo/Metamodel/SharedTable.java
    # this file has the lambda expression code commented out, and needs
    # to be completed urgently

Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.class: Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.java \
	javac Rakudo/Metamodel/KnowHOW/KnowHOWBootstrapper.java
	# this file has 6 lambda expressions to translate, currently suffering
	# from 55 compile time errors :(

Rakudo/Metamodel/KnowHOW/KnowHOWREPR.class: Rakudo/Metamodel/KnowHOW/KnowHOWREPR.java \
		Rakudo/Metamodel/Hints.class
	javac Rakudo/Metamodel/KnowHOW/KnowHOWREPR.java

clean:
	perl -MExtUtils::Command -e rm_f $(ALL_CLASSES)
