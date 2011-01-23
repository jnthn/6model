# Compile a Java Syntax Tree down to Java.
class JST2JavaCompiler;

method compile(JST::Node $node) {
    my $*CUR_ID := 0;
    my %*CUR_ID;
    return java_for($node);
}

# Quick hack so we can get unique (for this compilation) IDs.
sub get_unique_id($prefix) {
    $*CUR_ID := $*CUR_ID + 1;
    return $prefix ~ '_' ~ $*CUR_ID;
}

# A slightly troubleshooting-friendlier alternative to get_unique_id
sub get_unique_global_name($prefix, $suffix) {
    my $number := 1;
    if %*CUR_ID{$prefix} > 0 { $number := %*CUR_ID{$prefix} + 1; }
    %*CUR_ID{$prefix} := $number;
    my $result := $prefix ~ '_' ~ $number;
    if $suffix ne '' { $result := $result ~ '_' ~ $suffix; }
    return $result;
}

our multi sub java_for(JST::CompilationUnit $node) {
    my @*USINGS;
    my $main := '';
    for @($node) {
        $main := $main ~ java_for($_);
    }
    my $code := '';
    for @*USINGS {
        $code := $code ~ $_;
    }
    return $code ~ $main;
}

our multi sub java_for(JST::Using $using) {
    @*USINGS.push("import " ~ $using.namespace ~ "; // JST::Using\n");
    return '';
}

our multi sub java_for(JST::Class $class) {
    my $code := '';
    if $class.namespace {
        $code := $code ~ 'package ' ~ $class.namespce ~ ";\n";
    }
    $code := $code ~ 'public class ' ~ $class.name ~ " \{ // JST::Class\n"; # the C# version omits public
    for @($class) {
        $code := $code ~ java_for($_);
    }
    $code := $code ~ "}\n";
    return $code;
}

our multi sub java_for(JST::Attribute $attr) {
    return '    private static ' ~ $attr.type ~ ' ' ~ $attr.name ~ "; // JST:Attribute\n";
}

our multi sub java_for(JST::Method $meth) {
    my $*LAST_TEMP := '';

    # Method header.
    my $code := '    public static ' ~ # the C# version has private
        $meth.return_type ~ ' ' ~ 
        $meth.name ~ '(' ~
        pir::join(', ', $meth.params) ~
        ") \{ // JST::Method\n";

    # Emit everything in the method.
    for @($meth) {
        $code := $code ~ java_for($_);
    }

    # Return statement if needed, and block ending.
    unless $meth.return_type eq 'void' {
        $code := $code ~ "        return $*LAST_TEMP;\n";
    }
    return $code ~ "    }\n\n";
}

our multi sub java_for(JST::Stmts $stmts) {
    my $code := '';
    for @($stmts) {
        $code := $code ~ java_for($_);
    }
    return $code;
}

our multi sub java_for(JST::TryFinally $tf) {
    unless +@($tf) == 2 { pir::die('JST::TryFinally nodes must have 2 children') }
    my $try_result := get_unique_global_name('try_result','');
    my $code := "        RakudoObject $try_result; // JST::TryFinally\n" ~
                "        try \{\n" ~
                java_for((@($tf))[0]);
    $code := $code ~
                "        $try_result = $*LAST_TEMP;\n" ~
                "        } finally \{\n" ~
                java_for((@($tf))[1]) ~
                "        }\n";
    $*LAST_TEMP := $try_result;
    return $code;
}

our multi sub java_for(JST::TryCatch $tc) {
    unless +@($tc) == 2 { pir::die('JST::TryCatch nodes must have 2 children') }
    my $try_result := get_unique_global_name('trycatch_result','');
    my $code := "        RakudoObject $try_result;\n" ~
                "        try \{\n" ~
                java_for((@($tc))[0]);
    $code := $code ~
                "        $try_result = $*LAST_TEMP;\n" ~
                "        } catch(" ~ $tc.exception_type ~ " " ~ $tc.exception_var ~ ")\{\n" ~
                java_for((@($tc))[1]) ~
                "        $try_result = $*LAST_TEMP;\n" ~
                "        }\n";
    $*LAST_TEMP := $try_result;
    return $code;
}

