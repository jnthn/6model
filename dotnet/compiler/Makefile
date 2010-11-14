# This works provided you have a parrot-nqp and csc somewhere in your path.

all: RakudoRuntime.dll gen_actions.pir gen_grammar.pir gen_dnst.pir gen_past2dnst.pir gen_dnst2csharp.pir NQPSetting.dll P6Objects.dll

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

RakudoRuntime.dll:
	msbuild /nologo /verbosity:q ..\runtime\Rakudo.Net.csproj
	perl -MExtUtils::Command -e cp ..\runtime\bin\Debug\RakudoRuntime.dll .
	perl -MExtUtils::Command -e cp ..\runtime\bin\Debug\RakudoRuntime.pdb .

NQPSetting.dll: RakudoRuntime.dll ../../common/NQP/NQPSetting.pm gen_actions.pir gen_grammar.pir gen_dnst.pir gen_past2dnst.pir gen_dnst2csharp.pir compile.pir
	parrot compile.pir ../../common/NQP/NQPSetting.pm --setting > NQPSetting.cs
	csc /nologo /warn:0 /target:library /debug /out:NQPSetting.dll /reference:RakudoRuntime.dll NQPSetting.cs

P6Objects.dll: RakudoRuntime.dll ../../common/NQP/P6Objects.pm NQPSetting.dll
	parrot compile.pir ../../common/NQP/P6Objects.pm --no-core-libs > P6Objects.cs
	csc /nologo /warn:0 /target:library /debug /out:P6Objects.dll /reference:RakudoRuntime.dll P6Objects.cs

test: all
	prove -r --exec try ..\..\t\nqp

test-unixy: all
	prove -r --exec ./try.sh ../../t/nqp

clean:
	perl -MExtUtils::Command -e rm_f gen_*.pir *.dll *.pdb *.exe *.cs
