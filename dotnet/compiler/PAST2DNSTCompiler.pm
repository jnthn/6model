# This is the beginings of a PAST to Dotnet Syntax Tree translator. It'll
# no doubt evolve somewhat over time, and get filled out as we support more
# and more of PAST.
class PAST2DNSTCompiler;

# Entry point for the compiler.
method compile(PAST::Node $node) {
    # This tracks the unique IDs we generate in this compilation unit.
    my $*CUR_ID := 0;

    # The nested blocks, flattened out.
    my @*INNER_BLOCKS;

    # Any loadinits we'll need to run, and if we're in one.
    my $*IN_LOADINIT;
    my @*LOADINITS;
    my @*SIGINITS;

    # We'll build a static block info array too; this helps us do so.
    my $*OUTER_SBI := 0;
    my $*SBI_POS := 1;
    my $*SBI_SETUP := DNST::Stmts.new();

    # We'll do similar for values - essentially, this builds a constants
    # table so we don't have to build them again and again.
    my @*CONSTANTS;

    # Also need to track the PAST blocks we're in.
    my @*PAST_BLOCKS;

    # Current namespace path.
    my @*CURRENT_NS;

    # Compile the node; ensure it is an immediate block.
    $node.blocktype('immediate');
    my $main_block_call := dnst_for($node);

    # Build a class node and add the inner code blocks.
    my $class := DNST::Class.new(
        :name($*COMPILING_NQP_SETTING ?? 'NQPSetting' !! unique_name_for_module())
    );
    for @*INNER_BLOCKS {
        $class.push($_);
    }

    # If we're compiling the setting, we'll hack the TryFinally node of the
    # outermost block to *not* restore the caller context, so then we can
    # steal it and use it as the outer for our other stuff.
    if $*COMPILING_NQP_SETTING {
        my $outermost := @*INNER_BLOCKS[0];
        for @($outermost) {
            if $_ ~~ DNST::TryFinally {
                $_.pop();
                $_.push(DNST::Stmts.new());
            }
        }
    }

    # Also need to include setup of static block info.
    $class.push(DNST::Attribute.new( :name('StaticBlockInfo'), :type('RakudoCodeRef.Instance[]') ));
    $class.push(DNST::Attribute.new( :name('ConstantsTable'), :type('RakudoObject[]') ));
    $class.push(make_blocks_init_method('blocks_init'));
    $class.push(make_constants_init_method('constants_init'));

    # Calls to loadinits.
    my $loadinit_calls := DNST::Stmts.new();
    for @*LOADINITS {
        $loadinit_calls.push($_);
    }
    for @*SIGINITS {
        $loadinit_calls.push($_);
    }

    # Finally, startup handling code.
    if $*COMPILING_NQP_SETTING {
        $class.push(DNST::Method.new(
            :name('LoadSetting'),
            :return_type('Context'),
            DNST::Temp.new( :name('TC'), :type('ThreadContext'),
                DNST::MethodCall.new(
                    :on('Rakudo.Init'), :name('Initialize'),
                    :type('ThreadContext'),
                    'null'
                )
            ),
            DNST::Call.new(
                :name('blocks_init'),
                :void(1),
                'TC'
            ),

            # We fudge in a fake NQPStr, for the :repr('P6Str'). Bit hacky,
            # but best I can think of for now. :-)
            DNST::MethodCall.new(
                :on('StaticBlockInfo[1].StaticLexPad'), :name('SetByName'), :void(1), :type('RakudoObject'),
                DNST::Literal.new( :value('NQPStr'), :escape(1) ),
                'REPRRegistry.get_REPR_by_name("P6str").type_object_for(null, null)'
            ),

            # We do the loadinit calls before building the constants, as we
            # may build some constants with types we're yet to define.
            $loadinit_calls,
            DNST::Call.new(
                :name('constants_init'),
                :void(1),
                'TC'
            ),
            $main_block_call,
            "TC.CurrentContext"
        ));
    }
    else {
        # Commonalities for no matter how we start running (be it from the
        # command line or loaded as a library).
        my @params;
        @params.push('ThreadContext TC');
        $class.push(DNST::Method.new(
            :name('Init'),
            :params(@params),
            :return_type('void'),
            DNST::Call.new(
                :name('blocks_init'),
                :void(1),
                'TC'
            ),
            DNST::Call.new(
                :name('constants_init'),
                :void(1),
                'TC'
            ),
            $loadinit_calls
        ));

        # Code for when it's the entry point (e.g. a Main method).
        $class.push(DNST::Method.new(
            :name('Main'),
            :return_type('void'),
            DNST::Temp.new( :name('TC'), :type('ThreadContext'),
                DNST::MethodCall.new(
                    :on('Rakudo.Init'), :name('Initialize'), :type('ThreadContext'),
                    DNST::Literal.new( :value('NQPSetting'), :escape(1) )
                )
            ),
            DNST::Call.new( :name('Init'), :void(1), 'TC' ),
            $main_block_call
        ));

        # Code for when it's being loaded as a library.
        $class.push(DNST::Method.new(
            :name('Load'),
            :params('ThreadContext TC', 'Context Setting'),
            :return_type('RakudoObject'),
            DNST::Call.new( :name('Init'), :void(1), 'TC' ),
            $main_block_call
        ));
    }

    # Package up in a compilation unit with the required "using"s.
    return DNST::CompilationUnit.new(
        DNST::Using.new( :namespace('System') ),
        DNST::Using.new( :namespace('System.Collections.Generic') ),
        DNST::Using.new( :namespace('Rakudo.Metamodel') ),
        DNST::Using.new( :namespace('Rakudo.Metamodel.Representations') ),
        DNST::Using.new( :namespace('Rakudo.Runtime') ),
        DNST::Using.new( :namespace('Rakudo.Runtime.Exceptions') ),
        $class
    );
}

# Creates a not-really-that-unique-yet name for the module (good enough if
# we compile one per second, which given we're cross-compiling, is enough
# for now.)
sub unique_name_for_module() {
    'NQPOutput_' ~ pir::set__IN(pir::time__N())
}

