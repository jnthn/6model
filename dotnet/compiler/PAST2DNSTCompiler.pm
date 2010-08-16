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

    # Any loadinits we'll need to run.
    my @*LOADINITS;

    # We'll build a static block info array too; this helps us do so.
    my $*OUTER_SBI := 0;
    my $*SBI_POS := 1;
    my $*SBI_SETUP := DNST::Stmts.new();

    # Also need to track the PAST blocks we're in.
    my @*PAST_BLOCKS;

    # Compile the node; ensure it is an immediate block.
    $node.blocktype('immediate');
    my $main_block_call := dnst_for($node);

    # Build a class node and add the inner code blocks.
    my $class := DNST::Class.new(
        :name('RakudoOutput') # XXX
    );
    for @*INNER_BLOCKS {
        $class.push($_);
    }

    # Also need to include setup of static block info.
    $class.push(DNST::Attribute.new( :name('StaticBlockInfo'), :type('RakudoCodeRef.Instance[]') ));
    $class.push(make_blocks_init_method('blocks_init'));

    # Calls to loadinits.
    my $loadinit_calls := DNST::Stmts.new();
    for @*LOADINITS {
        $loadinit_calls.push($_);
    }

    # Finally, startup handling code.
    # XXX This will need to change some day for modules.
    $class.push(DNST::Method.new(
        :name('Main'),
        :return_type('void'),
        DNST::Temp.new( :name('TC'), :type('var'),
            DNST::MethodCall.new( :on('Rakudo.Init'), :name('Initialize') )
        ),
        DNST::Call.new(
            :name('blocks_init'),
            :void(1),
            'TC'
        ),
        $loadinit_calls,
        $main_block_call
    ));

    # Package up in a compilation unit with the required "using"s.
    return DNST::CompilationUnit.new(
        DNST::Using.new( :namespace('System') ),
        DNST::Using.new( :namespace('System.Collections.Generic') ),
        DNST::Using.new( :namespace('Rakudo.Runtime') ),
        DNST::Using.new( :namespace('Rakudo.Metamodel') ),
        DNST::Using.new( :namespace('Rakudo.Metamodel.Representations') ),
        $class
    );
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
                'null', 'null',
                DNST::ArrayLiteral.new( :type('String') ),
                'null'
            )
        ),
        DNST::Bind.new(
            'StaticBlockInfo[0].CurrentContext',
            'TC.CurrentContext'
        ),

        # The others.
        $*SBI_SETUP
    );
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
    
    # Fresh bind context.
    my $*BIND_CONTEXT := 0;

    # Setup static block info.
    my $outer_sbi := $*OUTER_SBI;
    my $our_sbi := $*SBI_POS;
    my $our_sbi_setup := DNST::MethodCall.new( :on('CodeObjectUtility'), :name('BuildStaticBlockInfo') );
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
        :params('ThreadContext TC', 'IRakudoObject Block', 'IRakudoObject Capture'),
        :return_type('IRakudoObject')
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

    # See if we have a loadinit.
    if +@($block.loadinit) {
        my $*OUTER_SBI := $our_sbi;
        my @*INNER_BLOCKS;

        # We'll fake this as an inner block to compile.
        @*LOADINITS.push(dnst_for(PAST::Block.new(
            :blocktype('immediate'), $block.loadinit
        )));

        # Add blocks from this compilation (probably just one,
        # but handle nested blocks from the loadinit).
        for @*INNER_BLOCKS {
            @inner_blocks.push($_);
        }
    }

    # Before start of statements, we want to bind the signature.
    $stmts.unshift(DNST::MethodCall.new(
        :on('SignatureBinder'), :name('Bind'), :void(1), 'C', 'Capture'
    ));

    # Wrap in block prelude/postlude.
    $result.push(DNST::Temp.new(
        :name('C'), :type('var'),
        DNST::New.new( :type('Context'), "StaticBlockInfo[$our_sbi]", "TC.CurrentContext" )
    ));
    $result.push(DNST::Bind.new( 'TC.CurrentContext', 'C' ));
    $result.push(DNST::TryFinally.new(
        $stmts,
        DNST::Bind.new( 'TC.CurrentContext', 'C.Caller' )
    ));
    
    # Add nested inner blocks after it (.Net does not support
    # nested blocks).
    @*INNER_BLOCKS.push($result);
    for @inner_blocks {
        @*INNER_BLOCKS.push($_);
    }

    # Finish geneating code setup block call.
    $our_sbi_setup.push(DNST::New.new(
        :type('Func<ThreadContext, IRakudoObject, IRakudoObject, IRakudoObject>'),
        $result.name
    ));
    $our_sbi_setup.push("StaticBlockInfo[$outer_sbi]");
    my $lex_setup := DNST::ArrayLiteral.new( :type('string') );
    for @*LEXICALS {
        $lex_setup.push(DNST::Literal.new( :value($_), :escape(1) ));
    }
    $our_sbi_setup.push($lex_setup);
    $our_sbi_setup.push(compile_signature(@*PARAMS));

    # Clear up this PAST::Block from the blocks list.
    @*PAST_BLOCKS.shift;

    # For immediate evaluate to a call; for declaration, evaluate to the
    # low level code object.
    if $block.blocktype eq 'immediate' {
        return DNST::MethodCall.new(
            :name('STable.Invoke'),
            "StaticBlockInfo[$our_sbi]",
            'TC',
            "StaticBlockInfo[$our_sbi]",
            DNST::MethodCall.new(
                :on('CaptureHelper'),
                :name('FormWith')
            )
        );
    }
    else {
        return "StaticBlockInfo[$our_sbi]";
    }
}

