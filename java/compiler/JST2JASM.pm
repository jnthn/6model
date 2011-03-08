# Compile a Java Syntax Tree down to Java Assembly Language
class JST2JASMCompiler;

method compile(JST::Node $node) {
    my $*CUR_ID := 0; my %*CUR_ID; # for get_unique_global_name
    return jasm_for($node);
}

# Quick hack so we can get unique (for this compilation) IDs.
sub get_unique_id($prefix) {
    $*CUR_ID := $*CUR_ID + 1;
    return $prefix ~ '_' ~ $*CUR_ID;
}

# CompilationUnit
our multi sub jasm_for(JST::CompilationUnit $node) {
    my @*USINGS;
    my $main := '';
    for @($node) {
        $main := $main ~ jasm_for($_);
    }
    return $main;
}

our multi sub jasm_for(JST::Using $node) {
    # XXX Doesn't make much sense...
    return '';
}

# Class
our multi sub jasm_for(JST::Class $class) {
    my $code := ".source RakudoOutput.jasm\n";
    # XXX $class.namespace ignored
    $code := $code ~ ".class public " ~ $class.name ~ "\n";
    $code := $code ~ ".super java/lang/Object\n\n";
    for @($class) {
        $code := $code ~ jasm_for($_);
    }

    return $code;
}

# Attribute
our multi sub jasm_for(JST::Attribute $attr) {
    return '';
#    return '    private static ' ~ $attr.type ~ ' ' ~ $attr.name ~ "; // JST:Attribute\n";
}

# Method
our multi sub jasm_for(JST::Method $meth) {
    return '';
    
#    my $*LAST_TEMP := '';

    # Method header.
#    my $code := '    public static ' ~ # C# has private
#        $meth.return_type ~ ' ' ~ 
#        $meth.name ~ '(' ~
#        pir::join(', ', $meth.params) ~
#        ") \{ // JST::Method\n";

    # Emit everything in the method.
#    for @($meth) {
#        $code := $code ~ jasm_for($_);
#    }

    # Return statement if needed, and block ending.
 #   unless $meth.return_type eq 'void' {
 #       $code := $code ~ "        return $*LAST_TEMP;\n";
 #   }
 #   return $code ~ "    }\n\n";
}

# Stmts
our multi sub jasm_for(JST::Stmts $stmts) {
    my $code := '';
    for @($stmts) {
        $code := $code ~ jasm_for($_);
    }
    return $code;
}

# TryFinally
our multi sub jasm_for(JST::TryFinally $tf) {
    return '';
#    unless +@($tf) == 2 { pir::die('JST::TryFinally nodes must have 2 children') }
#    my $try_result := get_unique_global_name('try_result','');
#    my $code := "        RakudoObject $try_result; // JST::TryFinally\n" ~
#                "        try \{\n" ~
#                jasm_for((@($tf))[0]);
#    $code := $code ~
#                "        $try_result = $*LAST_TEMP;\n" ~
#                "        } finally \{\n" ~
#                jasm_for((@($tf))[1]) ~
#                "        }\n";
#    $*LAST_TEMP := $try_result;
#    return $code;
}

# TryCatch
our multi sub jasm_for(JST::TryCatch $tc) {
    unless +@($tc) == 2 { pir::die('JST::TryCatch nodes must have 2 children') }
    return '';
#    
#    my $try_result := get_unique_global_name('trycatch_result','');
#    my $code := "        RakudoObject $try_result;\n" ~
#                "        try \{\n" ~
#                jasm_for((@($tc))[0]);
#    $code := $code ~
#                "        $try_result = $*LAST_TEMP;\n" ~
#                "        } catch(" ~ $tc.exception_type ~ " " ~ $tc.exception_var ~ ")\{\n" ~
#                jasm_for((@($tc))[1]) ~
#                "        $try_result = $*LAST_TEMP;\n" ~
#                "        }\n";
#    $*LAST_TEMP := $try_result;
#    return $code;
}