# This makes the block static info initialization sub. One day, this
# can likely go away and we freeze a bunch of this info. But for now,
# this will do.
sub make_blocks_init_method($name) {
    my @params;
    @params.push('ThreadContext TC');
    return DNST::Method.new(
        :name($name),
        :params(@params),
        :return_type('void'),
        
        # Create array for storing these.
        DNST::Bind.new(
            'StaticBlockInfo',
            'new RakudoCodeRef.Instance[' ~ $*SBI_POS ~ ']'
        ),

        # Fake up outermost one for now.
        DNST::Bind.new(
            'StaticBlockInfo[0]',
            DNST::MethodCall.new(
                :on('CodeObjectUtility'), :name('BuildStaticBlockInfo'),
                :type('RakudoCodeRef.Instance'),
                'null', 'null',
                DNST::ArrayLiteral.new( :type('String') )
            )
        ),
        DNST::Bind.new(
            'StaticBlockInfo[0].CurrentContext',
            'TC.Domain.Setting'
        ),

        # The others.
        $*SBI_SETUP
    );
}

# Sets up the constants table initialization method.
sub make_constants_init_method($name) {
    # Build init method.
    my @params;
    @params.push('ThreadContext TC');
    my $result := DNST::Method.new(
        :name($name),
        :params(@params),
        :return_type('void'),

        # Fake up a context with the outer being the main block.
        DNST::Temp.new(
            :name('C'), :type('Context'),
            DNST::New.new(
                :type('Context'),
                DNST::MethodCall.new(
                    :on('CodeObjectUtility'), :name('BuildStaticBlockInfo'),
                    :type('RakudoCodeRef.Instance'),
                    'null',
                    'StaticBlockInfo[1]',
                    DNST::ArrayLiteral.new( :type('string') )
                ),
                'TC.CurrentContext',
                'null'
            )
        ),
        
        # Create array for storing these.
        DNST::Bind.new(
            'ConstantsTable',
            'new RakudoObject[' ~ +@*CONSTANTS ~ ']'
        )
    );

    # Add all constants into table.
    my $i := 0;
    while $i < +@*CONSTANTS {
        $result.push(DNST::Bind.new(
            "ConstantsTable[$i]",
            @*CONSTANTS[$i]
        ));
        $i := $i + 1;
    }

    return $result;
}

# Quick hack so we can get unique (for this compilation) IDs.
sub get_unique_id($prefix) {
    $*CUR_ID := $*CUR_ID + 1;
    return $prefix ~ '_' ~ $*CUR_ID;
}

# Compiles a block.
our multi sub dnst_for(PAST::Block $block) {
    # Unshift this PAST::Block onto the block list.
    @*PAST_BLOCKS.unshift($block);
    
    # We'll collect all the parameter nodes and lexical declarations.
    my @*PARAMS;
    my @*LEXICALS;
    my @*HANDLERS;

    # Update namespace.
    my $prev_ns := @*CURRENT_NS;
    if pir::isa($block.namespace(), 'ResizablePMCArray') {
        @*CURRENT_NS := $block.namespace();
    }
    elsif ~$block.namespace() ne '' {
        @*CURRENT_NS := pir::new('ResizablePMCArray');
        @*CURRENT_NS.push(~$block.namespace());
    }
    
    # Fresh bind context.
    my $*BIND_CONTEXT := 0;

    # Setup static block info.
    my $outer_sbi := $*OUTER_SBI;
    my $our_sbi := $*SBI_POS;
    my $our_sbi_setup := DNST::MethodCall.new(
        :on('CodeObjectUtility'),
        :name('BuildStaticBlockInfo'),
        :type('RakudoCodeRef.Instance')
    );
    $*SBI_POS := $*SBI_POS + 1;
    $*SBI_SETUP.push(DNST::Bind.new(
        "StaticBlockInfo[$our_sbi]",
        $our_sbi_setup
    ));

    # Label the PAST block with its SBI.
    $block<SBI> := "StaticBlockInfo[$our_sbi]";

    # Make start of block.
    my $result := DNST::Method.new(
        :name(get_unique_id('block')),
        :params('ThreadContext TC', 'RakudoObject Block', 'RakudoObject Capture'),
        :return_type('RakudoObject')
    );
    
    # Emit all the statements.
    my @inner_blocks;
    my $stmts := DNST::Stmts.new();
    for @($block) {
        my $*OUTER_SBI := $our_sbi;
        my @*INNER_BLOCKS;
        $stmts.push(dnst_for($_));
        for @*INNER_BLOCKS {
            @inner_blocks.push($_);
        }
    }

    # Handle loadinit. 
    if +@($block.loadinit) {
        my $*OUTER_SBI := $our_sbi;
        my @*INNER_BLOCKS;

        # We'll fake this as an inner block to compile.
        my $*IN_LOADINIT := 1;
        @*LOADINITS.push(dnst_for(PAST::Block.new(
            :blocktype('immediate'), $block.loadinit
        )));

        # Add blocks from this compilation (probably just one,
        # but handle nested blocks from the loadinit).
        for @*INNER_BLOCKS {
            @inner_blocks.push($_);
        }
    }

    # If we have a return handler, add it.
    if $block.control eq 'return_pir' {
        my $*OUTER_SBI := $our_sbi;
        my @*INNER_BLOCKS;
        my %handler;
        %handler<type> := 57;
        %handler<code> := dnst_for(PAST::Block.new(PAST::Stmts.new(
            PAST::Var.new( :name('$!'), :scope('parameter') ),
            DNST::MethodCall.new(
                :on('Ops'), :name('leave_block'),
                'TC',
                DNST::Literal.new( :value('TC.CurrentContext.Outer.StaticCodeObject') ),
                dnst_for(PAST::Var.new( :name('$!'), :scope('lexical') ))
            )
        )));
        $stmts.unshift(%handler<code>); # To get the right lexical context.
        for @*INNER_BLOCKS {
            @inner_blocks.push($_);
        }
        @*HANDLERS.push(%handler);
    }


    # Add signature generation/setup. We need to do this in the
    # correct lexical scope. Also this is handy place to set up
    # the handlers; keep a placeholder for that.
    my $handlers_setup_placeholder := DNST::Stmts.new();
    my $sig_setup_block := get_unique_id('block');
    my @params;
    @params.push('ThreadContext TC');
    @inner_blocks.push(DNST::Method.new(
        :return_type('void'),
        :name($sig_setup_block),
        :params(@params),
        DNST::Temp.new(
            :type('Context'), :name('C'),
            DNST::New.new(
                :type('Context'),
                DNST::MethodCall.new(
                    :on('CodeObjectUtility'), :name('BuildStaticBlockInfo'),
                    :type('RakudoCodeRef.Instance'),
                    'null',
                    "StaticBlockInfo[$our_sbi]",
                    DNST::ArrayLiteral.new( :type('string') ),
                ),
                'TC.CurrentContext',
                'null'
            )
        ),
        DNST::Bind.new( 'TC.CurrentContext', 'C' ),
        DNST::Bind.new(
            "StaticBlockInfo[$our_sbi].Sig",
            compile_signature(@*PARAMS)
        ),
        $handlers_setup_placeholder,
        DNST::Bind.new( 'TC.CurrentContext', 'C.Caller' )
    ));
    @*SIGINITS.push(DNST::Call.new( :name($sig_setup_block), :void(1), 'TC' ));

    # Before start of statements, we want to bind the signature.
    $stmts.unshift(DNST::MethodCall.new(
        :on('SignatureBinder'), :name('Bind'), :void(1), 'TC', 'C', 'Capture'
    ));

    # Wrap in block prelude/postlude.
    $result.push(DNST::Temp.new(
        :name('C'), :type('Context'),
        DNST::New.new( :type('Context'), "Block", "TC.CurrentContext", "Capture" )
    ));
    $result.push(DNST::Bind.new( 'TC.CurrentContext', 'C' ));
    $result.push(DNST::TryFinally.new(
        DNST::TryCatch.new(
            :exception_type('LeaveStackUnwinderException'),
            :exception_var('exc'),
            $stmts,
            DNST::Stmts.new(
                DNST::If.new(
                    DNST::Literal.new(
                        :value("(exc.TargetBlock != Block ? 1 : 0)")
                    ),
                    DNST::Throw.new()
                ),
                "exc.PayLoad"
            )
        ),
        DNST::Bind.new( 'TC.CurrentContext', 'C.Caller' )
    ));
    
    # Add nested inner blocks after it (.Net does not support
    # nested blocks).
    @*INNER_BLOCKS.push($result);
    for @inner_blocks {
        @*INNER_BLOCKS.push($_);
    }

    # Set up body, static outer and lexicals in the code setup block call.
    $our_sbi_setup.push(DNST::New.new(
        :type('Func<ThreadContext, RakudoObject, RakudoObject, RakudoObject>'),
        $result.name
    ));
    $our_sbi_setup.push("StaticBlockInfo[$outer_sbi]");
    my $lex_setup := DNST::ArrayLiteral.new( :type('string') );
    for @*LEXICALS {
        $lex_setup.push(DNST::Literal.new( :value($_), :escape(1) ));
    }
    $our_sbi_setup.push($lex_setup);

    # Add handlers.
    if +@*HANDLERS {
        my $handler_node := DNST::ArrayLiteral.new( :type('Rakudo.Runtime.Exceptions.Handler') );
        for @*HANDLERS {
            $handler_node.push(DNST::New.new(
                :type('Rakudo.Runtime.Exceptions.Handler'),
                DNST::Literal.new( :value($_<type>) ),
                $_<code>
            ));
        }
        $handlers_setup_placeholder.push(DNST::Bind.new(
            "StaticBlockInfo[$our_sbi].Handlers",
            $handler_node
        ));
    }

    # Clear up this PAST::Block from the blocks list and restore outer NS.
    @*PAST_BLOCKS.shift;
    @*CURRENT_NS := $prev_ns;

    # For immediate evaluate to a call; for declaration, evaluate to the
    # low level code object.
    if $block.blocktype eq 'immediate' {
        return DNST::MethodCall.new(
            :name('STable.Invoke'), :type('RakudoObject'),
            "StaticBlockInfo[$our_sbi]",
            'TC',
            "StaticBlockInfo[$our_sbi]",
            DNST::MethodCall.new(
                :on('CaptureHelper'),
                :name('FormWith'),
                :type('RakudoObject')
            )
        );
    }
    else {
        return DNST::MethodCall.new(
            :on('Ops'), :name($block.closure ?? 'new_closure' !! 'capture_outer'),
            :type('RakudoObject'),
            'TC',
            "StaticBlockInfo[$our_sbi]"
        );
    }
}

