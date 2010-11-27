# This compiles a .Net Syntax Tree down to C#.
class DNST2CSharpCompiler;

method compile(DNST::Node $node) {
    my $*CUR_ID := 0;
    return cs_for($node);
}

# Quick hack so we can get unique (for this compilation) IDs.
sub get_unique_id($prefix) {
    $*CUR_ID := $*CUR_ID + 1;
    return $prefix ~ '_' ~ $*CUR_ID;
}

our multi sub cs_for(DNST::CompilationUnit $node) {
    my @*USINGS;
    my $main := '';
    for @($node) {
        $main := $main ~ cs_for($_);
    }
    my $code := '';
    for @*USINGS {
        $code := $code ~ $_;
    }
    return $code ~ $main;
}

our multi sub cs_for(DNST::Using $using) {
    @*USINGS.push("using " ~ $using.namespace ~ ";\n");
    return '';
}

our multi sub cs_for(DNST::Class $class) {
    my $code := '';
    if $class.namespace {
        $code := $code ~ 'namespace ' ~ $class.namespace ~ " \{\n";
    }
    $code := $code ~ 'class ' ~ $class.name ~ " \{\n";
    for @($class) {
        $code := $code ~ cs_for($_);
    }
    $code := $code ~ "}\n";
    if $class.namespace {
        $code := $code ~ "}\n";
    }
    return $code;
}

our multi sub cs_for(DNST::Attribute $attr) {
    return '    private static ' ~ $attr.type ~ ' ' ~ $attr.name ~ ";\n";
}

our multi sub cs_for(DNST::Method $meth) {
    my $*LAST_TEMP := '';

    # Method header.
    my $code := '    private static ' ~
        $meth.return_type ~ ' ' ~ 
        $meth.name ~ '(' ~
        pir::join(', ', $meth.params) ~
        ") \{\n";

    # Emit everything in the method.
    for @($meth) {
        $code := $code ~ cs_for($_);
    }

    # Return statement if needed, and block ending.
    unless $meth.return_type eq 'void' {
        $code := $code ~ "        return $*LAST_TEMP;\n";
    }
    return $code ~ "    }\n\n";
}

our multi sub cs_for(DNST::Stmts $stmts) {
    my $code := '';
    for @($stmts) {
        $code := $code ~ cs_for($_);
    }
    return $code;
}

our multi sub cs_for(DNST::TryFinally $tf) {
    unless +@($tf) == 2 { pir::die('DNST::TryFinally nodes must have 2 children') }
    my $try_result := get_unique_id('try_result');
    my $code := "        RakudoObject $try_result;\n" ~
                "        try \{\n" ~
                cs_for((@($tf))[0]);
    $code := $code ~
                "        $try_result = $*LAST_TEMP;\n" ~
                "        } finally \{\n" ~
                cs_for((@($tf))[1]) ~
                "        }\n";
    $*LAST_TEMP := $try_result;
    return $code;
}

our multi sub cs_for(DNST::TryCatch $tc) {
    unless +@($tc) == 2 { pir::die('DNST::TryCatch nodes must have 2 children') }
    my $try_result := get_unique_id('try_result');
    my $code := "        RakudoObject $try_result;\n" ~
                "        try \{\n" ~
                cs_for((@($tc))[0]);
    $code := $code ~
                "        $try_result = $*LAST_TEMP;\n" ~
                "        } catch(" ~ $tc.exception_type ~ " " ~ $tc.exception_var ~ ")\{\n" ~
                cs_for((@($tc))[1]) ~
                "        $try_result = $*LAST_TEMP;\n" ~
                "        }\n";
    $*LAST_TEMP := $try_result;
    return $code;
}

our multi sub cs_for(DNST::MethodCall $mc) {
    # Code generate all the arguments.
    my @arg_names;
    my $code := '';
    for @($mc) {
        $code := $code ~ cs_for($_);
        @arg_names.push($*LAST_TEMP);
    }

    # What're we calling it on?
    my $invocant := $mc.on || @arg_names.shift;

    # Code-gen the call.
    $code := $code ~ '        ';
    unless $mc.void {
        my $ret_type := $mc.type || 'var';
        $*LAST_TEMP := get_unique_id('result');
        my $method_name := $invocant ~ '.' ~ $mc.name;
        # the next bit is very hacky, should be handled upstream in
        # PAST2DNSTCompiler.pm
        if $ret_type eq 'var' && $method_name eq 'Ops.unbox_str'
        {
            $ret_type := 'string';
        }
        if $ret_type eq 'var' && (
            $method_name eq 'Ops.multi_dispatch_over_lexical_candidates' ||
            $method_name eq 'Ops.throw_dynamic'
        ) {
            $ret_type := 'RakudoObject';
        }
        $code := $code ~ "$ret_type $*LAST_TEMP = ";
        if $ret_type eq 'var' { $code := $code ~ "// var from " ~ $invocant ~ "." ~ $mc.name ~ "\n"; }
    }
    $code := $code ~ "$invocant." ~ $mc.name ~
        "(" ~ pir::join(', ', @arg_names) ~ ");\n";
    return $code;
}

