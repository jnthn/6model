.sub '' :anon :load :init
    load_bytecode 'HLL.pbc'
    load_bytecode 'P6Regex.pbc'
.end

.include 'gen_grammar.pir'
.include 'gen_actions.pir'
.include 'gen_dnst.pir'
.include 'gen_past2dnst.pir'
.include 'gen_dnst2csharp.pir'
.loadlib 'io_ops'

.sub 'main' :main
    .param pmc args
    
    .local pmc g, a, pastcomp, dnstcomp
    g = get_hll_global ['JnthnNQP'], 'Grammar'
    a = get_hll_global ['JnthnNQP'], 'Actions'
    pastcomp = get_hll_global 'PAST2DNSTCompiler'
    dnstcomp = get_hll_global 'DNST2CSharpCompiler'
    
    .local string filename, file
    .local pmc fh
    filename = args[1]
    fh = open filename, 'r'
    file = fh.'readall'()
    fh.'close'()
    
    .local pmc match, ast, dnst, compiled
    match = g.'parse'(file, 'actions'=>a)
    ast = match.'ast'()
    dnst = pastcomp.'compile'(ast)
    compiled = dnstcomp.'compile'(dnst)
    say compiled
.end
