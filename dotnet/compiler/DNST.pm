# The Dotnet Syntax Tree set of nodes is designed to represent fundemental
# .Net concepts. This allows most of a PAST compiler for .Net to be
# written and used to generate C# for now, but later we can generate IL.
# A tree must have the form:
# 
#    DNST::CompilationUnit
#        DNST::Using
#        ...more usings...
#        DNST::Class
#            DNST::Method
#                Binding and method call nodes
#            ...more methods...
#        ...more classes...
# 
# That is, we must have a compilation unit at the top level, which may
# contain Using or Class nodes. The Class nodes may only contain Method
# nodes.

sub get_unique_id($prefix) {
    $*CUR_ID := $*CUR_ID + 1;
    return $prefix ~ '_' ~ $*CUR_ID;
}

class DNST::Node {
    has @!children;
    method set_children(@children) {
        @!children := @children;
    }
    method push($obj) {
        @!children.push($obj);
    }
    method pop() {
        @!children.pop;
    }
    method unshift($obj) {
        @!children.unshift($obj);
    }
    method shift() {
        @!children.shift;
    }
    method list() {
        @!children
    }
    method CREATE() {
        pir::new__pp(self.HOW.get_parrotclass(self))
    }
}

class DNST::CompilationUnit is DNST::Node {
    method new(*@children) {
        my $obj := self.CREATE;
        $obj.set_children(@children);
        $obj;
    }
}

class DNST::Stmts is DNST::Node {
    method new(*@children) {
        my $obj := self.CREATE;
        $obj.set_children(@children);
        $obj;
    }
}

class DNST::Using is DNST::Node {
    has $!namespace;

    method namespace($set?) {
        if $set { $!namespace := $set }
        $!namespace
    }

    method new(:$namespace!) {
        my $obj := self.CREATE;
        $obj.namespace($namespace);
        $obj
    }
}

class DNST::Class is DNST::Node {
    has $!namespace;
    has $!name;

    method namespace($set?) {
        if $set { $!namespace := $set }
        $!namespace
    }

    method name($set?) {
        if $set { $!name := $set }
        $!name
    }

    method new(:$name!, :$namespace, *@children) {
        my $obj := self.CREATE;
        $obj.name($name);
        if $namespace { $obj.namespace($namespace); }
        $obj.set_children(@children);
        $obj;
    }
}

class DNST::Attribute is DNST::Node {
    has $!name;
    has $!type;

    method name($set?) {
        if $set { $!name := $set }
        $!name
    }

    method type($set?) {
        if $set { $!type := $set }
        $!type
    }

    method new(:$name!, :$type!) {
        my $obj := self.CREATE;
        $obj.name($name);
        $obj.type($type);
        $obj;
    }
}

class DNST::Method is DNST::Node {
    has $!name;
    has $!return_type;
    has @!params;

    method name($set?) {
        if $set { $!name := $set }
        $!name
    }

    method return_type($set?) {
        if $set { $!return_type := $set }
        $!return_type
    }

    method params(@set?) {
        if @set { @!params := @set }
        @!params
    }

    method new(:$name!, :$return_type!, :@params, *@children) {
        my $obj := self.CREATE;
        $obj.name($name);
        $obj.return_type($return_type);
        $obj.params(@params);
        $obj.set_children(@children);
        $obj;
    }
}

class DNST::Call is DNST::Node {
    has $!name;
    has $!void;

    method name($set?) {
        if $set { $!name := $set }
        $!name
    }

    method void($set?) {
        if $set { $!void := $set }
        $!void
    }

    method new(:$name!, :$void, *@children) {
        my $obj := self.CREATE;
        $obj.name($name);
        $obj.void($void);
        $obj.set_children(@children);
        $obj;
    }
}

class DNST::MethodCall is DNST::Node {
    has $!on;
    has $!name;
    has $!void;
    has $!type;
    
    method on($set?) {
        if $set { $!on := $set }
        $!on
    }

    method name($set?) {
        if $set { $!name := $set }
        $!name
    }

    method void($set?) {
        if $set { $!void := $set }
        $!void
    }

