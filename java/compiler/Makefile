# This works if you have parrot-nqp, parrot and javac in your path.

all: gen_actions.pir gen_grammar.pir gen_jst.pir gen_past2jst.pir gen_jst2java.pir NQPSetting.class

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

NQPSetting.class: ../../common/NQP/NQPSetting.pm \
		gen_actions.pir \
		gen_grammar.pir \
		gen_jst.pir \
		gen_past2jst.pir \
		gen_jst2java.pir
	parrot compile.pir ../../common/NQP/NQPSetting.pm --setting > NQPSetting.java
	javac -classpath ../runtime/RakudoRuntime.jar NQPSetting.java

test: all
	prove -r --exec try.sh ..\..\t\nqp

clean:
	perl -MExtUtils::Command -e rm_f gen_*.pir *.class *.jar *.java