our multi sub cs_for(DNST::Call $mc) {
    # Code generate all the arguments.
    my @arg_names;
    my $code := '';
    for @($mc) {
        $code := $code ~ cs_for($_);
        @arg_names.push($*LAST_TEMP);
    }

    # Code-gen the call.
    $code := $code ~ '        ';
    unless $mc.void {
        $*LAST_TEMP := get_unique_id('result');
        $code := $code ~ "RakudoObject $*LAST_TEMP = ";
    }
    $code := $code ~ $mc.name ~
        "(" ~ pir::join(', ', @arg_names) ~ ");\n";

    return $code;
}

our multi sub cs_for(DNST::New $new) {
    # Code generate all the arguments.
    my @arg_names;
    my $code := '';
    for @($new) {
        $code := $code ~ cs_for($_);
        @arg_names.push($*LAST_TEMP);
    }

    # Code-gen the constructor call.
    $*LAST_TEMP := get_unique_id('new');
    $code := $code ~ "        " ~ $new.type ~ " $*LAST_TEMP = new " ~
        $new.type ~ "(" ~ pir::join(', ', @arg_names) ~ ");\n";

    return $code;
}

our multi sub cs_for(DNST::If $if) {
    unless +@($if) >= 2 { pir::die('A DNST::If node must have at least 2 children') }

    # Need a variable to put the final result in.
    my $if_result := get_unique_id('if_result') if $if.result;

    # Get the conditional and emit if.
    my $code := cs_for((@($if))[0]);
    $code := $code ~
             "        " ~ $if.type ~ " $if_result = null;\n" if $if.result;
    $code := $code ~
             "        if ($*LAST_TEMP" ~ ($if.bool ?? "" !! " != 0") ~ ") \{\n";

    # Compile branch(es).
    $*LAST_TEMP := 'null';
    $code := $code ~ cs_for((@($if))[1]);
    $code := $code ~ "        $if_result = $*LAST_TEMP;\n" if $if.result;
    $code := $code ~ "        }\n";
    if +@($if) == 3 {
        $*LAST_TEMP := 'null';
        $code := $code ~ "        else \{\n";
        $code := $code ~ cs_for((@($if))[2]);
        $code := $code ~ "        $if_result = $*LAST_TEMP;\n" if $if.result;
        $code := $code ~ "        }\n";
    }

    $*LAST_TEMP := $if_result if $if.result;
    return $code;
}

our multi sub cs_for(DNST::Return $ret) {
    return cs_for($ret.target) ~ "        return " ~ $*LAST_TEMP ~ ";\n";
}

our multi sub cs_for(DNST::Label $lab) {
    return "      " ~ $lab.name ~ ":\n";
}

our multi sub cs_for(DNST::Goto $gt) {
    return "        goto " ~ $gt.label ~ ";\n";
}

our multi sub cs_for(DNST::Temp $tmp) {
    unless +@($tmp) == 1 { pir::die('A DNST::Temp must have exactly one child') }
    my $code := cs_for((@($tmp))[0]);
    my $name := $tmp.name;
    $code := $code ~ "        " ~ $tmp.type ~ " $name = $*LAST_TEMP;\n";
    $*LAST_TEMP := $name;
    return $code;
}

our multi sub cs_for(DNST::Bind $bind) {
    unless +@($bind) == 2 { pir::die('DNST::Bind nodes must have 2 children') }
    my $code := cs_for((@($bind))[0]);
    my $lhs := $*LAST_TEMP;
    $code := $code ~ cs_for((@($bind))[1]);
    my $rhs := $*LAST_TEMP;
    $code := $code ~ "        $lhs = $rhs;\n";
    $*LAST_TEMP := $lhs;
    return $code;
}

our multi sub cs_for(DNST::Literal $lit) {
    $*LAST_TEMP := $lit.escape ??
        ('@"' ~ pir::join__ssp('""', pir::split__pss('"', ~$lit.value)) ~ '"') !!
        $lit.value;
    return '';
}

our multi sub cs_for(DNST::Local $loc) {
    $*LAST_TEMP := $loc.name;
    return '';
}