# Compiles a bunch of parameter nodes down to a signature.
# XXX Doesn't handle default values just yet.
sub compile_signature(@params) {
    # Go through each of the parameters and compile them.
    my $params := DNST::ArrayLiteral.new( :type('Parameter') );
    for @params {
        my $param := DNST::New.new( :type('Parameter') );

        # Type.
        if $_.multitype {
            $param.push(dnst_for($_.multitype));
        }
        else {
            $param.push('null');
        }

        # Variable name to bind into.
        my $lexpad_position := +@*LEXICALS;
        @*LEXICALS.push($_.name);
        $param.push(DNST::Literal.new( :value($_.name), :escape(1) ));
        $param.push(DNST::Literal.new( :value($lexpad_position) ));

        # Named param or not?
        $param.push((!$_.slurpy && $_.named) ??
            DNST::Literal.new( :value(pir::substr($_.name, 1)), :escape(1) ) !!
            'null');

        # Flags.
        $param.push(
            $_.viviself && $_.named ?? 'Parameter.OPTIONAL_FLAG | Parameter.NAMED_FLAG' !!
            $_.viviself             ?? 'Parameter.OPTIONAL_FLAG'                        !!
            $_.slurpy && $_.named   ?? 'Parameter.NAMED_SLURPY_FLAG'                    !!
            $_.slurpy               ?? 'Parameter.POS_SLURPY_FLAG'                      !!
            $_.named                ?? 'Parameter.NAMED_FLAG'                           !!
            'Parameter.POS_FLAG');

        # Definedness constraint.
        $param.push($_<definedness> eq 'D' ?? 'DefinednessConstraint.DefinedOnly' !!
                    $_<definedness> eq 'U' ?? 'DefinednessConstraint.UndefinedOnly' !!
                    'DefinednessConstraint.None');
        
        # viviself.
        $param.push($_.viviself ~~ PAST::Node
            ?? dnst_for(PAST::Block.new(:closure(1), $_.viviself))
            !! 'null');

        $params.push($param);
    }

    # Build up a signature object.
    return DNST::New.new( :type('Signature'), $params );
}

# Compiles a statements node - really just all the stuff in it.
our multi sub dnst_for(PAST::Stmts $stmts) {
    my $result := DNST::Stmts.new();
    for @($stmts) {
        $result.push(dnst_for($_));
    }
    return $result;
}

