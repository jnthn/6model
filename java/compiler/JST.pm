# The Java Syntax Tree set of nodes is designed to represent fundamental
# JVM concepts. This allows most of a PAST compiler for JVM to be
# written and used to generate Java for now, but later we can generate
# bytecode IL.  A tree must have the form:
#
#    JST::CompilationUnit
#        JST::Using
#        ...more usings...
#        JST::Class
#            JST::Method
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

class JST::Node {
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

class JST::CompilationUnit is JST::Node {
    method new(*@children) {
        my $obj := self.CREATE;
        $obj.set_children(@children);
        $obj;
    }
}

class JST::Stmts is JST::Node {
    method new(*@children) {
        my $obj := self.CREATE;
        $obj.set_children(@children);
        $obj;
    }
}

class JST::Using is JST::Node { # though Java's word for using is import
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

class JST::Class is JST::Node {
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

class JST::Attribute is JST::Node {
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

class JST::Method is JST::Node {
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

class JST::Call is JST::Node {
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

class JST::MethodCall is JST::Node {
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
            pir::die('Must supply a type for a JST::MethodCall if it is not void');
        }
        $obj.name($name);
        $obj.void($void);
        $obj.type($type);
        $obj.set_children(@children);
        $obj;
    }
}

class JST::New is JST::Node {
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

class JST::TryFinally is JST::Node {
    method new(*@children) {
        my $obj := self.CREATE;
        $obj.set_children(@children);
        $obj;
    }
}

class JST::TryCatch is JST::Node {
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

class JST::Throw is JST::Node {
    method new() {
        my $obj := self.CREATE;
        $obj;
    }
}

class JST::If is JST::Node {
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

class JST::Return is JST::Node {
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

class JST::Label is JST::Node {
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

class JST::Goto is JST::Node {
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

class JST::Bind is JST::Node {
    method new(*@children) {
        my $obj := self.CREATE;
        $obj.set_children(@children);
        $obj;
    }
}

class JST::Literal is JST::Node {
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

class JST::Null is JST::Node {
    method new() {
        self.CREATE
    }
}

class JST::BinaryOp is JST::Node {
    method new(*@children) {
        my $obj := self.CREATE;
        $obj.set_children(@children);
        $obj;
    }
}

class JST::Add is JST::BinaryOp { }
class JST::Subtract is JST::BinaryOp { }
class JST::GT is JST::BinaryOp { }
class JST::LT is JST::BinaryOp { }
class JST::GE is JST::BinaryOp { }
class JST::LE is JST::BinaryOp { }
class JST::EQ is JST::BinaryOp { }
class JST::NE is JST::BinaryOp { }
class JST::NOT is JST::BinaryOp { } # not really BinaryOp, but whatever
# no such thing as short-circuiting XOR, of course.
class JST::OR is JST::BinaryOp { }
class JST::AND is JST::BinaryOp { }
class JSTr::XOR is JST::BinaryOp { }
class JST::BOR is JST::BinaryOp { }
class JST::BAND is JST::BinaryOp { }
class JST::BXOR is JST::BinaryOp { }

# build/emit only one of these per method (if any)
class JST::JumpTable is JST::Node {
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
    
    # JST::Local - int register in which to store the target
    #   branch's index in the jumptable
    method register($set?) {
        if pir::defined($set) { $!register := $set }
        $!register
    }
    
    # returns JST::Stmts node with code that ends up branching
    #   (indirectly through a jumptable with string gotos computed
    #   at compile-time) to the destination label with that $name
    method jump($name) {
        JST::Stmts.new(
            JST::Bind.new(JST::Literal.new($!register.name, :escape(0)), $name),
            JST::Goto.new(:label($!label.name))
        )
    }
    
    # accepts the name of a label; registers a label with this
    #   jumptable; returns the label.
    method mark($name) {
        my $lbl := JST::Label.new(:name($name));
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
        $obj.label(JST::Label.new(:name(get_unique_id('jump_table'))));
        $obj.register(JST::Local.new(
            :name(get_unique_id('jump_table_int_register')),
            :isdecl(1),
            :type('int'),
            JST::Literal.new( :value('0'), :escape(0))
        ));
        $obj
    }
}

class JST::Local is JST::Node {
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

class JST::ArrayLiteral is JST::Node {
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

class JST::DictionaryLiteral is JST::Node {
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
