# This is the beginnings of a PAST to Java Syntax Tree translator. It'll
# no doubt evolve somewhat over time, and get filled out as we support
# more and more of PAST.
class PAST2JSTCompiler;

# Set up a hash of operator signatures. Only needed for those that do not
# return and take just RakudoObject instances. First type is return type,
# following ones are argument types.
our %nqp_op_sigs;
INIT {
    %nqp_op_sigs                  := pir::new__pS('Hash');
    %nqp_op_sigs<equal_nums>      := ('int', 'num', 'num');
    %nqp_op_sigs<equal_ints>      := ('int', 'int', 'int');
    %nqp_op_sigs<equal_strs>      := ('int', 'str', 'str');
    %nqp_op_sigs<logical_not_int> := ('int', 'int');
    %nqp_op_sigs<add_int>         := ('int', 'int', 'int');
    %nqp_op_sigs<sub_int>         := ('int', 'int', 'int');
    %nqp_op_sigs<mul_int>         := ('int', 'int', 'int');
    %nqp_op_sigs<div_int>         := ('int', 'int', 'int');
    %nqp_op_sigs<mod_int>         := ('int', 'int', 'int');
}

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
    my $*SBI_SETUP := JST::Stmts.new();

    # We'll do similar for values - essentially, this builds a constants
    # table so we don't have to build them again and again.
    my @*CONSTANTS;

    # Also need to track the PAST blocks we're in.
    my @*PAST_BLOCKS;

    # Current namespace path.
    my @*CURRENT_NS;

    # The current type context, e.g. what result type the thing further
    # up in the tree is expecting.
    my $*TYPE_CONTEXT := 'obj';

    # Compile the node; ensure it is an immediate block.
    $node.blocktype('immediate');
    my $main_block_call := jst_for($node);

    # Build a class node and add the inner code blocks.
    my $class := JST::Class.new(
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
            if $_ ~~ JST::TryFinally {
                $_.pop();
                $_.push(JST::Stmts.new());
            }
        }
    }

    # Also need to include setup of static block info.
    $class.push(JST::Attribute.new( :name('StaticBlockInfo'), :type('RakudoCodeRef.Instance[]') ));
    $class.push(JST::Attribute.new( :name('ConstantsTable'), :type('RakudoObject[]') ));
    $class.push(make_blocks_init_method('blocks_init'));
    $class.push(make_constants_init_method('constants_init'));

    # Calls to loadinits.
    my $loadinit_calls := JST::Stmts.new();
    for @*LOADINITS {
        $loadinit_calls.push($_);
    }
    for @*SIGINITS {
        $loadinit_calls.push($_);
    }

    # Finally, startup handling code.
    if $*COMPILING_NQP_SETTING {
        $class.push(JST::Method.new(
            :name('LoadSetting'),
            :return_type('Context'),
            JST::Local.new( :name('TC'), :isdecl(1), :type('ThreadContext'),
                JST::MethodCall.new(
                   :on('Rakudo.Init'), :name('Initialize'),
                   :type('ThreadContext'),
                   JST::Null.new()
                )
            ),
            JST::Call.new( :name('blocks_init'), :void(1), TC() ),

            # We fudge in a fake NQPStr, for the :repr('P6Str'). Bit hacky,
            # but best I can think of for now. :-)
            JST::MethodCall.new(
                :on('StaticBlockInfo[1].StaticLexPad'), :name('SetByName'), :void(1), :type('RakudoObject'),
                JST::Literal.new( :value('NQPStr'), :escape(1) ),
                'REPRRegistry.get_REPR_by_name("P6str").type_object_for(null, null)'
            ),

            # We do the loadinit calls before building the constants, as we
            # may build some constants with types we're yet to define.
            $loadinit_calls,
            JST::Call.new( :name('constants_init'), :void(1), TC() ),
            $main_block_call,
            "TC.CurrentContext"
        ));
    }
    else {
        # Commonalities for no matter how we start running (be it from the
        # command line or loaded as a library).
        my @params;
        @params.push('ThreadContext TC');
        $class.push(JST::Method.new(
            :name('Init'),
            :params(@params),
            :return_type('void'),
            JST::Call.new( :name('blocks_init'), :void(1), TC() ),
            JST::Call.new( :name('constants_init'), :void(1), TC() ),
            $loadinit_calls
        ));

        # Code for when it's the entry point (e.g. a main method).
        @params := (); @params.push('String[] args'); $class.push(JST::Method.new(
            :name('main'), # C# has Main
            :return_type('void'), :params(@params),
            JST::Local.new( :name('TC'), :isdecl(1), :type('ThreadContext'),
                JST::MethodCall.new(
                    :on('Rakudo.Init'), :name('Initialize'), :type('ThreadContext'),
                    JST::Literal.new( :value('NQPSetting'), :escape(1) )
                )
            ),
            JST::Call.new( :name('Init'), :void(1), TC() ),
            $main_block_call
        ));

        # Code for when it's being loaded as a library.
        $class.push(JST::Method.new(
            :name('Load'),
            :params('ThreadContext TC', 'Context Setting'),
            :return_type('RakudoObject'),
            JST::Call.new( :name('Init'), :void(1), TC() ),
            $main_block_call
        ));
    }

    # Package up in a compilation unit with the required "import"s.
    return JST::CompilationUnit.new(
        JST::Using.new( :namespace('java.util.ArrayList') ),
        JST::Using.new( :namespace('java.util.HashMap') ),
        JST::Using.new( :namespace('Rakudo.Metamodel.Hints') ),
        JST::Using.new( :namespace('Rakudo.Metamodel.RakudoObject') ),
        JST::Using.new( :namespace('Rakudo.Metamodel.Representations.P6int') ),
        JST::Using.new( :namespace('Rakudo.Metamodel.Representations.RakudoCodeRef') ),
        JST::Using.new( :namespace('Rakudo.Metamodel.Representations.RakudoCodeRef.Instance') ),
        JST::Using.new( :namespace('Rakudo.Metamodel.REPRRegistry') ),
        JST::Using.new( :namespace('Rakudo.Runtime.CaptureHelper') ),
        JST::Using.new( :namespace('Rakudo.Runtime.CodeObjectUtility') ),
        JST::Using.new( :namespace('Rakudo.Runtime.DefinednessConstraint') ),
        JST::Using.new( :namespace('Rakudo.Runtime.Context') ),
        JST::Using.new( :namespace('Rakudo.Runtime.Exceptions.LeaveStackUnwinderException') ),
        JST::Using.new( :namespace('Rakudo.Runtime.Ops') ),
        JST::Using.new( :namespace('Rakudo.Runtime.Parameter') ),
        JST::Using.new( :namespace('Rakudo.Runtime.Signature') ),
        JST::Using.new( :namespace('Rakudo.Runtime.SignatureBinder') ),
        JST::Using.new( :namespace('Rakudo.Runtime.ThreadContext') ),
        $class
    );
}