# Compiles the various forms of PAST::Op.
our multi sub dnst_for(PAST::Op $op) {
    if $op.pasttype eq 'callmethod' {
        # We want to emit code for the args, but also need the
        # invocant to hand specially.
        my @args := @($op);
        if +@args == 0 { pir::die("callmethod node must have at least an invocant"); }
        
        # Invocant.
        my $inv := DNST::Temp.new(
            :name(get_unique_id('inv')), :type('RakudoObject'),
            dnst_for(@args.shift)
        );
        
        # Method name, for indirectly named dotty calls
        my $name := $op.name ~~ PAST::Node
          ?? DNST::MethodCall.new(
                :on('Ops'), :name('unbox_str'), :type('string'), 'TC',
                dnst_for(PAST::Op.new(
                    :pasttype('callmethod'), :name('Str'),
                    dnst_for($op.name)
                )))
          !! DNST::Literal.new( :value($op.name), :escape(1) );
        
        # Method lookup.
        my $callee := DNST::Temp.new(
            :name(get_unique_id('callee')), :type('RakudoObject'),
            DNST::MethodCall.new(
                :on($inv.name), :name('STable.FindMethod'), :type('RakudoObject'),
                'TC',
                $inv.name,
                $name,
                'Hints.NO_HINT'
            )
        );

        # Emit the call.
        return DNST::Stmts.new(
            $inv,
            DNST::MethodCall.new(
                :name('STable.Invoke'), :type('RakudoObject'),
                $callee,
                'TC',
                $callee.name,
                form_capture(@args, $inv)
            )
        );
    }

    elsif $op.pasttype eq 'call' || $op.pasttype eq '' {
        my @args := @($op);
        my $callee;

        # See if we've a name or have to use the first arg as the callee.
        if $op.name ne "" {
            $callee := emit_lexical_lookup($op.name);
        }
        else {
            unless +@args {
                pir::die("PAST::Op call nodes with no name must have at least one child");
            }
            $callee := dnst_for(@args.shift);
        }
        $callee := DNST::Temp.new( :name(get_unique_id('callee')), :type('RakudoObject'), $callee );

        # Emit call.
        return DNST::MethodCall.new(
            :name('STable.Invoke'), :type('RakudoObject'),
            $callee,
            'TC',
            $callee.name,
            form_capture(@args)
        );
    }

    elsif $op.pasttype eq 'bind' {
        my $*BIND_CONTEXT := 1;
        my $*BIND_VALUE;
        {
            my $*BIND_CONTEXT := 0;
            $*BIND_VALUE := dnst_for((@($op))[1]);
        }
        return dnst_for((@($op))[0]);
    }

    elsif $op.pasttype eq 'nqpop' {
        # Just a call on the Ops class. Always pass thread context
        # as the first parameter.
        my $result := DNST::MethodCall.new(
            :on('Ops'), :name($op.name), :type('RakudoObject'), 'TC'
        );
        for @($op) {
            $result.push(dnst_for($_));
        }
        return $result;
    }

    elsif $op.pasttype eq 'if' {
        my $cond_evaluated := get_unique_id('if_cond');
        return DNST::Stmts.new(
            DNST::Temp.new(
                :name($cond_evaluated), :type('RakudoObject'),
                dnst_for(PAST::Op.new(
                    :pasttype('callmethod'), :name('Bool'),
                    (@($op))[0]
                ))
            ),
            DNST::If.new(
                DNST::MethodCall.new(
                    :on('Ops'), :name('unbox_int'), :type('int'),
                    'TC', $cond_evaluated
                ),
                dnst_for((@($op))[1]),
                (+@($op) == 3 ?? dnst_for((@($op))[2]) !! $cond_evaluated)
            )
        );
    }

    elsif $op.pasttype eq 'unless' {
        my $cond_evaluated := get_unique_id('if_cond');
        return DNST::Stmts.new(
            DNST::Temp.new(
                :name($cond_evaluated), :type('RakudoObject'),
                dnst_for(PAST::Op.new(
                    :pasttype('call'), :name('&prefix:<!>'),
                    (@($op))[0]
                ))
            ),
            DNST::If.new(
                DNST::MethodCall.new(
                    :on('Ops'), :name('unbox_int'), :type('int'),
                    'TC', $cond_evaluated
                ),
                dnst_for((@($op))[1]),
                $cond_evaluated
            )
        );
    }

    elsif $op.pasttype eq 'while' || $op.pasttype eq 'until' {
        # Need labels for start and end.
        my $test_label := get_unique_id('while_lab');
        my $end_label := get_unique_id('while_end_lab');
        my $cond_result := get_unique_id('cond');
        
        # Compile the condition.
        
        my $cop := $op.pasttype eq 'until'
          ?? PAST::Op.new(
                :pasttype('call'), :name('&prefix:<!>'),
                (@($op))[0]
            )
          !! PAST::Op.new(
                :pasttype('callmethod'), :name('Bool'),
                (@($op))[0]
            );
        my $cond := DNST::Temp.new(
            :name($cond_result), :type('RakudoObject'),
            dnst_for($cop)
        );

        # Compile the body.
        my $body := dnst_for((@($op))[1]);

        # Build up result.
        return DNST::Stmts.new(
            DNST::Label.new( :name($test_label) ),
            $cond,
            DNST::If.new(
                DNST::MethodCall.new(
                    :on('Ops'), :name('unbox_int'), :type('int'),
                    'TC', $cond_result
                ),
                $body,
                DNST::Stmts.new(
                    $cond_result,
                    DNST::Goto.new( :label($end_label) )
                )
            ),
            DNST::Goto.new( :label($test_label) ),
            DNST::Label.new( :name($end_label) )
        );
    }

    elsif $op.pasttype eq 'repeat_while' || $op.pasttype eq 'repeat_until' {
        # Need labels for start and end.
        my $test_label := get_unique_id('while_lab');
        my $block_label := get_unique_id('block_lab');
        my $cond_result := get_unique_id('cond');
        
        # Compile the condition.
        
        my $cop := $op.pasttype eq 'repeat_until'
          ?? PAST::Op.new(
                :pasttype('call'), :name('&prefix:<!>'),
                (@($op))[0]
            )
          !! PAST::Op.new(
                :pasttype('callmethod'), :name('Bool'),
                (@($op))[0]
            );
        my $cond := DNST::Temp.new(
            :name($cond_result), :type('RakudoObject'),
            dnst_for($cop)
        );

        # Compile the body.
        my $body := dnst_for((@($op))[1]);

        # Build up result.
        return DNST::Stmts.new(
            DNST::Label.new( :name($block_label) ),
            $body,
            $cond,
            DNST::If.new(
                DNST::MethodCall.new(
                    :on('Ops'), :name('unbox_int'), :type('int'),
                    'TC', $cond_result
                ),
                DNST::Stmts.new(
                    $cond_result,
                    DNST::Goto.new( :label($block_label) )
                )
            )
        );
    }

    elsif $op.pasttype eq 'list' {
        my $tmp_name := get_unique_id('list_');
        my $result := DNST::Stmts.new(
            DNST::Temp.new(
                :name($tmp_name), :type('RakudoObject'),
                dnst_for(PAST::Op.new(
                    :pasttype('callmethod'), :name('new'),
                    PAST::Var.new( :name('NQPArray'), :scope('lexical') )
                ))
            )
        );
        my $i := 0;
        for @($op) {
            $result.push(DNST::MethodCall.new(
                :on('Ops'), :name('lllist_bind_at_pos'), :void(1), :type('RakudoObject'),
                'TC',
                $tmp_name,
                dnst_for(PAST::Val.new( :value($i) )),
                dnst_for($_)
            ));
            $i := $i + 1;
        }
        $result.push($tmp_name);
        return $result;
    }

    elsif $op.pasttype eq 'return' {
        return DNST::MethodCall.new(
            :on('Ops'), :name('throw_lexical'),
            'TC',
            dnst_for((@($op))[0]),
            dnst_for(PAST::Val.new( :value(57) ))
        );
    }

    elsif $op.pasttype eq 'def_or' {
        # Evaluate and store the first item.
        my $first_name := get_unique_id('def_or_first_');
        my $first := DNST::Temp.new(
            :name($first_name), :type('RakudoObject'),
            dnst_for((@($op))[0])
        );

        # Compile it as an if node that checks definedness.
        my $first_var := DNST::Local.new( :name($first_name) );
        return DNST::Stmts.new(
            $first,
            dnst_for(PAST::Op.new( :pasttype('if'),
                PAST::Op.new( :pasttype('callmethod'), :name('defined'), $first_var ),
                $first_var,
                (@($op))[1]
            ))
        );
    }

    else {
        pir::die("Don't know how to compile pasttype " ~ $op.pasttype);
    }
}

