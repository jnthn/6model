# This works provided you (1) have a parrot-nqp somewhere in your path and
# (2) are on Windows. :-)

all: gen_actions.pir gen_grammar.pir gen_dnst.pir gen_past2dnst.pir gen_dnst2csharp.pir

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

clean:
	del gen_*.pir *.dll *.pdb *.exe *.cs
