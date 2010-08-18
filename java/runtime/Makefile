# Makefile for 6model/java/runtime

all: Rakudo/Metamodel/SharedTable.class

# compiles ok: (listed in dependency order)
Hints.class: Hints.java
	javac Hints.java

Rakudo/Metamodel/IBoxableRepresentation.class: Rakudo/Metamodel/IBoxableRepresentation.java
	javac Rakudo/Metamodel/IBoxableRepresentation.java

Rakudo/Metamodel/REPRRegistry.class: Rakudo/Metamodel/REPRRegistry.java
	javac Rakudo/Metamodel/REPRRegistry.java

Rakudo/Serialization/SerializationContext.class: Rakudo/Serialization/SerializationContext.java \
		Hints.class
	javac Rakudo/Serialization/SerializationContext.java

Rakudo/Metamodel/IRakudoObject.class: Rakudo/Metamodel/IRakudoObject.java \
		Rakudo/Serialization/SerializationContext.class
	javac Rakudo/Metamodel/IRakudoObject.java

Rakudo/Metamodel/IRepresentation.class: Rakudo/Metamodel/IRepresentation.java \
		Rakudo/Metamodel/IRakudoObject.class
	javac Rakudo/Metamodel/IRepresentation.java

Rakudo/Metamodel/SharedTable.class: Rakudo/Metamodel/SharedTable.java \
		Rakudo/Metamodel/IRepresentation.class
	javac Rakudo/Metamodel/SharedTable.java
    # this file has the lambda expression code commented out, and needs
    # to be completed urgently

clean:
	perl -MExtUtils::Command -e rm_rf '*~'