# How is capture formed?
sub form_capture(@args, $inv?) {
    # Create the various parts we might put into the capture.
    my $capture := DNST::MethodCall.new(
        :on('CaptureHelper'), :name('FormWith'), :type('RakudoObject')
    );
    my $pos_part := DNST::ArrayLiteral.new( :type('RakudoObject') );
    my $named_part := DNST::DictionaryLiteral.new(
        :key_type('string'), :value_type('RakudoObject') );
    my $flatten_flags := DNST::ArrayLiteral.new( :type('int') );
    my $has_flats := 0;
    
    # If it's a method call, we'll have an invocant to emit.
    if $inv ~~ DNST::Node {
        $pos_part.push($inv.name);
    }

    # Go over the args.
    for @args {
        if $_ ~~ PAST::Node && $_.named {
            if $_.flat {
                $pos_part.push(dnst_for($_));
                $flatten_flags.push('CaptureHelper.FLATTEN_NAMED');
                $has_flats := 1;
            }
            else {
                $named_part.push(DNST::Literal.new( :value($_.named), :escape(1) ));
                $named_part.push(dnst_for($_));
            }
        }
        elsif $_ ~~ PAST::Node && $_.flat {
            $pos_part.push(dnst_for($_));
            $flatten_flags.push('CaptureHelper.FLATTEN_POS');
            $has_flats := 1;
        }
        else {
            $pos_part.push(dnst_for($_));
            $flatten_flags.push('CaptureHelper.FLATTEN_NONE');
        }
    }

    # Push the various parts as needed.
    $capture.push($pos_part);
    if +@($named_part) || $has_flats { $capture.push($named_part); }
    if $has_flats { $capture.push($flatten_flags); }

    $capture;
}

# Emits a value.
our multi sub dnst_for(PAST::Val $val) {
    # If it's a block reference, hand back the SBI.
    if $val.value ~~ PAST::Block {
        unless $val.value<SBI> {
            pir::die("Can't use PAST::Val for a block reference for an as-yet uncompiled block");
        }
        return DNST::Literal.new( :value($val.value<SBI>) );
    }

    # Look up the type to box to.
    my $type;
    my $primitive;
    if pir::isa($val.value, 'Integer') {
        $primitive := 'int';
        $type := 'NQPInt';
    }
    elsif pir::isa($val.value, 'String') {
        $primitive := 'str';
        $type := 'NQPStr';
    }
    elsif pir::isa($val.value, 'Float') {
        $primitive := 'num';
        $type := 'NQPNum';
    }
    else {
        pir::die("Can not detect type of value")
    }
    my $*BIND_CONTEXT := 0;
    my $type_dnst := emit_lexical_lookup($type);
    
    # Add to constants table.
    my $make_const := DNST::MethodCall.new(
        :on('Ops'), :name('box_' ~ $primitive), :type('RakudoObject'),
        'TC',
        DNST::Literal.new( :value($val.value), :escape($primitive eq 'str') ),
        $type_dnst
    );
    if $*IN_LOADINIT || $*COMPILING_NQP_SETTING {
        return $make_const;
    }
    else {
        my $const_id := +@*CONSTANTS;
        @*CONSTANTS.push($make_const);
        return DNST::Literal.new( :value("ConstantsTable[$const_id]") );
    }
}