# MethodCall
our multi sub jasm_for(JST::MethodCall $mc) {
    return '';
    
    # Code generate all the arguments.
#    my @arg_names;
#    my $code := '';
#    for @($mc) {
#        $code := $code ~ jasm_for($_);
#        @arg_names.push($*LAST_TEMP);
#    }

    # What're we calling it on?
#    my $invocant := $mc.on || @arg_names.shift;

    # Code-gen the call.
#    $code := $code ~ '        ';
#    unless $mc.void {
#        my $ret_type := $mc.type || 'var';
#        $*LAST_TEMP := get_unique_global_name('result','methcall');
#        my $method_name := $invocant ~ "." ~ $mc.name;
#        $code := $code ~ "$ret_type $*LAST_TEMP = ";
#    }
#    $code := $code ~ "$invocant." ~ $mc.name ~
#        "(" ~ pir::join(', ', @arg_names) ~ "); // JST::MethodCall\n";
#    return $code;
}

# Call
our multi sub jasm_for(JST::Call $mc) {
    return '';
    
    # Code generate all the arguments.
#    my @arg_names;
#    my $code := '';
#    for @($mc) {
#        $code := $code ~ jasm_for($_);
#        @arg_names.push($*LAST_TEMP);
#    }

#    # Code-gen the call.
#    $code := $code ~ '        ';
#    unless $mc.void {
#        $*LAST_TEMP := get_unique_global_name('result','call');
#        $code := $code ~ "RakudoObject $*LAST_TEMP = ";
#    }
#    $code := $code ~ $mc.name ~
#        "(" ~ pir::join(', ', @arg_names) ~ "); // JST::Call\n";
#
#    return $code;
}

# New
our multi sub jasm_for(JST::New $new) {
    return '';
    # Code generate all the arguments.
    #~ my @arg_names;
    #~ my $code := '';
    #~ for @($new) {
        #~ $code := $code ~ jasm_for($_);
        #~ @arg_names.push($*LAST_TEMP);
    #~ }

    #~ # Code-gen the constructor call.
    #~ $*LAST_TEMP := get_unique_global_name('new','');
    #~ $code := $code ~ "        " ~ $new.type ~ " $*LAST_TEMP = new " ~
        #~ gen_new_type($new, @arg_names);

    #~ return $code;
}

# If
our multi sub jasm_for(JST::If $if) {
    unless +@($if) >= 2 { pir::die('A JST::If node must have at least 2 children') }

    return '';

    #~ # Need a variable to put the final result in.
    #~ my $if_result := get_unique_global_name('if_result','');

    #~ # Get the conditional and emit if.
    #~ my $code := jasm_for((@($if))[0]);
    #~ $code := $code ~
             #~ "        " ~ $if.type ~ " $if_result = null;\n" if $if.result;
    #~ $code := $code ~
             #~ "        if ($*LAST_TEMP" ~ ($if.bool ?? "" !! " != 0") ~ ") \{\n";

    #~ # Compile branch(es).
    #~ $*LAST_TEMP := 'null';
    #~ $code := $code ~ jasm_for((@($if))[1]);
    #~ $code := $code ~ "        $if_result = $*LAST_TEMP;\n" if $if.result;
    #~ $code := $code ~ "        }\n";
    #~ if +@($if) == 3 {
        #~ $*LAST_TEMP := 'null';
        #~ $code := $code ~ "        else \{\n";
        #~ $code := $code ~ jasm_for((@($if))[2]);
        #~ $code := $code ~ "        $if_result = $*LAST_TEMP;\n" if $if.result;
        #~ $code := $code ~ "        }\n";
    #~ }

    #~ $*LAST_TEMP := $if_result if $if.result;
    #~ return $code;
}

# Return
our multi sub jasm_for(DNST::Return $ret) {
    return "    .return\n";
}

# Label # Not available in Java, but retained for eventual JVM bytecode emitter
our multi sub jasm_for(JST::Label $lab) {
    return '';
#    return "      " ~ $lab.name ~ ": ; // JST::Label\n";
}

# Goto # Not available in Java, but retained for eventual JVM bytecode emitter
our multi sub jasm_for(JST::Goto $gt) {
    return '';
#    return "// DO NOT WANT  goto " ~ $gt.label ~ "; // JST::Goto\n";
}

# Bind
our multi sub jasm_for(JST::Bind $bind) {
    unless +@($bind) == 2 { pir::die('JST::Bind nodes must have 2 children') }
    return '';
    
    #~ my $code := jasm_for((@($bind))[0]);
    #~ my $lhs := $*LAST_TEMP;
    #~ $code := $code ~ jasm_for((@($bind))[1]);
    #~ my $rhs := $*LAST_TEMP;
    #~ $code := $code ~ "        $lhs = $rhs; // JST::Bind\n";
    #~ $*LAST_TEMP := $lhs;
    #~ return $code;
}

