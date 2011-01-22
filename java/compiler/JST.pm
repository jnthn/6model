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
    method new(*@children) {
        my $obj := self.CREATE;
        $obj.set_children(@children);
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

class JST::Temp is JST::Node {
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

    method new(:$name!, :$type!, *@children) {
        my $obj := self.CREATE;
        $obj.name($name);
        $obj.type($type);
        $obj.set_children(@children);
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