our multi sub java_for(JST::MethodCall $mc) {
    # Code generate all the arguments.
    my @arg_names;
    my $code := '';
    for @($mc) {
        $code := $code ~ java_for($_);
        @arg_names.push($*LAST_TEMP);
    }

    # What'we we calling it on?
    my $invocant := $mc.on || @arg_names.shift;

    # Code-gen the call.
    $code := $code ~ '        ';
    unless $mc.void {
        my $ret_type := $mc.type || 'var';
        $*LAST_TEMP := get_unique_global_name('result','methcall');
        my $method_name := $invocant ~ "." ~ $mc.name;
        # the next bit is very hacky, should be handled upstream in
        # PAST2JSTCompiler.pm
        if $ret_type eq 'var' && $method_name eq 'Ops.unbox_str'
        {
            $ret_type := 'String';
        }
        if $ret_type eq 'var' && (
#           $method_name eq 'CaptureHelper.FormWith' ||
#           $method_name eq 'Ops.add_int' ||
#           $method_name eq 'Ops.box_str' ||
#           $method_name eq 'Ops.coerce_int_to_num' ||
#           $method_name eq 'Ops.coerce_int_to_str' ||
#           $method_name eq 'Ops.coerce_num_to_int' ||
#           $method_name eq 'Ops.coerce_num_to_str' ||
#           $method_name eq 'Ops.concat' ||
#           $method_name eq 'Ops.div_int' ||
#           $method_name eq 'Ops.equal_ints' ||
#           $method_name eq 'Ops.equal_nums' ||
#           $method_name eq 'Ops.equal_strs' ||
#           $method_name eq 'Ops.get_how' ||
#           $method_name eq 'Ops.get_what' ||
#           $method_name eq 'Ops.instance_of' ||
            $method_name eq 'Ops.leave_block' ||
#           $method_name eq 'Ops.lllist_elems' ||
#           $method_name eq 'Ops.lllist_get_at_pos' ||
#           $method_name eq 'Ops.logical_not_int' ||
            $method_name eq 'Ops.multi_dispatch_over_lexical_candidates' ||
#           $method_name eq 'Ops.mod_int' ||
#           $method_name eq 'Ops.mul_int' ||
#           $method_name eq 'Ops.sub_int' ||
            $method_name eq 'Ops.throw_dynamic' ||
            $method_name eq 'Ops.throw_lexical'
        ) {
            $ret_type := 'RakudoObject';
        }
        $code := $code ~ "$ret_type $*LAST_TEMP = ";
        if $ret_type eq 'var' { $code := $code ~ "// var from " ~ $invocant ~ "." ~ $mc.name ~ "\n"; }
    }
    $code := $code ~ "$invocant." ~ $mc.name ~
        "(" ~ pir::join(', ', @arg_names) ~ "); // JST::MethodCall\n";
    return $code;
}

our multi sub java_for(JST::Call $mc) {
    # Code generate all the arguments.
    my @arg_names;
    my $code := '';
    for @($mc) {
        $code := $code ~ java_for($_);
        @arg_names.push($*LAST_TEMP);
    }

    # Code-gen the call.
    $code := $code ~ '        ';
    unless $mc.void {
        $*LAST_TEMP := get_unique_global_name('result','call');
        $code := $code ~ "RakudoObject $*LAST_TEMP = ";
    }
    $code := $code ~ $mc.name ~
        "(" ~ pir::join(', ', @arg_names) ~ "); // JST::Call\n";

    return $code;
}

our multi sub java_for(JST::New $new) {
    # Code generate all the arguments.
    my @arg_names;
    my $code := '';
    for @($new) {
        $code := $code ~ java_for($_);
        @arg_names.push($*LAST_TEMP);
    }

    # Code-gen the constructor call.
    $*LAST_TEMP := get_unique_global_name('new','');
    $code := $code ~ "        " ~ $new.type ~ " $*LAST_TEMP = new ";
    $code := $code ~ $new.type;
    if $new.type eq 'RakudoCodeRef.IFunc_Body' {
        my $block_name := (@($new))[0];
        $code := $code ~ "() \{ public RakudoObject Invoke( ThreadContext TC, RakudoObject Obj, RakudoObject Cap ) \{ return $block_name(TC, Obj, Cap);\}\}";
    }
    else {
        $code := $code ~ "(" ~ pir::join(', ', @arg_names) ~ ")";
    }
    $code := $code ~ "; // JST::New\n";
    return $code;
}

