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

    method new(:$name!, :$on, :$void, *@children) {
        my $obj := self.CREATE;
        if $on { $obj.on($on); }
        $obj.name($name);
        $obj.void($void);
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

class DNST::If is DNST::Node {
    method new(*@children) {
        my $obj := self.CREATE;
        $obj.set_children(@children);
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

class DNST::Temp is DNST::Node {
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

    method value($set?) {
        if pir::defined($set) { $!value := $set }
        $!value
    }

    method escape($set?) {
        if pir::defined($set) { $!escape := $set }
        $!escape
    }

    method new(:$value!, :$escape) {
        my $obj := self.CREATE;
        $obj.value($value);
        $obj.escape($escape);
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