    method type($set?) {
        if $set { $!type := $set }
        $!type
    }

    method new(:$name!, :$on, :$void, :$type, *@children) {
        my $obj := self.CREATE;
        if $on { $obj.on($on); }
        if !$void && !$type {
            pir::die('Must supply a type for a DNST::MethodCall if it is not void');
        }
        $obj.name($name);
        $obj.void($void);
        $obj.type($type);
        $obj.set_children(@children);
        $obj;
    }
}

class DNST::New is DNST::Node {
    has $!type;

    method type($set?) {
        if $set { $!type := $set }
        $!type
    }

    method new(:$type!, *@children) {
        my $obj := self.CREATE;
        $obj.type($type);
        $obj.set_children(@children);
        $obj;
    }
}

class DNST::TryFinally is DNST::Node {
    method new(*@children) {
        my $obj := self.CREATE;
        $obj.set_children(@children);
        $obj;
    }
}

class DNST::TryCatch is DNST::Node {
    has $!exception_type;
    has $!exception_var;

    method exception_type($set?) {
        if $set { $!exception_type := $set }
        $!exception_type
    }

    method exception_var($set?) {
        if $set { $!exception_var := $set }
        $!exception_var
    }

    method new(*@children, :$exception_type!, :$exception_var!) {
        my $obj := self.CREATE;
        $obj.exception_type($exception_type);
        $obj.exception_var($exception_var);
        $obj.set_children(@children);
        $obj;
    }
}

class DNST::Throw is DNST::Node {
    method new() {
        my $obj := self.CREATE;
        $obj;
    }
}

class DNST::If is DNST::Node {
    has $!type;
    has $!bool;
    has $!result;

    method bool($set?) {
        if $set { $!bool := $set }
        $!bool
    }

    method type($set?) {
        if $set { $!type := $set }
        $!type
    }

    method result($set?) {
        if $set { $!result := $set }
        $!result
    }

    method new(:$bool, :$type, :$result, *@children) {
        my $obj := self.CREATE;
        $obj.set_children(@children);
        $obj.bool($bool);
        $obj.type(pir::defined($type) ?? $type !! 'RakudoObject');
        $obj.result(pir::defined($result) ?? $result !! 1);
        $obj;
    }
}

class DNST::Return is DNST::Node {
    has $!target;

    method target($set?) {
        if pir::defined($set) { $!target := $set }
        $!target
    }
    
    method new($target) {
        my $obj := self.CREATE;
        $obj.target($target);
        $obj;
    }
}

class DNST::Label is DNST::Node {
    has $!name;

    method name($set?) {
        if $set { $!name := $set }
        $!name
    }

    method new(:$name!) {
        my $obj := self.CREATE;
        $obj.name($name);
        $obj;
    }
}

class DNST::Goto is DNST::Node {
    has $!label;

    method label($set?) {
        if $set { $!label := $set }
        $!label
    }

    method new(:$label!) {
        my $obj := self.CREATE;
        $obj.label($label);
        $obj;
    }
}

class DNST::Bind is DNST::Node {
    method new(*@children) {
        my $obj := self.CREATE;
        $obj.set_children(@children);
        $obj;
    }
}

class DNST::Literal is DNST::Node {
    has $!value;
    has $!escape;
    has $!type;

    method value($set?) {
        if pir::defined($set) { $!value := $set }
        $!value
    }

    method escape($set?) {
        if pir::defined($set) { $!escape := $set }
        $!escape
    }

    method type($set?) {
        if pir::defined($set) { $!type := $set }
        $!type
    }

    method new(:$value!, :$escape, :$type) {
        my $obj := self.CREATE;
        $obj.value($value);
        $obj.escape($escape);
        $obj.type($type);
        $obj;
    }
}

class DNST::Null is DNST::Node {
    method new() {
        self.CREATE
    }
}

class DNST::BinaryOp is DNST::Node {
    method new(*@children) {
        my $obj := self.CREATE;
        $obj.set_children(@children);
        $obj;
    }
}