# Creates a not-really-that-unique-yet name for the module (good enough if
# we compile one per second, which given we're cross-compiling, is enough
# for now.)
sub unique_name_for_module() {
# TODO    'NQPOutput_' ~ pir::set__IN(pir::time__N())
    'RakudoOutput'
}

# This makes the block static info initialization sub. One day, this
# can likely go away and we freeze a bunch of this info. But for now,
# this will do.
sub make_blocks_init_method($name) {
    my @params;
    @params.push('ThreadContext TC');
    return JST::Method.new(
        :name($name),
        :params(@params),
        :return_type('void'),
        
        # Create array for storing these.
        JST::Bind.new(
            loc('StaticBlockInfo', 'RakudoCodeRef.Instance[]'),
            'new RakudoCodeRef.Instance[' ~ $*SBI_POS ~ ']'
        ),

        # Fake up outermost one for now.
        JST::Bind.new(
            'StaticBlockInfo[0]',
            JST::MethodCall.new(
                :on('CodeObjectUtility'), :name('BuildStaticBlockInfo'),
                :type('RakudoCodeRef.Instance'),
                JST::Null.new(), JST::Null.new(),
                JST::ArrayLiteral.new( :type('String') )
            )
        ),
        JST::Bind.new(
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
    my $result := JST::Method.new(
        :name($name),
        :params(@params),
        :return_type('void'),

        # Fake up a context with the outer being the main block.
        JST::Local.new(
            :name('C'), :isdecl(1), :type('Context'),
            JST::New.new(
                :type('Context'),
                JST::MethodCall.new(
                    :on('CodeObjectUtility'), :name('BuildStaticBlockInfo'),
                    :type('RakudoCodeRef.Instance'),
                    JST::Null.new(),
                    'StaticBlockInfo[1]',
                    JST::ArrayLiteral.new( :type('String') )
                ),
                'TC.CurrentContext',
                JST::Null.new()
            )
        ),
        
        # Create array for storing these.
        JST::Bind.new(
            loc('ConstantsTable', 'RakudoObject[]'),
            'new RakudoObject[' ~ +@*CONSTANTS ~ ']'
        )
    );

    # Add all constants into table.
    my $i := 0;
    while $i < +@*CONSTANTS {
        $result.push(JST::Bind.new(
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
our multi sub jst_for(PAST::Block $block) {
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
    my $our_sbi_setup := JST::MethodCall.new(
        :on('CodeObjectUtility'),
        :name('BuildStaticBlockInfo'),
        :type('RakudoCodeRef.Instance')
    );
    $*SBI_POS := $*SBI_POS + 1;
    $*SBI_SETUP.push(JST::Bind.new(
        "StaticBlockInfo[$our_sbi]",
        $our_sbi_setup
    ));

    # Label the PAST block with its SBI.
    $block<SBI> := "StaticBlockInfo[$our_sbi]";

    # Make start of block.
    my $result := JST::Method.new(
        :name(get_unique_id('block')),
        :params('ThreadContext TC', 'RakudoObject Block', 'RakudoObject Capture'),
        :return_type('RakudoObject')
    );
    
    # Emit all the statements.
    my @inner_blocks;
    my $stmts := JST::Stmts.new();
    for @($block) {
        my $*OUTER_SBI := $our_sbi;
        my @*INNER_BLOCKS;
        $stmts.push(jst_for($_));
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
        @*LOADINITS.push(jst_for(PAST::Block.new(
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
        %handler<code> := jst_for(PAST::Block.new(PAST::Stmts.new(
            PAST::Var.new( :name('$!'), :scope('parameter') ),
            emit_op('leave_block',
                JST::Literal.new( :value('TC.CurrentContext.Outer.StaticCodeObject') ),
                jst_for(PAST::Var.new( :name('$!'), :scope('lexical') ))
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
    my $handlers_setup_placeholder := JST::Stmts.new();
    my $sig_setup_block := get_unique_id('block');
    my @params;
    @params.push('ThreadContext TC');
    @inner_blocks.push(JST::Method.new(
        :return_type('void'),
        :name($sig_setup_block),
        :params(@params),
        JST::Local.new(
            :type('Context'), :name('C'), :isdecl(1),
            JST::New.new(
                :type('Context'),
                JST::MethodCall.new(
                    :on('CodeObjectUtility'), :name('BuildStaticBlockInfo'),
                    :type('RakudoCodeRef.Instance'),
                    JST::Null.new(),
                    "StaticBlockInfo[$our_sbi]",
                    JST::ArrayLiteral.new( :type('String') )
                ),
                'TC.CurrentContext',
                JST::Null.new()
            )
        ),
        JST::Bind.new( 'TC.CurrentContext', loc('C', 'Context') ),
        JST::Bind.new(
            "StaticBlockInfo[$our_sbi].Sig",
            compile_signature(@*PARAMS)
        ),
        $handlers_setup_placeholder,
        JST::Bind.new( 'TC.CurrentContext', 'C.Caller' )
    ));
    @*SIGINITS.push(JST::Call.new( :name($sig_setup_block), :void(1), TC() ));

    # Before start of statements, we want to bind the signature.
    $stmts.unshift(JST::MethodCall.new(
        :on('SignatureBinder'), :name('Bind'), :void(1),
        TC(), loc('C', 'Context'), loc('Capture')
    ));

    # Wrap in block prelude/postlude.
    $result.push(JST::Local.new(
        :name('C'), :isdecl(1), :type('Context'),
        JST::New.new( :type('Context'), "Block", "TC.CurrentContext", loc("Capture") )
    ));
    $result.push(JST::Bind.new( 'TC.CurrentContext', loc('C', 'Context') ));
    $result.push(JST::TryFinally.new(
        JST::TryCatch.new(
            :exception_type('LeaveStackUnwinderException'),
            :exception_var('exc'),
            $stmts,
            JST::Stmts.new(
                JST::If.new(
                    JST::Literal.new(
                        :value("(exc.TargetBlock != Block ? 1 : 0)")
                    ),
                    JST::Throw.new()
                ),
                "exc.PayLoad"
            )
        ),
        JST::Bind.new( 'TC.CurrentContext', 'C.Caller' )
    ));
    
    # Add nested inner blocks after it (.Net does not support
    # nested blocks).
    @*INNER_BLOCKS.push($result);
    for @inner_blocks {
        @*INNER_BLOCKS.push($_);
    }

    # Set up body, static outer and lexicals in the code setup block call.
    $our_sbi_setup.push(JST::New.new(
        :type('RakudoCodeRef.IFunc_Body'), # C# has :type('Func<ThreadContext, RakudoObject, RakudoObject, RakudoObject>'),
        $result.name
    ));
    $our_sbi_setup.push("StaticBlockInfo[$outer_sbi]");
    my $lex_setup := JST::ArrayLiteral.new( :type('String') );
    for @*LEXICALS {
        $lex_setup.push(JST::Literal.new( :value($_), :escape(1) ));
    }
    $our_sbi_setup.push($lex_setup);

    # Add handlers.
    if +@*HANDLERS {
        my $handler_node := JST::ArrayLiteral.new( :type('Rakudo.Runtime.Exceptions.Handler') );
        for @*HANDLERS {
            $handler_node.push(JST::New.new(
                :type('Rakudo.Runtime.Exceptions.Handler'),
                JST::Literal.new( :value($_<type>) ),
                $_<code>
            ));
        }
        $handlers_setup_placeholder.push(JST::Bind.new(
            "StaticBlockInfo[$our_sbi].Handlers",
            $handler_node
        ));
    }

    # Clear up this PAST::Block from the blocks list.
    @*PAST_BLOCKS.shift;
    @*CURRENT_NS := $prev_ns;

    # For immediate evaluate to a call; for declaration, evaluate to the
    # low level code object.
    if $block.blocktype eq 'immediate' {
        return JST::MethodCall.new(
            :name('getSTable().Invoke'), :type('RakudoObject'),
            "StaticBlockInfo[$our_sbi]",
            TC(),
            "StaticBlockInfo[$our_sbi]",
            JST::MethodCall.new(
                :on('CaptureHelper'),
                :name('FormWith'),
                :type('RakudoObject')
            )
        );
    }
    else {
        return emit_op(
            ($block.closure ?? 'new_closure' !! 'capture_outer'),
            JST::Local.new( :name("StaticBlockInfo[$our_sbi]") )
        );
    }
}

# Compiles a bunch of parameter nodes down to a signature.
sub compile_signature(@params) {
    # Go through each of the parameters and compile them.
    my $params := JST::ArrayLiteral.new( :type('Parameter') );
    for @params {
        my $param := JST::New.new( :type('Parameter') );

        # Type.
        if $_.multitype {
            $param.push(jst_for($_.multitype));
        }
        else {
            $param.push(JST::Null.new());
        }

        # Variable name to bind into.
        my $lexpad_position := +@*LEXICALS;
        @*LEXICALS.push($_.name);
        $param.push(JST::Literal.new( :value($_.name), :escape(1) ));
        $param.push(JST::Literal.new( :value($lexpad_position) ));

        # Named param or not?
        $param.push((!$_.slurpy && $_.named) ??
            JST::Literal.new( :value(pir::substr($_.name, 1)), :escape(1) ) !!
            JST::Null.new());

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
            ?? jst_for(PAST::Block.new(:closure(1), $_.viviself))
            !! JST::Null.new());

        $params.push($param);
    }

    # Build up a signature object.
    return JST::New.new( :type('Signature'), $params );
}

# Compiles a statements node - really just all the stuff in it.
our multi sub jst_for(PAST::Stmts $stmts) {
    my $result := JST::Stmts.new();
    for @($stmts) {
        $result.push(jst_for($_));
    }
    return $result;
}

# Compiles the various forms of PAST::Op.
our multi sub jst_for(PAST::Op $op) {
    if $op.pasttype eq 'callmethod' {
        # We want to emit code for the args, but also need the
        # invocant to hand specially.
        my @args := @($op);
        if +@args == 0 { pir::die("callmethod node must have at least an invocant"); }
        
        # Invocant.
        my $inv := JST::Local.new(
            :name(get_unique_id('inv')), :isdecl(1), :type('RakudoObject'),
            jst_for(@args.shift)
        );

        # Method name, for indirectly named dotty calls
        my $name := $op.name ~~ PAST::Node
          ?? unbox('str', PAST::Op.new(
                 :pasttype('callmethod'), :name('Str'),
                 jst_for($op.name)
             ))
          !! JST::Literal.new( :value($op.name), :escape(1) );
        
        # Method lookup.
        my $callee := JST::Local.new(
            :name(get_unique_id('callee')), :isdecl(1), :type('RakudoObject'),
            JST::MethodCall.new(
                :on($inv.name), :name('getSTable().FindMethod'), :type('RakudoObject'),
                TC(),
                $inv.name,
                $name,
                'Hints.NO_HINT'
            )
        );

        # Emit the call.
        return JST::Stmts.new(
            $inv,
            JST::MethodCall.new(
                :name('getSTable().Invoke'), :type('RakudoObject'),
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
            $callee := jst_for(@args.shift);
        }
        $callee := JST::Local.new( :name(get_unique_id('callee')), :isdecl(1), :type('RakudoObject'), $callee );

        # Emit call.
        return JST::MethodCall.new(
            :name('getSTable().Invoke'), :type('RakudoObject'),
            $callee,
            TC(),
            $callee.name,
            form_capture(@args)
        );
    }

    elsif $op.pasttype eq 'bind' {
# DROP  # Construct JST for LHS in bind context.
        my $lhs; 
        {
            my $*BIND_CONTEXT := 1;
            $lhs := jst_for((@($op))[0]);
        }
        # Now push onto that the evaluated RHS.
        $lhs.push(jst_for((@($op))[1]));

        return $lhs;
# TODO  my $*BIND_CONTEXT := 1;
#       my $*BIND_VALUE;
#       {
#           my $*BIND_CONTEXT := 0;
#           $*BIND_VALUE := jst_for((@($op))[1]);
#       }
#       return jst_for((@($op))[0]);
    }

    elsif $op.pasttype eq 'nqpop' {
        # Just a call on the Ops class. Always pass thread context
        # as the first parameter.
# TODO  my @args := @($op);
#       return emit_op($op.name, |@args);
        my $result := JST::MethodCall.new( # TODO: DROP
            :on('Ops'), :name($op.name), :type('RakudoObject'), 'TC' # TODO: :type()
        );
        for @($op) {
            $result.push(jst_for($_));
        }
        return $result;
    }

    elsif $op.pasttype eq 'if' {
        my $cond_evaluated := JST::Local.new( :name(get_unique_id('if_cond')) );
        return JST::Stmts.new(
            JST::Local.new(
                :name($cond_evaluated.name), :isdecl(1), :type('RakudoObject'),
                jst_for(PAST::Op.new(
                    :pasttype('callmethod'), :name('Bool'),
                    (@($op))[0]
                ))
            ),
            JST::If.new(
                unbox('int', $cond_evaluated),
                jst_for((@($op))[1]),
                (+@($op) == 3 ?? jst_for((@($op))[2]) !! $cond_evaluated)
            )
        );
    }

    elsif $op.pasttype eq 'unless' {
        my $cond_evaluated := get_unique_id('unless_cond');
        my $temp;
        return JST::Stmts.new(
            ($temp := JST::Local.new(
                :name(get_unique_id('unless_result')), :isdecl(1), :type('RakudoObject'), val(0)
            )),
            JST::Local.new(
                :name($cond_evaluated), :isdecl(1), :type('RakudoObject'),
                jst_for(PAST::Op.new(
                    :pasttype('call'), :name('&prefix:<!>'),
                    JST::Bind.new(lit($temp.name), jst_for((@($op))[0]))
                ))
            ),
            JST::If.new(
                JST::MethodCall.new(
                    :on('Ops'), :name('unbox_int'), :type('int'),
                    TC(), $cond_evaluated
                ),
                JST::Bind.new(lit($temp.name), jst_for((@($op))[1])),
                JST::Bind.new(lit($temp.name), jst_for($cond_evaluated)),
            ),
            lit($temp.name)
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
        my $cond := JST::Local.new(
            :name($cond_result.name), :isdecl(1), :type('RakudoObject'),
            jst_for($cop)
        );

        # Compile the body.
        my $body := jst_for((@($op))[1]);

        # Build up result.
        return JST::Stmts.new(
            JST::Label.new( :name($test_label) ),
            $cond,
            JST::If.new(
                unbox('int', $cond_result),
                $body,
                JST::Stmts.new(
                    $cond_result,
                    JST::Goto.new( :label($end_label) )
                )
            ),
            JST::Goto.new( :label($test_label) ),
            JST::Label.new( :name($end_label) )
        );
    }

    elsif $op.pasttype eq 'repeat_while' || $op.pasttype eq 'repeat_until' {
        # Need labels for start and end.
        my $test_label := get_unique_id('while_lab');
        my $block_label := get_unique_id('block_lab');
        my $cond_result := JST::Local.new( :name(get_unique_id('cond')) );
        
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
        my $cond := JST::Local.new(
            :name($cond_result.name), :isdecl(1), :type('RakudoObject'),
            jst_for($cop)
        );

        # Compile the body.
        my $body := jst_for((@($op))[1]);

        # Build up result.
        return JST::Stmts.new(
            JST::Label.new( :name($block_label) ),
            $body,
            $cond,
            JST::If.new(
                unbox('int', $cond_result),
                JST::Stmts.new(
                    $cond_result,
                    JST::Goto.new( :label($block_label) )
                )
            )
        );
    }

    elsif $op.pasttype eq 'list' {
        my $tmp_name := get_unique_id('list_');
        my $result := JST::Stmts.new(
            JST::Local.new(
                :name($tmp_name), :isdecl(1), :type('RakudoObject'),
                jst_for(PAST::Op.new(
                    :pasttype('callmethod'), :name('new'),
                    PAST::Var.new( :name('NQPArray'), :scope('lexical') )
                ))
            )
        );
        my $i := 0;
        for @($op) {
            $result.push(JST::MethodCall.new(
                :on('Ops'), :name('lllist_bind_at_pos'), :void(1), :type('RakudoObject'),
                TC(),
                $tmp_name,
                jst_for(PAST::Val.new( :value($i) )),
                jst_for($_)
            ));
            $i := $i + 1;
        }
        $result.push($tmp_name);
        return $result;
    }

    elsif $op.pasttype eq 'return' {
        return emit_op('throw_lexical', (@($op))[0], PAST::Val.new( :value(57) ));
    }

    elsif $op.pasttype eq 'def_or' {
        # Evaluate and store the first item.
        my $first_name := get_unique_id('def_or_first_');
        my $first := JST::Local.new(
            :name($first_name), :isdecl(1), :type('RakudoObject'),
            jst_for((@($op))[0])
        );

        # Compile it as an if node that checks definedness.
        my $first_var := JST::Local.new( :name($first_name) );
        return JST::Stmts.new(
            $first,
            jst_for(PAST::Op.new( :pasttype('if'),
                PAST::Op.new( :pasttype('callmethod'), :name('defined'), $first_var ),
                $first_var,
                (@($op))[1]
            ))
        );
    }

    else {
        pir::die("PAST2JSTCompiler.pm does not know how to compile pasttype " ~ $op.pasttype);
    }
}

# How is capture formed?
sub form_capture(@args, $inv?) {
    # Create the various parts we might put into the capture.
    my $capture := JST::MethodCall.new(
        :on('CaptureHelper'), :name('FormWith'), :type('RakudoObject')
    );
    my $pos_part := JST::ArrayLiteral.new( :type('RakudoObject') );
    my $named_part := JST::DictionaryLiteral.new(
        :key_type('String'), :value_type('RakudoObject') );
    my $flatten_flags := JST::ArrayLiteral.new( :type('int') );
    my $has_flats := 0;

    # If it's a method call, we'll have an invocant to emit.
    if $inv ~~ JST::Node {
        $pos_part.push($inv.name);
    }

    # Go over the args.
    for @args {
        if $_ ~~ PAST::Node && $_.named {
            if $_.flat {
                $pos_part.push(jst_for($_));
                $flatten_flags.push('CaptureHelper.FLATTEN_NAMED');
                $has_flats := 1;
            }
            else {
                $named_part.push(JST::Literal.new( :value($_.named), :escape(1) ));
                $named_part.push(jst_for($_));
            }
        }
        elsif $_ ~~ PAST::Node && $_.flat {
            $pos_part.push(jst_for($_));
            $flatten_flags.push('CaptureHelper.FLATTEN_POS');
            $has_flats := 1;
        }
        else {
            $pos_part.push(jst_for($_));
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
our multi sub jst_for(PAST::Val $val) {
    # If it's a block reference, hand back the SBI.
    if $val.value ~~ PAST::Block {
        unless $val.value<SBI> {
            pir::die("Can't use PAST::Val for a block reference for an as-yet uncompiled block");
        }
        return JST::Literal.new( :value($val.value<SBI>) );
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
        $type := 'NQPNum'; # TODO: DROP
    }
    else {
        pir::die("PAST2JSTCompiler.pm cannot detect type of value")
    }
    
    # If we have a non-object type context, can hand back a literal value.
    if $*TYPE_CONTEXT ne 'obj' {
        return JST::Literal.new(
            :value($val.value),
            :type(vm_type_for($primitive)),
            :escape($primitive eq 'str')
        );
    }


    # Otherwise, need to box it. Add to constants table if possible.
#   my $make_const := box($primitive, DNST::Literal.new(
# TODO  :value($val.value), :escape($primitive eq 'str') ));
    my $type_jst := emit_lexical_lookup($type); # TODO: DROP
    my $make_const := JST::MethodCall.new(
        :on('Ops'), :name('box_' ~ $primitive), :type('RakudoObject'),
        'TC',
        JST::Literal.new( :value($val.value), :escape($primitive eq 'str') ),
        $type_jst
    );
    if $*IN_LOADINIT  || $*COMPILING_NQP_SETTING {
        return $make_const;
    }
    else {
        my $const_id := +@*CONSTANTS;
        @*CONSTANTS.push($make_const);
# TODO  return DNST::Literal.new( :value("ConstantsTable[$const_id]") );
        return "ConstantsTable[$const_id]";
    }
}

# Emits code for a variable node.
our multi sub jst_for(PAST::Var $var) {
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
        return JST::Stmts.new();
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
            $lookup := jst_for(PAST::Op.new(
                :pasttype('callmethod'), :name('get_namespace'),
                $lookup,
                PAST::Val.new( :value(~$_) )
            ));
        }

        # Binding, if needed.
        if $*BIND_CONTEXT {
            my $*BIND_CONTEXT := 0;
            $lookup := jst_for(PAST::Op.new(
                :pasttype('callmethod'), :name('bind_key'),
                $lookup,
                PAST::Val.new( :value(~$target) ),
                $*BIND_VALUE
            ));
        }
        else {
            $lookup := jst_for(PAST::Op.new(
                :pasttype('callmethod'), :name('at_key'),
                $lookup,
                PAST::Val.new( :value(~$target) )
            ));
        }

        return $lookup;
    }
    elsif $scope eq 'register' {
        if $var.isdecl {
            my $result := JST::Local.new( :name($var.name), :isdecl(1), :type('RakudoObject') );
            unless $*BIND_CONTEXT {
                $result.push(ST::Null.new());
            }
            return $result;
        }
        elsif $*BIND_CONTEXT {
            return JST::Bind.new( $var.name );
        }
        else {
            return $var.name;
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
        my $lookup := emit_op(($*BIND_CONTEXT ?? 'bind_attr' !! 'get_attr'),
            $self,
            $class,
            JST::Literal.new( :value($var.name), :escape(1) )
        );
        if $*BIND_CONTEXT {
            $lookup.push($*BIND_VALUE);
        }
        elsif pir::defined($var.viviself) {
            # May need to auto-vivify.
            my $viv_name := get_unique_id('viv_attr_');
            my $temp := JST::Local.new( :name($viv_name), :isdecl(1), :type('RakudoObject'), $lookup );
            $lookup := JST::Stmts.new(
                $temp,
                JST::If.new( :bool(1),
                    eq(JST::Local.new( :name($viv_name) ), DNST::Null.new()),
                    jst_for($var.viviself),
                    JST::Local.new( :name($viv_name) )
                )
            );
        }
        return $lookup;
    }
    elsif $scope eq 'keyed_int' {
        # XXX viviself, vivibase.
        if $*BIND_CONTEXT {
            # Get thing to do lookup in without bind context applied - we simply
            # want to look it up.
            my $*BIND_CONTEXT := 0;
            return jst_for(PAST::Op.new(
                :pasttype('callmethod'), :name('bind_pos'),
                @($var)[0], @($var)[1], $*BIND_VALUE
            ));
        }
        else {
            return jst_for(PAST::Op.new(
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
            return jst_for(PAST::Op.new(
                :pasttype('callmethod'), :name('bind_key'),
                @($var)[0], @($var)[1], $*BIND_VALUE
            ));
        }
        else {
            return jst_for(PAST::Op.new(
                :pasttype('callmethod'), :name('at_key'),
                @($var)[0], @($var)[1]
            ));
        }
    }
    else {
        pir::die("PAST2JSTCompiler.pm does not know how to compile variable scope " ~ $var.scope);
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
            $*BIND_VALUE := jst_for($var.viviself);
        }
        return emit_lexical_lookup($var.name);
    }
    else {
        return emit_lexical_lookup($var.name);
    }
}

# Catch-all for error detection.
our multi sub jst_for($any) {
    if $any ~~ JST::Node {
        # JST of something already in JST is itself.
        return $any;
    }
    elsif pir::isa($any, 'String') || pir::isa($any, 'Integer') || pir::isa($any, 'Float') {
        # Literals - wrap up in a value node and compile that.
        return jst_for(PAST::Val.new( :value($any) ));
    }
    else {
        pir::die("PAST2JSTCompiler.pm does not know how to compile a " ~ pir::typeof__SP($any) ~ "(" ~ $any ~ ")");
    }
}

# Non-regex nodes reached inside a regex
our multi sub jst_regex($r) {
    jst_for($r)
}

# Emits a lookup of a lexical.
sub emit_lexical_lookup($name) {
# TODO:
#   my $lookup := emit_op(($*BIND_CONTEXT ?? 'bind_lex' !! 'get_lex'),
#       JST::Literal.new( :value($name), :escape(1) )
#   );
#   if $*BIND_CONTEXT {
#       $lookup.push($*BIND_VALUE);
#   }
#   $lookup
    return JST::MethodCall.new(
        :on('Ops'), :name($*BIND_CONTEXT ?? 'bind_lex' !! 'get_lex'),
        :type('RakudoObject'),
        'TC',
        JST::Literal.new( :value($name), :escape(1) )
    );
}

# Emits a lookup of a lexical in a scope outside the present one.
sub emit_outer_lexical_lookup($name) {
    if $*BIND_CONTEXT {
        pir::die("Cannot bind to something using scope 'outer'.");
    }
    my $lookup := emit_op('get_lex_skip_current',
        JST::Literal.new( :value($name), :escape(1) )
    );
    $lookup
}

# Emits a lookup of a dynamic var.
sub emit_dynamic_lookup($name) {
# TODO:
#   my $lookup := emit_op(($*BIND_CONTEXT ?? 'bind_dynamic' !! 'get_dynamic'),
#       JST::Literal.new( :value($name), :escape(1) )
#   );
#   if $*BIND_CONTEXT {
#       $lookup.push($*BIND_VALUE);
#   }
#   $lookup
    return JST::MethodCall.new(
        :on('Ops'), :name($*BIND_CONTEXT ?? 'bind_dynamic' !! 'get_dynamic'),
        :type('RakudoObject'),
        'TC',
        JST::Literal.new( :value($name), :escape(1) )
    );
}

# Emits the printing of something 
# XXX Debugging and Java only, silly.
sub emit_say($arg) {
    JST::Stmts.new(JST::MethodCall.new(
        :on('System.out'), :name('WriteLine'),
        :void(1),
        jst_for($arg)
    ), jst_for(PAST::Val.new( :value("") )))
}

sub temp_int($arg?, :$name) {
    JST::Local.new(
        :name(get_unique_id('int_' ~ ($name || ''))), :isdecl(1), :type('int'),
        pir::defined($arg) ?? jst_for($arg) !! lit(0)
    )
}

sub temp_str($arg?, :$name) {
    JST::Local.new(
        :name(get_unique_id('string_' ~ ($name || ''))), :isdecl(1), :type('String'),
        pir::defined($arg) ?? jst_for($arg) !! lits("")
    )
}

# Emits a boxing operation to an int/num/str.
sub box($type, $arg) {
    JST::MethodCall.new(
        :on('Ops'), :name("box_$type"), :type('RakudoObject'),
        TC(), jst_for($arg)
    )
}

# Emits the unboxing of a str/num/int.
sub unbox($type, $arg) {
    JST::MethodCall.new(
        :on('Ops'), :name("unbox_$type"),
        :type(vm_type_for($type)),
        TC(), jst_for($arg)
    )
}

# Maps a hand-wavey type (one of the three we box/unbox with) to a CLR type.
sub vm_type_for($type) {
    $type eq 'num' ?? 'double' !!
    $type eq 'str' ?? 'String' !!
    $type eq 'int' ?? 'int'    !!
    $type eq 'obj' ?? 'RakudoObject' !!
                      pir::die("Don't know VM type for $type")
}

sub plus($l, $r, $type?) {
    JST::Add.new(jst_for($l), jst_for($r), pir::defined($type) ?? $type !! 'int')
}

sub minus($l, $r, $type?) {
    JST::Subtract.new(jst_for($l), jst_for($r), pir::defined($type) ?? $type !! 'int')
}

sub bitwise_or($l, $r, $type?) {
    JST::BOR.new(jst_for($l), jst_for($r), pir::defined($type) ?? $type !! 'int')
}

sub bitwise_and($l, $r, $type?) {
    JST::BAND.new(jst_for($l), jst_for($r), pir::defined($type) ?? $type !! 'int')
}

sub bitwise_xor($l, $r, $type?) {
    JST::BXOR.new(jst_for($l), jst_for($r), pir::defined($type) ?? $type !! 'int')
}

sub gt($l, $r) {
    JST::GT.new(jst_for($l), jst_for($r), 'bool')
}

sub lt($l, $r) {
    JST::LT.new(jst_for($l), jst_for($r), 'bool')
}

sub ge($l, $r) {
    JST::GE.new(jst_for($l), jst_for($r), 'bool')
}

sub le($l, $r) {
    JST::LE.new(jst_for($l), jst_for($r), 'bool')
}

sub eq($l, $r) {
    JST::EQ.new(jst_for($l), jst_for($r), 'bool')
}

sub ne($l, $r) {
    JST::NE.new(jst_for($l), jst_for($r), 'bool')
}

sub not($operand) {
    JST::NOT.new(jst_for($operand), 'bool')
}

# short-circuiting logical AND
sub log_and($l, $r) {
    my $temp;
    JST::Stmts.new(
    ($temp := JST::Local.new(
        :name(get_unique_id('log_and')), :isdecl(1), :type('bool'), lit('false')
    )),
    if_then(JST::Local.new(
        :name(get_unique_id('left_bool')), :isdecl(1), :type('bool'), jst_for($l)
    ), if_then(JST::Local.new(
        :name(get_unique_id('right_bool')), :isdecl(1), :type('bool'), jst_for($r)
    ), JST::Bind.new(
    ### XXX The next line works only with the C# backend (so far)
    ###   b/c the Bind causes the Temp to be redeclared without the lit(___.name)
    lit($temp.name)
    , lit('true')))));
}

# short-circuiting logical OR
sub log_or($l, $r) {
    my $temp;
    JST::Stmts.new(
    ($temp := JST::Local.new(
        :name(get_unique_id('log_or')), :isdecl(1), :type('bool'), lit('false')
    )),
    if_then(JST::Local.new(
        :name(get_unique_id('left_bool')), :isdecl(1), :type('bool'), jst_for($l)
    ),
    JST::Bind.new(lit($temp.name), lit('true')),
    if_then(JST::Local.new(
        :name(get_unique_id('right_bool')), :isdecl(1), :type('bool'), jst_for($r)
    ),
    JST::Bind.new(lit($temp.name), lit('true')),
    )));
}

sub log_xor($l, $r) {
    JST::XOR.new(jst_for($l), jst_for($r), 'bool')
}

sub if_then($cond, $pred, $oth?, :$bool?) {
    pir::defined($oth)
        ?? JST::If.new($cond, $pred, $oth, :bool(pir::defined($bool) ?? $bool !! 1), :result(0))
        !! JST::If.new($cond, $pred, :bool(pir::defined($bool) ?? $bool !! 1), :result(0))
}

sub lits($str) {
    JST::Literal.new( :value($str), :escape(1))
}

sub lit($str) {
    $str ~~ JST::Literal
        ?? $str
        !! JST::Literal.new( :value($str), :escape(0))
}

sub val($val) {
    $val ~~ JST::Node
        ?? $val
        !! jst_for(PAST::Val.new( :value($val) ))
}

sub emit_op($name, *@args) {
    # See if we have any info on this op's siggy.
    my $sig := %nqp_op_sigs{$name};
    my $type := 'obj';
    if pir::defined($sig) {
        $type := $sig[0];
    }
    
    # Compile the args.
    my @jst_args;
    my $i := 1;
    for @args {
        # Set the type context that is desired.
        my $*TYPE_CONTEXT := pir::defined($sig) ?? $sig[$i] !! 'obj';
        my $arg_jst := jst_for($_);
        
        # We may need to auto-unbox it if we don't have the desired type
        # of thing.
        if $*TYPE_CONTEXT ne 'obj' {
            unless ($arg_jst ~~ JST::MethodCall || $arg_jst ~~ JST::Call || $arg_jst ~~ JST::Literal)
              && $arg_jst.type eq vm_type_for($*TYPE_CONTEXT) {
                $arg_jst := unbox($*TYPE_CONTEXT, $arg_jst);
            }
        }

        @jst_args.push($arg_jst);
    }

    # Build op call.
    #pir::say("name is $name; type is $type; jst_arg count is " ~ +@jst_args);
    my $call := JST::MethodCall.new(
        :on('Ops'), :name($name),
        :type(vm_type_for($type)),
        TC(), |@jst_args
    );

    # We may need to auto-box it.
    $type ne $*TYPE_CONTEXT && $*TYPE_CONTEXT eq 'obj' ??
        box($type, $call) !!
        $call
}

sub emit_call($on, $name, $type, *@args) {
    my @jst_args;
    for @args {
        @jst_args.push(jst_for($_))
    }
    JST::MethodCall.new(
        :on($on), :name($name),
        :type($type),
        |@jst_args
    )
}

sub returns_array($expr, *@result_slots) {
    my $tmp;
    my $stmts := JST::Stmts.new(
        $tmp := JST::Local.new(
            :type('RakudoObject'),
            :name(get_unique_id('array_result')),
            :isdecl(1),
            $expr
        )
    );
    my $i := 0;
    while $i < +@result_slots {
        $stmts.push(JST::Bind.new(
            @result_slots[$i],
            @result_slots[$i + 1] eq 'int'
            ?? unbox('int', emit_op('lllist_get_at_pos',
                JST::Local.new(:name($tmp.name)),
                lit(~($i / 2))))
            !! 
            @result_slots[$i + 1] eq 'String'
            ?? unbox('str', emit_op('lllist_get_at_pos',
                JST::Local.new(:name($tmp.name)),
                lit(~($i / 2))))
            !! emit_op('lllist_get_at_pos',
                JST::Local.new(:name($tmp.name)),
                lit(~($i / 2)))
        ));
        $i := $i + 2;
    };
    $stmts
}

# Returns a JST::Local for looking up the variable name with the
# given type. Default type is RakudoObject.
sub loc($name, $type = 'RakudoObject') {
    JST::Local.new( :name($name), :type($type) )
}

# Returns a JST::Local referencing the current thread context.
sub TC() {
    loc('TC', 'ThreadContext')
}