our multi sub cs_for(DNST::JumpTable $jt) {
    my $reg := $jt.register;
    my $skip_label := DNST::Label.new(:name('skip_jumptable_for_' ~ $reg.name));
    my $code := cs_for(DNST::Goto.new(:label($skip_label.name)));
    $code := $code ~ cs_for($jt.label);
    $code := '        switch( ' ~ $reg.name ~ " ) \{\n";
    my $i := 0;
    for $jt.labels {
        $code := $code ~ "        case $i : goto " ~ $_.name ~ ";\n";
        $i := $i + 1;
    }
    $code := $code ~ "        }\n" ~ cs_for($skip_label);
    return $code;
}

sub lhs_rhs_op(@ops, $op) {
    my $code := cs_for(@ops[0]);
    my $lhs := $*LAST_TEMP;
    $code := $code ~ cs_for(@ops[1]);
    my $rhs := $*LAST_TEMP;
    $*LAST_TEMP := get_unique_id('expr_result');
    # @ops[2] is the type
    return "$code        " ~ @ops[2] ~ " $*LAST_TEMP = $lhs $op $rhs;\n";
}

our multi sub cs_for(DNST::Add $ops) {
    lhs_rhs_op(@($ops), '+')
}

our multi sub cs_for(DNST::Subtract $ops) {
    lhs_rhs_op(@($ops), '-')
}

our multi sub cs_for(DNST::GT $ops) {
    lhs_rhs_op(@($ops), '>')
}

our multi sub cs_for(DNST::LT $ops) {
    lhs_rhs_op(@($ops), '<')
}

our multi sub cs_for(DNST::GE $ops) {
    lhs_rhs_op(@($ops), '>=')
}

our multi sub cs_for(DNST::LE $ops) {
    lhs_rhs_op(@($ops), '<=')
}

our multi sub cs_for(DNST::EQ $ops) {
    lhs_rhs_op(@($ops), '==')
}

our multi sub cs_for(DNST::NE $ops) {
    lhs_rhs_op(@($ops), '!=')
}

our multi sub cs_for(DNST::OR $ops) {
    lhs_rhs_op(@($ops), '||')
}

our multi sub cs_for(DNST::AND $ops) {
    lhs_rhs_op(@($ops), '&&')
}

our multi sub cs_for(DNST::BOR $ops) {
    lhs_rhs_op(@($ops), '|')
}

our multi sub cs_for(DNST::BAND $ops) {
    lhs_rhs_op(@($ops), '&')
}

our multi sub cs_for(DNST::BXOR $ops) {
    lhs_rhs_op(@($ops), '^')
}

our multi sub cs_for(DNST::NOT $ops) {
    my $code := cs_for((@(ops))[0]);
    my $lhs := $*LAST_TEMP;
    $*LAST_TEMP := get_unique_id('expr_result_negated');
    return "$code        bool $*LAST_TEMP = !$lhs;\n";
}

our multi sub cs_for(DNST::XOR $ops) {
    my $code := cs_for((@(ops))[0]);
    my $lhs := $*LAST_TEMP;
    $code := $code ~ cs_for((@(ops))[0]);
    my $rhs := $*LAST_TEMP;
    $*LAST_TEMP := get_unique_id('expr_result');
    return "$code        bool $*LAST_TEMP = $lhs ? ! $rhs : $rhs;\n";
}

our multi sub cs_for(DNST::Throw $throw) {
    $*LAST_TEMP := 'null';
    return '        throw;';
}

our multi sub cs_for(String $s) {
    $*LAST_TEMP := $s;
    return '';
}

our multi sub cs_for(DNST::ArrayLiteral $al) {
    # Code-gen all the things to go in the array.
    my @item_names;
    my $code := '';
    for @($al) {
        $code := $code ~ cs_for($_);
        @item_names.push($*LAST_TEMP);
    }

    # Code-gen the array.
    $*LAST_TEMP := 'new ' ~ $al.type ~ '[] {' ~ pir::join(',', @item_names) ~ '}';
    return $code;
}

our multi sub cs_for(DNST::DictionaryLiteral $dl) {
    # Code-gen all the pieces that will go into the dictionary. The
    # list is key,value,key,value.
    my @items;
    my $code := '';
    for @($dl) -> $k, $v {
        $code := $code ~ cs_for($k);
        my $key := $*LAST_TEMP;
        $code := $code ~ cs_for($v);
        my $value := $*LAST_TEMP;
        @items.push('{ ' ~ $key ~ ', ' ~ $value ~ ' }');
    }

    # Code-gen the dictionary.
    $*LAST_TEMP := "new Dictionary<" ~ $dl.key_type ~ ', ' ~ $dl.value_type ~ '>() { ' ~
        pir::join(',', @items) ~ ' }';
    return $code;
}

our multi sub cs_for($any) {
    pir::die("DNST to C# compiler doesn't know how to compile a " ~ pir::typeof__SP($any));
}
