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
        $code := $code ~ 'namespace ' ~ $class.namespce ~ " \{\n";
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
    my $code := "        IRakudoObject $try_result;\n" ~
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

our multi sub cs_for(DNST::MethodCall $mc) {
    # Code generate all the arguments.
    my @arg_names;
    my $code := '';
    for @($mc) {
        $code := $code ~ cs_for($_);
        @arg_names.push($*LAST_TEMP);
    }

    # What'we we calling it on?
    my $invocant := $mc.on || @arg_names.shift;

    # Code-gen the call.
    $*LAST_TEMP := get_unique_id('result');
    $code := $code ~
        "        var $*LAST_TEMP = $invocant." ~ $mc.name ~
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
    $*LAST_TEMP := get_unique_id('result');
    $code := $code ~
        "        var $*LAST_TEMP = " ~ $mc.name ~
        "(" ~ pir::join(', ', @arg_names) ~ ");\n";

    return $code;
}

our multi sub cs_for(DNST::Temp $tmp) {
    unless +@($tmp) == 1 { pir::die('A DNST::Temp must have exactly one child') }
    my $code := cs_for((@($tmp))[0]);
    my $name := $tmp.name;
    $code := $code ~ "        var $name = $*LAST_TEMP;\n";
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
    my $result := $lit.escape ??
        # XXX Need to really escape stuff in there.
        ('@"' ~ ~$lit.value ~ '"') !!
        $lit.value;
    $*LAST_TEMP := get_unique_id('lit');
    return "        var $*LAST_TEMP = $result;\n";
}

our multi sub cs_for(String $s) {
    $*LAST_TEMP := $s;
    return '';
}

our multi sub cs_for($any) {
    pir::die("DNST to C# compiler doesn't know how to compile a " ~ pir::typeof__SP($any));
}