# Emits code for a variable node.
our multi sub dnst_for(PAST::Var $var) {
    # See if we have a scope provided. If not, work one out.
    my $scope := $var.scope;
    unless $scope {
        for @*PAST_BLOCKS {
            my %sym_info := $_.symbol($var.name);
            if %sym_info<scope> {
                $scope := %sym_info<scope>;
                last;
            }
        }
        unless $scope {
            pir::die('Symbol ' ~ $var.name ~ ' not pre-declared');
        }
    }

    # Now go by scope.
    if $scope eq 'parameter' {
        # Parameters we'll deal with later by building up a signature.
        @*PARAMS.push($var);
        return DNST::Stmts.new();
    }
    elsif $scope eq 'lexical' {
        if $var.isdecl {
            return declare_lexical($var);
        }
        else {
            return emit_lexical_lookup($var.name);
        }
    }
    elsif $scope eq 'outer' {
        if $var.isdecl {
            pir::die("Cannot use isdecl when scope is 'outer'.");
        }
        else {
            return emit_outer_lexical_lookup($var.name);
        }
    }
    elsif $scope eq 'contextual' {
        if $var.isdecl {
            return declare_lexical($var);
        }
        else {
            return emit_dynamic_lookup($var.name);
        }
    }
    elsif $scope eq 'package' {
        # Get all parts of the name.
        my @parts;
        @parts.push('GLOBAL');
        if pir::isa($var.namespace, 'ResizablePMCArray') {
            for $var.namespace { @parts.push($_); }
        }
        elsif +@*CURRENT_NS {
            for @*CURRENT_NS {
                @parts.push($_)
            }
        }
        @parts.push($var.name);

        # First, we need to look up the first part.
        my $lookup;
        {
            my $*BIND_CONTEXT := 0;
            $lookup := emit_lexical_lookup(@parts.shift);
        }

        # Also need to treat last part specially.
        my $target := @parts.pop;

        # Now chase down the rest.
        for @parts {
            $lookup := dnst_for(PAST::Op.new(
                :pasttype('callmethod'), :name('get_namespace'),
                $lookup,
                PAST::Val.new( :value(~$_) )
            ));
        }

        # Binding, if needed.
        if $*BIND_CONTEXT {
            my $*BIND_CONTEXT := 0;
            $lookup := dnst_for(PAST::Op.new(
                :pasttype('callmethod'), :name('bind_key'),
                $lookup,
                PAST::Val.new( :value(~$target) ),
                $*BIND_VALUE
            ));
        }
        else {
            $lookup := dnst_for(PAST::Op.new(
                :pasttype('callmethod'), :name('at_key'),
                $lookup,
                PAST::Val.new( :value(~$target) )
            ));
        }

        return $lookup;
    }
    elsif $scope eq 'register' {
        if $var.isdecl {
            my $result := DNST::Temp.new( :name($var.name), :type('RakudoObject') );
            if $*BIND_CONTEXT {
                $result.push($*BIND_VALUE);
            }
            elsif $var.viviself {
                $result.push(dnst_for($var.viviself));
            }
            else {
                $result.push('null');
            }
            return $result;
        }
        elsif $*BIND_CONTEXT {
            return DNST::Bind.new( DNST::Local.new( :name($var.name) ), $*BIND_VALUE );
        }
        else {
            return DNST::Local.new( :name($var.name) );
        }
    }
    elsif $scope eq 'attribute' {
        # Need to get hold of $?CLASS (always lookup) and self.
        my $class;
        my $self;
        {
            my $*BIND_CONTEXT := 0;
            $class := emit_lexical_lookup('$?CLASS');
            $self := emit_lexical_lookup('self');
        }

        # Emit attribute lookup/bind.
        my $lookup := DNST::MethodCall.new(
            :on('Ops'), :name($*BIND_CONTEXT ?? 'bind_attr' !! 'get_attr'),
            :type('RakudoObject'),
            'TC',
            $self,
            $class,
            DNST::Literal.new( :value($var.name), :escape(1) )
        );
        if $*BIND_CONTEXT {
            $lookup.push($*BIND_VALUE);
        }
        return $lookup;
    }
    elsif $scope eq 'keyed_int' {
        # XXX viviself, vivibase.
        if $*BIND_CONTEXT {
            # Get thing to do lookup in without bind context applied - we simply
            # want to look it up.
            my $*BIND_CONTEXT := 0;
            return dnst_for(PAST::Op.new(
                :pasttype('callmethod'), :name('bind_pos'),
                @($var)[0], @($var)[1], $*BIND_VALUE
            ));
        }
        else {
            return dnst_for(PAST::Op.new(
                :pasttype('callmethod'), :name('at_pos'),
                @($var)[0], @($var)[1]
            ));
        }
    }
    elsif $scope eq 'keyed' {
        # XXX viviself, vivibase.
        if $*BIND_CONTEXT {
            # Get thing to do lookup in without bind context applied - we simply
            # want to look it up.
            my $*BIND_CONTEXT := 0;
            return dnst_for(PAST::Op.new(
                :pasttype('callmethod'), :name('bind_key'),
                @($var)[0], @($var)[1], $*BIND_VALUE
            ));
        }
        else {
            return dnst_for(PAST::Op.new(
                :pasttype('callmethod'), :name('at_key'),
                @($var)[0], @($var)[1]
            ));
        }
    }
    else {
        pir::die("Don't know how to compile variable scope " ~ $var.scope);
    }
}

# Declares a lexical variable, and also handles viviself.
sub declare_lexical($var) {
    # Add to lexpad.
    @*LEXICALS.push($var.name);

    # Run viviself if there is one and bind it.
    if pir::defined($var.viviself) {
        my $*BIND_CONTEXT := 1;
        my $*BIND_VALUE;
        {
            my $*BIND_CONTEXT := 0;
            $*BIND_VALUE := dnst_for($var.viviself);
        }
        return emit_lexical_lookup($var.name);
    }
    else {
        return emit_lexical_lookup($var.name);
    }
}

# Catch-all for values and error detection.
our multi sub dnst_for($any) {
    if $any ~~ DNST::Node {
        # DNST of something already in DNST is itself.
        return $any;
    }
    elsif pir::isa($any, 'String') || pir::isa($any, 'Integer') || pir::isa($any, 'Float') {
        # Literals - wrap up in a value node and compile that.
        return dnst_for(PAST::Val.new( :value($any) ));
    }
    else {
        pir::die("Don't know how to compile a " ~ pir::typeof__SP($any) ~ "(" ~ $any ~ ")");
    }
}

# Non-regex nodes reached inside a regex
our multi sub dnst_regex(PAST::Node $r) {
    dnst_for($r)
}