our multi sub java_for(JST::If $if) {
    unless +@($if) >= 2 { pir::die('A JST::If node must have at least 2 children') }

    # Need a variable to put the final result in.
    my $if_result := get_unique_global_name('if_result','');

    # Get the conditional and emit if.
    my $code := java_for((@($if))[0]);
    $code := $code ~
             "        RakudoObject $if_result = null; // JST::If A\n" ~
             "        if ($*LAST_TEMP != 0) \{\n";

    # Compile branch(es).
    $*LAST_TEMP := 'null';
    $code := $code ~ java_for((@($if))[1]);
    $code := $code ~ "        // $if_result = $*LAST_TEMP; // JST::If B\n" ~
                     "        }\n";
    if +@($if) == 3 {
        $*LAST_TEMP := 'null';
        $code := $code ~ "        else \{\n";
        $code := $code ~ java_for((@($if))[2]);
        $code := $code ~ "        $if_result = $*LAST_TEMP; // JST::If C\n" ~
                         "        }\n";
    }

    $*LAST_TEMP := $if_result;
    return $code;
}

our multi sub java_for(JST::Label $lab) {
    return "      " ~ $lab.name ~ ": ; // JST::Label\n";
}

our multi sub java_for(JST::Goto $gt) {
    return "// DO NOT WANT  goto " ~ $gt.label ~ "; // JST::Goto\n";
}

our multi sub java_for(JST::Bind $bind) {
    unless +@($bind) == 2 { pir::die('JST::Bind nodes must have 2 children') }
    my $code := java_for((@($bind))[0]);
    my $lhs := $*LAST_TEMP;
    $code := $code ~ java_for((@($bind))[1]);
    my $rhs := $*LAST_TEMP;
    $code := $code ~ "        $lhs = $rhs; // JST::Bind\n";
    $*LAST_TEMP := $lhs;
    return $code;
}

our multi sub java_for(JST::Literal $lit) {
    if $lit.escape {  # the C# version is much simpler because @"" strings can contain control characters
        my $str_in := $lit.value;
        my $str_out := '';
        while pir::length($str_in) {
            my $char := pir::substr($str_in, 0, 1);
            $str_in := pir::substr($str_in, 1);
            if $char eq "\n" { $char := "\\n"; }
            if $char eq "\t" { $char := "\\t"; }
            $str_out := $str_out ~ $char;
        }
        $*LAST_TEMP := '"' ~ $str_out ~ '"'
    } else {
        $*LAST_TEMP := $lit.value;
    }
    return '';
}

our multi sub java_for(JST::Null $null) {
    $*LAST_TEMP := 'null';
    return '';
}

our multi sub java_for(JST::Local $loc) {
    my $code := '';
    if $loc.isdecl {
        unless +@($loc) == 1 {
            pir::die('A JST::Local with isdecl set must have exactly one child')
        }
        unless $loc.type {
            pir::die('JST::Local with isdecl requires type');
        }
        $code := java_for((@($loc))[0]);
        $code := $code ~ '        ' ~ $loc.type ~ ' ' ~ $loc.name ~ " = $*LAST_TEMP;\n";
    } elsif +@($loc) != 0 {
        pir::die('A JST::Local without isdecl set must have no children')
    }
    $*LAST_TEMP := $loc.name;
    return $code;
}

our multi sub java_for(JST::JumpTable $jt) {
    my $reg := $jt.register;
    my $skip_label := JST::Label.new(:name('skip_jumptable_for_' ~ $reg.name));
    my $code := java_for(JST::Goto.new(:label($skip_label.name)));
    $code := $code ~ java_for($jt.label);
    $code := $code ~ '        switch( ' ~ $reg.name ~ " ) \{\n";
    my $i := 0;
    for @($jt) {
        $code := $code ~ "          case $i : goto " ~ $_.name ~ ";\n";
        $i := $i + 1;
    }
    $code := $code ~ "        }\n" ~ java_for($skip_label);
    return $code;
}

sub lhs_rhs_op(@ops, $op) {
    my $code := java_for(@ops[0]);
    my $lhs := $*LAST_TEMP;
    $code := $code ~ java_for(@ops[1]);
    my $rhs := $*LAST_TEMP;
    $*LAST_TEMP := get_unique_id('expr_result');
    # @ops[2] is the type
    return "$code        " ~ @ops[2] ~ " $*LAST_TEMP = $lhs $op $rhs;\n";
}

our multi sub java_for(JST::Add $ops) {
    lhs_rhs_op(@($ops), '+')
}

our multi sub java_for(JST::Subtract $ops) {
    lhs_rhs_op(@($ops), '-')
}

