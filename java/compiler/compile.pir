.sub '' :anon :load :init
    load_bytecode 'HLL.pbc'
    load_bytecode 'P6Regex.pbc'
.end

.include 'gen_grammar.pir'
.include 'gen_actions.pir'
.include 'gen_jst.pir'
.include 'gen_past2jst.pir'
.include 'gen_jst2java.pir'
.loadlib 'io_ops'

.sub 'main' :main
    .param pmc args

    # Do we have an argument saying we're compiling the setting?
    $P0 = new ['Integer']
    .lex '$*COMPILING_NQP_SETTING', $P0
    $S0 = args[2]
    if $S0 != '--setting' goto not_setting
    $P0 = 1
  not_setting:

    .local pmc g, a, pastcomp, jstcomp
    g = get_hll_global ['JnthnNQP'], 'Grammar'
    a = get_hll_global ['JnthnNQP'], 'Actions'
    pastcomp = get_hll_global 'PAST2JSTCompiler'
    jstcomp = get_hll_global 'JST2JavaCompiler'

    .local string filename, file
    .local pmc fh
    filename = args[1]
    fh = open filename, 'r'
    fh.'encoding'('utf8')
    file = fh.'readall'()
    fh.'close'()

    .local pmc match, ast, jst, compiled
    match = g.'parse'(file, 'actions'=>a)
    ast = match.'ast'()
    jst = pastcomp.'compile'(ast)
    compiled = jstcomp.'compile'(jst)
    say compiled
.end