# Literal
our multi sub jasm_for(JST::Literal $lit) {
    #~ $*LAST_TEMP := $lit.escape ??
        #~ literal_escape($lit.value) !!
        #~ $lit.value;
    return '';
}

# Null
our multi sub jasm_for(JST::Null $null) {
    #~ $*LAST_TEMP := 'null';
    return '';
}

# Local
our multi sub jasm_for(JST::Local $loc) {
    return '';
    
    #~ my $code := '';
    #~ if $loc.isdecl {
        #~ unless +@($loc) == 1 {
            #~ pir::die('A JST::Local with isdecl set must have exactly one child')
        #~ }
        #~ unless $loc.type {
            #~ pir::die('JST::Local with isdecl requires type');
        #~ }
        #~ $code := jasm_for((@($loc))[0]);
        #~ $code := $code ~ '        ' ~ $loc.type ~ ' ' ~ $loc.name ~ " = $*LAST_TEMP;\n";
    #~ } elsif +@($loc) != 0 {
        #~ pir::die('A JST::Local without isdecl set must have no children')
    #~ }
    #~ $*LAST_TEMP := $loc.name;
    #~ return $code;
}

# JumpTable
our multi sub jasm_for(JST::JumpTable $jt) {
    return '';
    #~ my $reg := $jt.register;
    #~ my $skip_label := JST::Label.new(:name('skip_jumptable_for_' ~ $reg.name));
    #~ my $code := jasm_for(JST::Goto.new(:label($skip_label.name)));
    #~ $code := $code ~ jasm_for($jt.label);
    #~ $code := $code ~ '        switch( ' ~ $reg.name ~ " ) \{\n";
    #~ my $i := 0;
    #~ for @($jt) {
        #~ $code := $code ~ "          case $i : goto " ~ $_.name ~ ";\n";
        #~ $i := $i + 1;
    #~ }
    #~ $code := $code ~ "        }\n" ~ jasm_for($skip_label);
    #~ return $code;
}

sub lhs_rhs_op(@ops, $op) {
    return '';
    #~ my $code := jasm_for(@ops[0]);
    #~ my $lhs := $*LAST_TEMP;
    #~ $code := $code ~ jasm_for(@ops[1]);
    #~ my $rhs := $*LAST_TEMP;
    #~ $*LAST_TEMP := get_unique_id('expr_result');
    #~ # @ops[2] is the type
    #~ return "$code        " ~ @ops[2] ~ " $*LAST_TEMP = $lhs $op $rhs;\n";
}
# Add
our multi sub jasm_for(JST::Add $ops) {
    lhs_rhs_op(@($ops), '+')
}
# Subtract
our multi sub jasm_for(JST::Subtract $ops) {
    lhs_rhs_op(@($ops), '-')
}
# GT
our multi sub jasm_for(JST::GT $ops) {
    lhs_rhs_op(@($ops), '>')
}
# LT
our multi sub jasm_for(JST::LT $ops) {
    lhs_rhs_op(@($ops), '<')
}
# GE
our multi sub jasm_for(JST::GE $ops) {
    lhs_rhs_op(@($ops), '>=')
}
# LE
our multi sub jasm_for(JST::LE $ops) {
    lhs_rhs_op(@($ops), '<=')
}
# EQ
our multi sub jasm_for(JST::EQ $ops) {
    lhs_rhs_op(@($ops), '==')
}
# NE
our multi sub jasm_for(JST::NE $ops) {
    lhs_rhs_op(@($ops), '!=')
}
# OR
our multi sub jasm_for(JST::OR $ops) {
    lhs_rhs_op(@($ops), '||')
}
# AND
our multi sub jasm_for(JST::AND $ops) {
    lhs_rhs_op(@($ops), '&&')
}
# BOR
our multi sub jasm_for(JST::BOR $ops) {
    lhs_rhs_op(@($ops), '|')
}
# BAND
our multi sub jasm_for(JST::BAND $ops) {
    lhs_rhs_op(@($ops), '&')
}
# BXOR
our multi sub jasm_for(JST::BXOR $ops) {
    lhs_rhs_op(@($ops), '^')
}
# NOT
our multi sub jasm_for(JST::NOT $ops) {
    return '';
    #~ my $code := jasm_for((@($ops))[0]);
    #~ my $lhs := $*LAST_TEMP;
    #~ $*LAST_TEMP := get_unique_id('expr_result_negated');
    #~ return "$code        boolean $*LAST_TEMP = !$lhs;\n";
}