# Regex nodes reached from non-regex nodes
our multi sub dnst_for(PAST::Regex $r) {
    my $rb; # regex block
    my $pasttype := $r.pasttype;
    pir::die("Don't know how to compile toplevel regex pasttype $pasttype.") if $pasttype ne 'concat';
    my $stmts := PAST::Stmts.new;
    
    # create a name-based jump table for this CLR routine
    my $jt := DNST::JumpTable.new();
    
    $stmts.push(DNST::Bind.new($jt.register, lit('0')));
    $stmts.push($jt.mark("dummy_marker"));
    
    # cursor register
    my $re_cur_tmp := DNST::Temp.new(
        :name(get_unique_id('re_cur')), :type('RakudoObject'),
        dnst_for(PAST::Var.new( :name('self'), :scope('lexical')))
    );
    my $*re_cur := DNST::Local.new( :name($re_cur_tmp.name) );
    my $*re_cur_name := $re_cur_tmp.name;
    $stmts.push($re_cur_tmp);
    
    # current position register
    my $re_pos := unbox_int(PAST::Op.new(
        :pasttype('callmethod'), :name('pos'),
        $*re_cur
    ));
    $stmts.push($re_pos);
    my $*re_pos := $re_pos.name;
    my $*re_pos_lit := lit($re_pos.name);
    
    # end of string register
    my $re_eos := unbox_int(PAST::Op.new(
        :pasttype('callmethod'), :name('eos'),
        $*re_cur
    ));
    $stmts.push($re_eos);
    my $*re_eos := $re_eos.name;
    my $*re_eos_lit := lit($re_eos.name);
    
    # original offset register
    my $re_off := unbox_int(PAST::Op.new(
        :pasttype('callmethod'), :name('off'),
        $*re_cur
    ));
    $stmts.push($re_off);
    my $*re_off := $re_off.name;
    my $*re_off_lit := lit($re_off.name);
    
    # rep register
    my $re_rep := temp_int();
    $stmts.push($re_rep);
    my $*re_rep := $re_rep.name;
    my $*re_rep_lit := lit($re_rep.name);
    
    # target (string) register
    my $re_tgt := unbox_str(PAST::Op.new(
        :pasttype('callmethod'), :name('target'),
        $*re_cur
    ));
    $stmts.push($re_tgt);
    my $*re_tgt := $re_tgt.name;
    my $*re_tgt_lit := lit($re_tgt.name);
    
    # fail label
    my $re_fail_label := get_unique_id('re_fail');
    my $*re_fail := DNST::Goto.new(:label($re_fail_label));
    
    # restart label
    my $re_restart_label := get_unique_id('re_restart');
    my $re_restart := DNST::Goto.new(:label($re_restart_label));
    
    # done label
    my $re_done_label := get_unique_id('re_done');
    my $re_done := DNST::Goto.new(:label($re_done_label));
    
    # return label
    my $re_return_label := get_unique_id('re_return');
    my $re_return := DNST::Goto.new(:label($re_return_label));
    
    my $*I10 := temp_int();
    my $*P10 := temp_int();
    
    for @($r) {
        $stmts.push(dnst_regex($_));
    }
    
    $stmts.push($re_done);
    
	
	$stmts.push(DNST::Label.new(:name($re_restart_label)));
    # ops.'push'(faillabel)
    $stmts.push(DNST::Label.new(:name($re_fail_label)));
    # self.'!cursorop'(ops, '!mark_fail', 4, rep, pos, '$I10', '$P10', 0)
    $stmts.push(dnst_for(PAST::Op.new(
        :pasttype('callmethod'), :name('mark_fail'),
        $*re_cur, val(4), box_int($*re_rep_lit), box_int($*I10), box_int($*P10), val(0)
    )));
	# ops.'push_pirop'('lt', pos, CURSOR_FAIL, donelabel)
	$stmts.push(if_then(lt($*re_pos_lit, lit('-1')), $re_done));
	# ops.'push_pirop'('eq', pos, CURSOR_FAIL, faillabel)
	$stmts.push(if_then(eq($*re_pos_lit, lit('-1')), $*re_fail));
    # ops.'push_pirop'('jump', '$I10')
	# XXX perform string-based label jump from a constructed switch/case
	
	$stmts.push($jt);
	
	$stmts.push($re_return);
    
    $stmts.push(DNST::Label.new(:name($re_done_label)));
    
    $stmts.push(dnst_for(PAST::Op.new(
        :pasttype('callmethod'), :name('pos'),
        $*re_cur,
        box_int($*re_pos_lit)
    )));
    
    $stmts.push(DNST::Label.new(:name($re_return_label)));
    
    $stmts.push(DNST::Return.new($*re_cur));
	
    dnst_for($stmts);
}

# Regex nodes reached inside a regex
our multi sub dnst_regex(PAST::Regex $r) {
    my $pasttype := $r.pasttype;
    my $stmts := PAST::Stmts.new;
    if $pasttype eq 'concat' {
        # Handle a concatenation of regexes.
        for @($r) {
            $stmts.push(dnst_regex($_));
        }
    }
    elsif $pasttype eq 'scan' {
        # Code for initial regex scan.
        my $s0 := get_unique_id('rxscan');
		my $looplabel := DNST::Label.new(:name($s0 ~ '_loop'));
    }
    elsif $pasttype eq 'literal' {
        # Code for literal characters.  Faked/stubbed.
        $stmts.push(if_then(
            eq(emit_call($*re_tgt, 'IndexOf', 'int', lits((@($r))[0]), $*re_pos_lit), lit(0)),
            DNST::Bind.new($*re_pos, plus($*re_pos_lit, lit(pir::length((@($r))[0])))),
			$*re_fail
        ));
    }
    elsif $pasttype eq 'pass' {
        # Code for success
        
    }
    else {
        pir::die("Don't know how to compile regex pasttype $pasttype.");
    }
    dnst_for($stmts)
}

# Emits a lookup of a lexical.
sub emit_lexical_lookup($name) {
    my $lookup := DNST::MethodCall.new(
        :on('Ops'), :name($*BIND_CONTEXT ?? 'bind_lex' !! 'get_lex'),
        :type('RakudoObject'),
        'TC',
        DNST::Literal.new( :value($name), :escape(1) )
    );
    if $*BIND_CONTEXT {
        $lookup.push($*BIND_VALUE);
    }
    $lookup
}

# Emits a lookup of a lexical in a scope outside the present one.
sub emit_outer_lexical_lookup($name) {
    if $*BIND_CONTEXT {
        pir::die("Cannot bind to something using scope 'outer'.");
    }
    my $lookup := DNST::MethodCall.new(
        :on('Ops'), :name('get_lex_skip_current'),
        :type('RakudoObject'),
        'TC',
        DNST::Literal.new( :value($name), :escape(1) )
    );
    $lookup
}

# Emits a lookup of a dynamic var.
sub emit_dynamic_lookup($name) {
    my $lookup := DNST::MethodCall.new(
        :on('Ops'), :name($*BIND_CONTEXT ?? 'bind_dynamic' !! 'get_dynamic'),
        :type('RakudoObject'),
        'TC',
        DNST::Literal.new( :value($name), :escape(1) )
    );
    if $*BIND_CONTEXT {
        $lookup.push($*BIND_VALUE);
    }
    $lookup
}

# Emits the printing of something # C# only, silly.
sub emit_say($arg) {
    DNST::Stmts.new(DNST::MethodCall.new(
        :on('Console'), :name('WriteLine'),
        :void(1),
        dnst_for($arg)
    ), dnst_for(PAST::Val.new( :value("") )))
}