our multi sub java_for(JST::GT $ops) {
    lhs_rhs_op(@($ops), '>')
}

our multi sub java_for(JST::LT $ops) {
    lhs_rhs_op(@($ops), '<')
}

our multi sub java_for(JST::GE $ops) {
    lhs_rhs_op(@($ops), '>=')
}

our multi sub java_for(JST::LE $ops) {
    lhs_rhs_op(@($ops), '<=')
}

our multi sub java_for(JST::EQ $ops) {
    lhs_rhs_op(@($ops), '==')
}

our multi sub java_for(JST::NE $ops) {
    lhs_rhs_op(@($ops), '!=')
}

our multi sub java_for(JST::OR $ops) {
    lhs_rhs_op(@($ops), '||')
}

our multi sub java_for(JST::AND $ops) {
    lhs_rhs_op(@($ops), '&&')
}

our multi sub java_for(JST::BOR $ops) {
    lhs_rhs_op(@($ops), '|')
}

our multi sub java_for(JST::BAND $ops) {
    lhs_rhs_op(@($ops), '&')
}

our multi sub java_for(JST::BXOR $ops) {
    lhs_rhs_op(@($ops), '^')
}

our multi sub java_for(JST::NOT $ops) {
    my $code := java_for((@($ops))[0]);
    my $lhs := $*LAST_TEMP;
    $*LAST_TEMP := get_unique_id('expr_result_negated');
    return "$code        boolean $*LAST_TEMP = !$lhs;\n";
}

our multi sub java_for(JST::XOR $ops) {
    my $code := java_for((@($ops))[0]);
    my $lhs := $*LAST_TEMP;
    $code := $code ~ java_for((@($ops))[1]);
    my $rhs := $*LAST_TEMP;
    $*LAST_TEMP := get_unique_id('expr_result');
    return "$code        boolean $*LAST_TEMP = $lhs ? ! $rhs : $rhs;\n";
}

our multi sub java_for(JST::Throw $throw) {
    $*LAST_TEMP := 'null';
    return "        throw new UnsupportedOperationException(); // JST::Throw\n";
}

our multi sub java_for(String $s) {
    $*LAST_TEMP := $s;
    return '';
}

our multi sub java_for(JST::ArrayLiteral $al) {
    # Code-gen all the things to go in the array.
    my @item_names;
    my $code := '';
    for @($al) {
        $code := $code ~ java_for($_);
        @item_names.push($*LAST_TEMP);
    }

    # Code-gen the array.
    $*LAST_TEMP := get_unique_global_name('arr','arrayliteral');
    return $code ~ "        " ~ $al.type ~ "[] $*LAST_TEMP = new " ~ $al.type ~ '[] {' ~
        pir::join(',', @item_names) ~ "}; // JST::ArrayLiteral\n";
}

our multi sub java_for(JST::DictionaryLiteral $dl) {
    # Code-gen all the pieces that will go into the dictionary. The
    # list is key,value,key,value.
    my @items;
    my $code := '';
    for @($dl) -> $k, $v {
        $code := $code ~ java_for($k);
        my $key := $*LAST_TEMP;
            $code := $code ~ java_for($v);
        my $value := $*LAST_TEMP;
        @items.push('(' ~ $key ~ ', ' ~ $value ~ ')');
    }

    # Code-gen the dictionary.
    $*LAST_TEMP := get_unique_global_name('dic','dictionaryliteral');
    return $code ~ "        HashMap<" ~ $dl.key_type ~ ', ' ~ $dl.value_type ~ "> $*LAST_TEMP = new HashMap<" ~
        $dl.key_type ~ ', ' ~ $dl.value_type ~ ">(); // JST::DictionaryLiteral\n" ~
        "        $*LAST_TEMP.put" ~
        pir::join(";\n        $*LAST_TEMP.put", @items) ~ ";\n";
}

our multi sub java_for(JST::Temp $tmp) {
    unless +@($tmp) == 1 { pir::die('A JST::Temp must have exactly one child') }
    my $code := java_for((@($tmp))[0]);
    my $name := $tmp.name;
    $code := $code ~ "        " ~ $tmp.type ~ " $name = $*LAST_TEMP; // JST::Temp\n";
    $*LAST_TEMP := $name;
    return $code;
}

our multi sub java_for($any) {
    pir::die("JST to Java compiler doesn't know how to compile a " ~ pir::typeof__SP($any));
}