# XOR
our multi sub jasm_for(JST::XOR $ops) {
    return '';
    #~ my $code := jasm_for((@($ops))[0]);
    #~ my $lhs := $*LAST_TEMP;
    #~ $code := $code ~ jasm_for((@($ops))[1]);
    #~ my $rhs := $*LAST_TEMP;
    #~ $*LAST_TEMP := get_unique_id('expr_result');
    #~ return "$code        boolean $*LAST_TEMP = $lhs ? ! $rhs : $rhs;\n";
}

# Throw
our multi sub jasm_for(JST::Throw $throw) {
    return '';
    #~ $*LAST_TEMP := 'null';
    #~ return "if (1==1) throw new UnsupportedOperationException(); // JST::Throw\n"; # TODO: check this if (1==1) ???
}

# String
our multi sub jasm_for(String $s) {
    #~ $*LAST_TEMP := $s;
    return '';
}

# ArrayLiteral
our multi sub jasm_for(JST::ArrayLiteral $al) {
    return '';
    #~ # Code-gen all the things to go in the array.
    #~ my @item_names;
    #~ my $code := '';
    #~ for @($al) {
        #~ $code := $code ~ jasm_for($_);
        #~ @item_names.push($*LAST_TEMP);
    #~ }

    #~ # Code-gen the array.
    #~ $*LAST_TEMP := 'new ' ~ $al.type ~ '[] {' ~ pir::join(',', @item_names) ~ '}';
    #~ return $code;
}

# DictionaryLiteral
our multi sub jasm_for(JST::DictionaryLiteral $dl) {
    return '';
    
    #~ # Code-gen all the pieces that will go into the dictionary. The
    #~ # list is key,value,key,value.
    #~ my @items;
    #~ my $code := '';
    #~ for @($dl) -> $k, $v {
        #~ $code := $code ~ jasm_for($k);
        #~ my $key := $*LAST_TEMP;
        #~ $code := $code ~ jasm_for($v);
        #~ my $value := $*LAST_TEMP;
        #~ @items.push('(' ~ $key ~ ', ' ~ $value ~ ')');
    #~ }

    #~ # Code-gen the dictionary.
    #~ $*LAST_TEMP := get_unique_global_name('dic','dictionaryliteral');
    #~ return $code ~ "        HashMap<" ~ $dl.key_type ~ ', ' ~ $dl.value_type ~ "> $*LAST_TEMP = new HashMap<" ~
        #~ $dl.key_type ~ ', ' ~ $dl.value_type ~ ">(); // JST::DictionaryLiteral\n" ~
        #~ "        $*LAST_TEMP.put" ~
        #~ pir::join(";\n        $*LAST_TEMP.put", @items) ~ ";\n";
#~ # TODO:
#~ #   $*LAST_TEMP := "new HashMap<" ~ $dl.key_type ~ ', ' ~ $dl.value_type ~ '>() { ' ~
#~ #       pir::join(',', @items) ~ ' }';
#~ #   return $code;
}

our multi sub jasm_for($any) {
    pir::die("JST to Java compiler doesn't know how to compile a " ~ pir::typeof__SP($any));
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

# Generate a type for JST::New
sub gen_new_type($new, @arg_names) {
    my $code := $new.type;
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

# Convert a literal string to Java source code with escape characters
sub literal_escape($str_in) {
    # the C# version is much simpler because @"" strings can contain
    # control characters </excuses> ;)
    my $str_out := '';
    # TODO: extend this code to handle more control characters
    # TODO: optimize this algorithm. nibbling characters off the front
    #       end of the string cannot be the best way, because this
    #       causes many heap allocations.
    while pir::length($str_in) {
        my $char := pir::substr($str_in, 0, 1);
        $str_in := pir::substr($str_in, 1);
        if $char eq "\"" { $char := "\\\""; }
        if $char eq "\n" { $char := "\\n"; }
        if $char eq "\t" { $char := "\\t"; }
        $str_out := $str_out ~ $char;
    }
    return '"' ~ $str_out ~ '"';
}