# Emits the unboxing of an int
sub unbox_int($arg) {
    temp_int(dnst_for(DNST::MethodCall.new(
        :on('Ops'), :name('unbox_int'), :type('int'),
        'TC', dnst_for($arg)
    )))
}

sub temp_int($arg?) {
    DNST::Temp.new(
        :name(get_unique_id('int')), :type('int'),
        pir::defined($arg) ?? dnst_for($arg) !! lit(0)
    )
}

sub temp_str($arg?) {
    DNST::Temp.new(
        :name(get_unique_id('string')), :type('string'),
        pir::defined($arg) ?? dnst_for($arg) !! lits("")
    )
}

# Emits the boxing of an int
sub box_int($arg) {
    dnst_for(DNST::MethodCall.new(
        :on('Ops'), :name('box_int'), :type('RakudoObject'),
        'TC', dnst_for($arg)
    ))
}

# Emits the unboxing of an str
sub unbox_str($arg) {
    temp_str(dnst_for(DNST::MethodCall.new(
        :on('Ops'), :name('unbox_str'), :type('string'),
        'TC', dnst_for($arg)
    )))
}

sub plus($l, $r, $type?) {
    DNST::Add.new(dnst_for($l), dnst_for($r), pir::defined($type) ?? $type !! 'int')
}

sub minus($l, $r, $type?) {
    DNST::Subtract.new(dnst_for($l), dnst_for($r), pir::defined($type) ?? $type !! 'int')
}

sub bitwise_or($l, $r, $type?) {
    DNST::BOR.new(dnst_for($l), dnst_for($r), pir::defined($type) ?? $type !! 'int')
}

sub bitwise_and($l, $r, $type?) {
    DNST::BAND.new(dnst_for($l), dnst_for($r), pir::defined($type) ?? $type !! 'int')
}

sub bitwise_xor($l, $r, $type?) {
    DNST::BXOR.new(dnst_for($l), dnst_for($r), pir::defined($type) ?? $type !! 'int')
}

sub gt($l, $r) {
    DNST::GT.new(dnst_for($l), dnst_for($r), 'bool')
}

sub lt($l, $r) {
    DNST::LT.new(dnst_for($l), dnst_for($r), 'bool')
}

sub ge($l, $r) {
    DNST::GE.new(dnst_for($l), dnst_for($r), 'bool')
}

sub le($l, $r) {
    DNST::LE.new(dnst_for($l), dnst_for($r), 'bool')
}

sub eq($l, $r) {
    DNST::EQ.new(dnst_for($l), dnst_for($r), 'bool')
}

sub ne($l, $r) {
    DNST::NE.new(dnst_for($l), dnst_for($r), 'bool')
}

sub not($operand) {
    DNST::NOT.new(dnst_for($operand), 'bool')
}

# short-circuiting logical AND
sub log_and($l, $r) {
    my $temp;
    DNST::Stmts.new(
    ($temp := DNST::Temp.new(
        :name(get_unique_id('log_or')), :type('bool'), lit('false')
    )),
    if_then(DNST::Temp.new(
        :name(get_unique_id('left_bool')), :type('bool'), dnst_for($l)
    ), if_then(DNST::Temp.new(
        :name(get_unique_id('right_bool')), :type('bool'), dnst_for($r)
    ), DNST::Bind.new(
    ### XXX The next line works only with the C# backend (so far)
    ###   b/c the Bind causes the Temp to be redeclared without the lit(___.name)
    lit($temp.name)
    , lit('true')))));
}

# short-circuiting logical OR
sub log_or($l, $r) {
    my $temp;
    DNST::Stmts.new(
    ($temp := DNST::Temp.new(
        :name(get_unique_id('log_or')), :type('bool'), lit('false')
    )),
    if_then(DNST::Temp.new(
        :name(get_unique_id('left_bool')), :type('bool'), dnst_for($l)
    ),
    DNST::Bind.new(lit($temp.name), lit('true')),
    if_then(DNST::Temp.new(
        :name(get_unique_id('right_bool')), :type('bool'), dnst_for($r)
    ),
    DNST::Bind.new(lit($temp.name), lit('true')),
    )));
}

sub log_xor($l, $r) {
    DNST::XOR.new(dnst_for($l), dnst_for($r), 'bool')
}

sub if_then($cond, $pred, $oth?) {
    pir::defined($oth)
        ?? DNST::If.new($cond, $pred, $oth, :bool(1), :result(0))
        !! DNST::If.new($cond, $pred, :bool(1), :result(0))
}

sub lits($str) {
    DNST::Literal.new( :value($str), :escape(1))
}

sub lit($str) {
    $str ~~ DNST::Literal
        ?? $str
        !! DNST::Literal.new( :value($str), :escape(0))
}

sub val($val) {
    $val ~~ DNST::Node
        ?? $val
        !! dnst_for(PAST::Val.new( :value($val) ))
}

sub emit_op($name, $arg1, $arg2?, $arg3?) {
    my $res;
    if pir::defined($arg2) {
        if pir::defined($arg3) {
            $res := DNST::MethodCall.new(
                :on('Ops'), :name($name),
                :type('RakudoObject'),
                'TC',
                dnst_for($arg1),
                dnst_for($arg2),
                dnst_for($arg3)
            )
        } else {
            $res := DNST::MethodCall.new(
                :on('Ops'), :name($name),
                :type('RakudoObject'),
                'TC',
                dnst_for($arg1),
                dnst_for($arg2)
            )
        }
    } else {
        $res := DNST::MethodCall.new(
            :on('Ops'), :name($name),
            :type('RakudoObject'),
            'TC',
            dnst_for($arg1)
        )
    }
    $res
}

sub emit_call($on, $name, $type, $arg1, $arg2?, $arg3?) {
    my $res;
    if pir::defined($arg2) {
        if pir::defined($arg3) {
            $res := DNST::MethodCall.new(
                :on($on), :name($name),
                :type($type),
                dnst_for($arg1),
                dnst_for($arg2),
                dnst_for($arg3)
            )
        } else {
            $res := DNST::MethodCall.new(
                :on($on), :name($name),
                :type($type),
                dnst_for($arg1),
                dnst_for($arg2)
            );
        }
    } else {
        $res := DNST::MethodCall.new(
            :on($on), :name($name),
            :type($type),
            dnst_for($arg1)
        )
    }
    $res
}