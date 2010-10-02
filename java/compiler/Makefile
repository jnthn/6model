# This works if you have parrot, parrot-nqp and javac in your path.

CLASSES = classes/

JAVAC = javac -d $(CLASSES)

all: $(CLASSES) gen_actions.pir gen_grammar.pir gen_jst.pir gen_past2jst.pir gen_jst2java.pir NQPSetting.jar

$(CLASSES):
	perl -MExtUtils::Command -e mkpath $(CLASSES)

gen_actions.pir: Actions.pm
	parrot-nqp --target=pir Actions.pm > gen_actions.pir

gen_grammar.pir: Grammar.pm
	parrot-nqp --target=pir Grammar.pm > gen_grammar.pir

gen_past2jst.pir: PAST2JSTCompiler.pm
	parrot-nqp --target=pir PAST2JSTCompiler.pm > gen_past2jst.pir

gen_jst.pir: JST.pm
	parrot-nqp --target=pir JST.pm > gen_jst.pir

gen_jst2java.pir: JST2Java.pm
	parrot-nqp --target=pir JST2Java.pm > gen_jst2java.pir

NQPSetting.jar: ../../common/NQP/NQPSetting.pm \
		gen_actions.pir \
		gen_grammar.pir \
		gen_jst.pir \
		gen_past2jst.pir \
		gen_jst2java.pir
	parrot compile.pir ../../common/NQP/NQPSetting.pm --setting > gen_nqpsetting.java
	$(JAVAC) -classpath ../runtime/RakudoRuntime.jar gen_nqpsetting.java
	jar cf NQPSetting.jar -C $(CLASSES) .

test: all
	prove -r --exec try.sh ..\..\t\nqp

clean:
	perl -MExtUtils::Command -e rm_f gen_*.pir *.class *.jar *.java
	perl -MExtUtils::Command -e rm_rf $(CLASSES)

# Java source code files "depend" on files in the dotnet/* directories,
# in the sense that they were manually translated.
# The following definitions cause make to emit 'todo:' messages if a
# dotnet file becomes newer than a java file.
# The way to clear the error is to compare the files in question, and
# then to refresh the java file to be newer than the dotnet one.
Actions.pm: ../../dotnet/compiler/Actions.pm
	@echo "todo: $@ is older than $<"

JST.pm: ../../dotnet/compiler/DNST.pm
	@echo "todo: $@ is older than $<"

JST2Java.pm: ../../dotnet/compiler/DNST2CSharp.pm
	@echo "todo: $@ is older than $<"

PAST2JSTCompiler.pm: ../../dotnet/compiler/PAST2DNSTCompiler.pm
	@echo "todo: $@ is older than $<"


