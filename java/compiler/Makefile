# This works if you have parrot, parrot-nqp and javac in your path.

CLASSES_DIR = classes/

JAVAC = javac -d $(CLASSES_DIR)

# TODO add P6Objects.jar
all: $(CLASSES_DIR) gen_actions.pir gen_grammar.pir gen_jst.pir \
    	gen_past2jst.pir gen_jst2java.pir gen_jst2jasm.pir NQPSetting.jar

$(CLASSES_DIR):
	perl -MExtUtils::Command -e mkpath $(CLASSES_DIR)

gen_actions.pir: Actions.pm
	parrot-nqp --target=pir Actions.pm > gen_actions.pir

gen_grammar.pir: Grammar.pm
	parrot-nqp --target=pir Grammar.pm > gen_grammar.pir

gen_past2jst.pir: PAST2JSTCompiler.pm
	parrot-nqp --target=pir PAST2JSTCompiler.pm > gen_past2jst.pir

gen_nqpoptimizer.pir: ../../dotnet/compiler/NQPOptimizer.pm
	parrot-nqp --target=pir ../../dotnet/compiler/NQPOptimizer.pm > gen_nqpoptimizer.pir

gen_jst.pir: JST.pm
	parrot-nqp --target=pir JST.pm > gen_jst.pir

gen_jst2java.pir: JST2Java.pm
	parrot-nqp --target=pir JST2Java.pm > gen_jst2java.pir

gen_jst2jasm.pir: JST2JASM.pm
	parrot-nqp --target=pir JST2JASM.pm > gen_jst2jasm.pir

NQPSetting.java: NQPSetting.pm \
		compile.pir gen_actions.pir gen_grammar.pir gen_nqpoptimizer.pir gen_jst.pir gen_past2jst.pir gen_jst2java.pir
	parrot compile.pir NQPSetting.pm --setting > NQPSetting.java

NQPSetting.jar: NQPSetting.java
	$(JAVAC) -classpath ../runtime/RakudoRuntime.jar NQPSetting.java
	jar cf NQPSetting.jar -C $(CLASSES_DIR) .

P6Objects.java: ../../common/NQP/P6Objects.pm NQPSetting.jar \
		compile.pir gen_actions.pir gen_grammar.pir gen_nqpoptimizer.pir gen_jst.pir gen_past2jst.pir gen_jst2java.pir
	parrot compile.pir ../../common/NQP/P6Objects.pm --no-core-libs > P6Objects.java

P6Objects.jar: P6Objects.java
	$(JAVAC) P6Objects.java
	jar cf P6Objects.jar P6Objects.class

test: all
	prove -r --exec try.bat ..\..\t\nqp

test-unixy: all
	prove -r --exec ./try.sh ../../t/nqp

clean:
	perl -MExtUtils::Command -e rm_f gen_*.pir *.class *.jar *.java
	perl -MExtUtils::Command -e rm_rf $(CLASSES_DIR)

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