# Compiles a bunch of parameter nodes down to a signature.
# XXX Doesn't handle default values just yet.
sub compile_signature(@params) {
    # Go through each of the parameters and compile them.
    my $params := DNST::ArrayLiteral.new( :type('Parameter') );
    for @params {
        my $param := DNST::New.new( :type('Parameter') );

        # Type. XXX TODO.
        $param.push('null');

        # Variable name to bind into.
        $param.push(DNST::Literal.new( :value($_.name), :escape(1) ));

        # Named param or not?
        $param.push((!$_.slurpy && $_.named) ??
            DNST::Literal.new( :value(pir::substr($_.name, 1)), :escape(1) ) !!
            'null');

        # Flags.
        $param.push(
            $_.viviself           ?? 'Parameter.OPTIONAL_FLAG'     !!
            $_.slurpy && $_.named ?? 'Parameter.NAMED_SLURPY_FLAG' !!
            $_.slurpy             ?? 'Parameter.POS_SLURPY_FLAG'   !!
            '0');

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
            :name(get_unique_id('inv')), :type('var'),
            dnst_for(@args.shift)
        );

        # Method lookup.
        my $callee := DNST::Temp.new(
            :name(get_unique_id('callee')), :type('var'),
            DNST::MethodCall.new(
                :on($inv.name), :name('STable.FindMethod'),
                'TC',
                $inv.name,
                DNST::Literal.new( :value($op.name), :escape(1) ),
                'Hints.NO_HINT'
            )
        );
        
        # How is capture formed?
        my $capture := DNST::MethodCall.new(
            :on('CaptureHelper'), :name('FormWith')
        );
        my $pos_part := DNST::ArrayLiteral.new(
            :type('IRakudoObject'),
            $inv.name
        );
        my $named_part := DNST::DictionaryLiteral.new(
            :key_type('string'), :value_type('IRakudoObject') );
        for @args {
            if $_.named {
                $named_part.push(DNST::Literal.new( :value($_.named), :escape(1) ));
                $named_part.push(dnst_for($_));
            }
            else {
                $pos_part.push(dnst_for($_));
            }
        }
        $capture.push($pos_part);
        if +@($named_part) { $capture.push($named_part); }

        # Emit the call.
        return DNST::Stmts.new(
            $inv,
            DNST::MethodCall.new(
                :name('STable.Invoke'),
                $callee,
                'TC',
                $callee.name,
                $capture
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
        $callee := DNST::Temp.new( :name(get_unique_id('callee')), :type('var'), $callee );

        # How is capture formed?
        my $capture := DNST::MethodCall.new(
            :on('CaptureHelper'), :name('FormWith')
        );
        my $pos_part := DNST::ArrayLiteral.new( :type('IRakudoObject') );
        my $named_part := DNST::DictionaryLiteral.new(
            :key_type('string'), :value_type('IRakudoObject') );
        for @args {
            if $_.named {
                $named_part.push(DNST::Literal.new( :value($_.named), :escape(1) ));
                $named_part.push(dnst_for($_));
            }
            else {
                $pos_part.push(dnst_for($_));
            }
        }
        $capture.push($pos_part);
        if +@($named_part) { $capture.push($named_part); }

        # Emit call.
        return DNST::MethodCall.new(
            :name('STable.Invoke'),
            $callee,
            'TC',
            $callee.name,
            $capture
        );
    }

    elsif $op.pasttype eq 'bind' {
        # Construct DNST for LHS in bind context.
        my $lhs;
        {
            my $*BIND_CONTEXT := 1;
            $lhs := dnst_for((@($op))[0]);
        }

        # Now push onto that the evaluated RHS.
        $lhs.push(dnst_for((@($op))[1]));

        return $lhs;
    }

    elsif $op.pasttype eq 'nqpop' {
        # Just a call on the Ops class.
        my $result := DNST::MethodCall.new(
            :on('Ops'), :name($op.name)
        );
        for @($op) {
            $result.push(dnst_for($_));
        }
        return $result;
    }

    else {
        pir::die("Don't know how to compile pasttype " ~ $op.pasttype);
    }
}

# Emits a value.
our multi sub dnst_for(PAST::Val $val) {
    # If it's a block reference, hand back the SBI.
    if $val.value ~~ PAST::Block {
        unless $val.value<SBI> {
            pir::die("Can't use PAST::Val for a block reference for an as-yet uncompiled block");
        }
        return $val.value<SBI>;
    }

    # Look up the type to box to.
    my $type := $val.returns || (
        # XXX This is a bit of a Parrot-specific hack.
        pir::isa($val.value, 'Integer') ?? 'Int' !!
        pir::isa($val.value, 'String') ?? 'Str' !!
        pir::isa($val.value, 'Float') ?? 'Num' !!
        pir::die("Can not detect type of value")
    );
    my $type_dnst := emit_lexical_lookup($type);
    
    # Emit code to do the boxing.
    return DNST::MethodCall.new(
        :on('Ops'), :name('box'),
        DNST::Literal.new( :value($val.value), :escape($type eq 'Str') ),
        $type_dnst
    );
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
        if $var.isdecl { @*LEXICALS.push($var.name); }
        return emit_lexical_lookup($var.name);
    }
    elsif $scope eq 'contextual' {
        if $var.isdecl { @*LEXICALS.push($var.name); }
        return emit_dynamic_lookup($var.name);
    }
    elsif $scope eq 'package' {
        if $var.isdecl { pir::die("Don't know how to handle is_decl on package"); }

        # Get all parts of the name.
        my @parts;
        if $var.namespace {
            for $var.namespace { @parts.push($_); }
        }
        @parts.push($var.name);

        # First, we need to look up the first part.
        my $lookup := emit_lexical_lookup(@parts.shift);

        # Now chase down the rest.
        for @parts {
            # XXX todo: wrap the lookup in postcircumfix:<{ }> call(s).
            pir::die('Multi-level package lookups NYI');
        }

        return $lookup;
    }
    elsif $scope eq 'register' {
        if $var.isdecl {
            my $result := DNST::Temp.new( :name($var.name), :type('IRakudoObject') );
            unless $*BIND_CONTEXT { $result.push('null'); }
            return $result;
        }
        elsif $*BIND_CONTEXT {
            return DNST::Bind.new( $var.name );
        }
        else {
            return $var.name;
        }
    }
    else {
        pir::die("Don't know how to compile variable scope " ~ $var.scope);
    }
}

# Catch-all for error detection.
our multi sub dnst_for($any) {
    pir::die("Don't know how to compile a " ~ pir::typeof__SP($any) ~ "(" ~ $any ~ ")");
}

# Emits a lookup of a lexical.
sub emit_lexical_lookup($name) {
    return DNST::MethodCall.new(
        :on('Ops'), :name($*BIND_CONTEXT ?? 'bind_lex' !! 'get_lex'),
        'C',
        DNST::Literal.new( :value($name), :escape(1) )
    );
}

# Emits a lookup of a dynamic var.
sub emit_dynamic_lookup($name) {
    return DNST::MethodCall.new(
        :on('Ops'), :name($*BIND_CONTEXT ?? 'bind_dynamic' !! 'get_dynamic'),
        'C',
        DNST::Literal.new( :value($name), :escape(1) )
    );
}