class DNST::Add is DNST::BinaryOp { }
class DNST::Subtract is DNST::BinaryOp { }
class DNST::GT is DNST::BinaryOp { }
class DNST::LT is DNST::BinaryOp { }
class DNST::GE is DNST::BinaryOp { }
class DNST::LE is DNST::BinaryOp { }
class DNST::EQ is DNST::BinaryOp { }
class DNST::NE is DNST::BinaryOp { }
class DNST::NOT is DNST::BinaryOp { } # not really BinaryOp, but whatever
# no such thing as short-circuiting XOR, of course.
class DNST::OR is DNST::BinaryOp { }
class DNST::AND is DNST::BinaryOp { }
class DNST::XOR is DNST::BinaryOp { }
class DNST::BOR is DNST::BinaryOp { }
class DNST::BAND is DNST::BinaryOp { }
class DNST::BXOR is DNST::BinaryOp { }

# build/emit only one of these per method (if any)
class DNST::JumpTable is DNST::Node {
    has %!names;
    has $!label;
    has $!register;

    method names(%set?) {
        if pir::defined(%set) { %!names := %set }
        %!names
    }
    
    # (prologue) label just before the jumptable
    method label($set?) {
        if pir::defined($set) { $!label := $set }
        $!label
    }
    
    # DNST::Local - int register in which to store the target
    #   branch's index in the jumptable
    method register($set?) {
        if pir::defined($set) { $!register := $set }
        $!register
    }
    
    # returns DNST::Stmts node with code that ends up branching
    #   (indirectly through a jumptable with string gotos computed
    #   at compile-time) to the destination label with that $name
    method jump($name) {
        DNST::Stmts.new(
            DNST::Bind.new(DNST::Literal.new($!register.name, :escape(0)), $name),
            DNST::Goto.new(:label($!label.name))
        )
    }
    
    # accepts the name of a label; registers a label with this
    #   jumptable; returns the label.
    method mark($name) {
        my $lbl := DNST::Label.new(:name($name));
        %!names{$name} := ~+@!children;
        #pir::say("marked $name as " ~ %!names{$name});
        @!children.push($lbl);
        $lbl
    }
    
    method get_index($name) {
        my $i := 0;
        for @!children {
            return $i if $_.name eq $name;
            $i := $i + 1
        }
        -1
    }
    
    method new(*@children) {
        my $obj := self.CREATE;
        $obj.set_children(@children);
        $obj.label(DNST::Label.new(:name(get_unique_id('jump_table'))));
        $obj.register(DNST::Local.new(
            :name(get_unique_id('jump_table_int_register')),
            :isdecl(1),
            :type('int'),
            DNST::Literal.new( :value('0'), :escape(0))
        ));
        $obj
    }
}

class DNST::Local is DNST::Node {
    has $!name;
    has $!type;
    has $!isdecl;

    method name($set?) {
        if pir::defined($set) { $!name := $set }
        $!name
    }

    method type($set?) {
        if $set { $!type := $set }
        $!type
    }

    method isdecl($set?) {
        if $set { $!isdecl := $set }
        $!isdecl
    }

    method new(:$name!, :$type, :$isdecl, *@children) {
        my $obj := self.CREATE;
        $obj.name($name);
        $obj.type($type);
        $obj.isdecl($isdecl);
        $obj.set_children(@children);
        $obj;
    }
}

class DNST::ArrayLiteral is DNST::Node {
    has $!type;

    method type($set?) {
        if $set { $!type := $set }
        $!type
    }

    method new(:$type!, *@children) {
        my $obj := self.CREATE;
        $obj.type($type);
        $obj.set_children(@children);
        $obj;
    }
}

class DNST::DictionaryLiteral is DNST::Node {
    has $!key_type;
    has $!value_type;

    method key_type($set?) {
        if $set { $!key_type := $set }
        $!key_type
    }

    method value_type($set?) {
        if $set { $!value_type := $set }
        $!value_type
    }

    method new(:$key_type!, :$value_type!, *@children) {
        my $obj := self.CREATE;
        $obj.key_type($key_type);
        $obj.value_type($value_type);
        $obj.set_children(@children);
        $obj;
    }
}
