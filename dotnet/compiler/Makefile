# This works provided you have a parrot-nqp and csc somewhere in your path.

all: gen_actions.pir gen_grammar.pir gen_dnst.pir gen_past2dnst.pir gen_dnst2csharp.pir NQPSetting.dll

gen_actions.pir: Actions.pm
	parrot-nqp --target=pir Actions.pm > gen_actions.pir

gen_grammar.pir: Grammar.pm
	parrot-nqp --target=pir Grammar.pm > gen_grammar.pir

gen_dnst.pir: DNST.pm
	parrot-nqp --target=pir DNST.pm > gen_dnst.pir

gen_past2dnst.pir: PAST2DNSTCompiler.pm
	parrot-nqp --target=pir PAST2DNSTCompiler.pm > gen_past2dnst.pir

gen_dnst2csharp.pir: DNST2CSharp.pm
	parrot-nqp --target=pir DNST2CSharp.pm > gen_dnst2csharp.pir

NQPSetting.dll: ../../common/NQP/NQPSetting.pm  gen_actions.pir gen_grammar.pir gen_dnst.pir gen_past2dnst.pir gen_dnst2csharp.pir
	parrot compile.pir ../../common/NQP/NQPSetting.pm --setting > NQPSetting.cs
	csc /target:library /out:NQPSetting.dll /reference:RakudoRuntime.dll NQPSetting.cs

clean:
	perl -MExtUtils::Command -e rm_f gen_*.pir *.dll *.pdb *.exe *.cs
