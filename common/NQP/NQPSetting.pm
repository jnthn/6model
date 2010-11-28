my knowhow NQPStr is repr('P6str') {
    method new() {
        nqp::instance_of(self.WHAT);
    }
    method Bool() {
        nqp::logical_not_int(nqp::equal_strs(self, "")) &&
            nqp::logical_not_int(nqp::equal_strs(self, "0"))
    }
    method Int() {
        nqp::coerce_str_to_int(self, NQPInt)
    }
    method Num() {
        nqp::coerce_str_to_num(self, NQPNum)
    }
    method Numeric() {
        nqp::coerce_str_to_num(self, NQPNum)
    }
    method Str() {
        self
    }
    method Stringy() {
        self
    }
    method defined() {
        nqp::repr_defined(self)
    }
    method ACCEPTS($target) {
        my $what := self;
        $what.isa($target, $what);
    }
}

my knowhow NQPInt is repr('P6int') {
    method new() {
        nqp::instance_of(self.WHAT);
    }
    method Bool() {
        nqp::logical_not_int(nqp::equal_ints(self, 0))
    }
    method Int() {
        self
    }
    method Num() {
        nqp::coerce_int_to_num(self, NQPNum)
    }
    method Numeric() {
        self
    }
    method Str() {
        nqp::coerce_int_to_str(self, NQPStr)
    }
    method Stringy() {
        nqp::coerce_int_to_str(self, NQPStr)
    }
    method defined() {
        nqp::repr_defined(self)
    }
}

my knowhow NQPNum is repr('P6num') {
    method new() {
        nqp::instance_of(self.WHAT);
    }
    method Bool() {
        nqp::logical_not_int(nqp::equal_nums(self, 0.0))
    }
    method Int() {
        nqp::coerce_num_to_int(self, NQPStr)
    }
    method Num() {
        self
    }
    method Numeric() {
        self
    }
    method Str() {
        nqp::coerce_num_to_str(self, NQPStr)
    }
    method Stringy() {
        nqp::coerce_num_to_str(self, NQPStr)
    }
    method defined() {
        nqp::repr_defined(self)
    }
}

# XXX Bad hack, we'll replace this later.
my knowhow Any {
    method defined() { 0 }
    method Num() { nqp::coerce_int_to_num(0) }
    method Int() { 0 }
}

my knowhow NQPMapIter {
    has $!block;
    has @!list;
    method new($block, @list) {
        my $iter := nqp::instance_of(self.WHAT);
        $iter.BUILD($block, @list);
        $iter
    }
    method BUILD($block, @list) {
        $!block := $block;
        @!list := @list;
    }
    method eager() {
        my $i := 0;
        my $elems := +@!list;
        my @result;
        while $i < $elems {
            @result.push($!block(@!list[$i]));
            $i := $i + 1;
        }
        @result
    }
}

my knowhow NQPList is repr('P6list') {
    method new() {
        nqp::instance_of(self.WHAT)
    }
    method elems() {
        nqp::lllist_elems(self)
    }
    method Numeric() {
        self.elems
    }
    method at_pos($idx) {
        nqp::vivify(nqp::lllist_get_at_pos(self, $idx.Int), Any)
    }
    method defined() {
        nqp::repr_defined(self)
    }
    method map($block) {
        NQPMapIter.new($block, self)
    }
}

my knowhow NQPArray is repr('P6list') {
    method new() {
        nqp::instance_of(self.WHAT)
    }
    method Numeric() {
        self.elems
    }
    method elems() {
        nqp::lllist_elems(self)
    }
    method pop() {
        nqp::lllist_pop(self)
    }
    method push($item) {
        nqp::lllist_push(self, $item)
    }
    method shift() {
        nqp::lllist_shift(self)
    }
    method unshift($item) {
        nqp::lllist_unshift(self, $item)
    }
    method at_pos($idx) {
        nqp::vivify(nqp::lllist_get_at_pos(self, $idx.Int), Any)
    }
    method bind_pos($idx, $value) {
        nqp::lllist_bind_at_pos(self, $idx.Int, $value)
    }
    method defined() {
        nqp::repr_defined(self)
    }
    method map($block) {
        NQPMapIter.new($block, self)
    }
}

my knowhow NQPHash is repr('P6mapping') {
    method new() {
        nqp::instance_of(self.WHAT)
    }
    method Numeric() {
        self.elems
    }
    method elems() {
        nqp::llmapping_elems(self)
    }
    method at_key($key) {
        nqp::vivify(nqp::llmapping_get_at_key(self, $key.Str), Any)
    }
    method bind_key($key, $value) {
        nqp::llmapping_bind_at_key(self, $key.Str, $value)
    }
    method defined() {
        nqp::repr_defined(self)
    }
}

my knowhow NQPCode is repr('RakudoCodeRef') {
    method leave($with) {
        nqp::leave_block(self, $with)
    }
    method defined() {
        nqp::repr_defined(self)
    }
}

my knowhow NQPCapture is repr('P6capture') {
    method new() {
        nqp::instance_of(self.WHAT)
    }
    method pos_at($pos) {
        nqp::llcap_get_at_pos(self, $pos)
    }
    method key_at($key) {
        nqp::llcap_get_at_key(self, $key)
    }
    method bind_pos($pos, $val) {
        nqp::llcap_bind_at_pos(self, $pos, $val)
    }
    method bind_key($key, $val) {
        nqp::llcap_bind_at_key(self, $key, $val)
    }
}

proto sub &infix:<==>($x, $y) { * }
multi sub &infix:<==>($x, $y) {
    nqp::equal_nums($x.Num, $y.Num)
}
multi sub &infix:<==>(NQPInt $x, NQPInt $y) {
    nqp::equal_ints($x, $y)
}
multi sub &infix:<==>(NQPNum $x, NQPNum $y) {
    nqp::equal_nums($x, $y)
}

proto sub &infix:<!=>($x, $y) { * }
multi sub &infix:<!=>($x, $y) {
    nqp::logical_not_int(nqp::equal_nums($x.Num, $y.Num))
}
multi sub &infix:<!=>(NQPInt $x, NQPInt $y) {
    nqp::logical_not_int(nqp::equal_ints($x, $y))
}
multi sub &infix:<!=>(NQPNum $x, NQPNum $y) {
    nqp::logical_not_int(nqp::equal_nums($x, $y))
}

proto sub &infix:«<=»($x, $y) { * }
multi sub &infix:«<=»($x, $y) {
    nqp::less_than_or_equal_nums($x.Num, $y.Num)
}
multi sub &infix:«<=»(NQPInt $x, NQPInt $y) {
    nqp::less_than_or_equal_ints($x, $y)
}
multi sub &infix:«<=»(NQPNum $x, NQPNum $y) {
    nqp::less_than_or_equal_nums($x, $y)
}

proto sub &infix:«<»($x, $y) { * }
multi sub &infix:«<»($x, $y) {
    nqp::less_than_nums($x.Num, $y.Num)
}
multi sub &infix:«<»(NQPInt $x, NQPInt $y) {
    nqp::less_than_ints($x, $y)
}
multi sub &infix:«<»(NQPNum $x, NQPNum $y) {
    nqp::less_than_nums($x, $y)
}

proto sub &infix:«>=»($x, $y) { * }
multi sub &infix:«>=»($x, $y) {
    nqp::greater_than_or_equal_nums($x.Num, $y.Num)
}
multi sub &infix:«>=»(NQPInt $x, NQPInt $y) {
    nqp::greater_than_or_equal_ints($x, $y)
}
multi sub &infix:«>=»(NQPNum $x, NQPNum $y) {
    nqp::greater_than_or_equal_nums($x, $y)
}

proto sub &infix:«>»($x, $y) { * }
multi sub &infix:«>»($x, $y) {
    nqp::greater_than_nums($x.Num, $y.Num)
}
multi sub &infix:«>»(NQPInt $x, NQPInt $y) {
    nqp::greater_than_ints($x, $y)
}
multi sub &infix:«>»(NQPNum $x, NQPNum $y) {
    nqp::greater_than_nums($x, $y)
}

sub &infix:<eq>($x, $y) {
    nqp::equal_strs($x.Str, $y.Str)
}

sub &infix:<ne>($x, $y) {
    !nqp::equal_strs($x.Str, $y.Str)
}

sub &infix:<ge>($x, $y) {
    nqp::greater_than_or_equal_strs($x.Str, $y.Str)
}

sub &infix:<gt>($x, $y) {
    nqp::greater_than_strs($x.Str, $y.Str)
}

sub &infix:<le>($x, $y) {
    nqp::less_than_or_equal_strs($x.Str, $y.Str)
}

sub &infix:<lt>($x, $y) {
    nqp::less_than_strs($x.Str, $y.Str)
}

sub &infix:<=:=>($x, $y) {
    nqp::equal_refs($x, $y)
}

sub &prefix:<!>($x) {
    nqp::logical_not_int($x.Bool)
}

sub &prefix:<?>($x) {
    $x.Bool
}

sub &prefix:<~>($x) {
    $x.Stringy
}

sub &prefix:<+>($x) {
    $x.Numeric
}

proto sub &infix:<+>($x, $y) { * }
multi sub &infix:<+>($x, $y) {
    nqp::add_num($x.Num, $y.Num);
}
multi sub &infix:<+>(NQPInt $x, NQPInt $y) {
    nqp::add_int($x, $y);
}
multi sub &infix:<+>(NQPNum $x, NQPNum $y) {
    nqp::add_num($x, $y);
}

proto sub &infix:<->($x, $y) { * }
multi sub &infix:<->($x, $y) {
    nqp::sub_num($x.Num, $y.Num);
}
multi sub &infix:<->(NQPInt $x, NQPInt $y) {
    nqp::sub_int($x, $y);
}
multi sub &infix:<->(NQPNum $x, NQPNum $y) {
    nqp::sub_num($x, $y);
}

proto sub &infix:<*>($x, $y) { * }
multi sub &infix:<*>($x, $y) {
    nqp::mul_num($x.Num, $y.Num);
}
multi sub &infix:<*>(NQPInt $x, NQPInt $y) {
    nqp::mul_int($x, $y);
}
multi sub &infix:<*>(NQPNum $x, NQPNum $y) {
    nqp::mul_num($x, $y);
}

proto sub &infix:</>($x, $y) { * }
multi sub &infix:</>($x, $y) {
    nqp::div_num($x.Num, $y.Num);
}
multi sub &infix:</>(NQPInt $x, NQPInt $y) {
    nqp::div_int($x, $y);
}
multi sub &infix:</>(NQPNum $x, NQPNum $y) {
    nqp::div_num($x, $y);
}

proto sub &infix:<%>($x, $y) { * }
multi sub &infix:<%>($x, $y) {
    nqp::mod_int($x.Int, $y.Int);
}
multi sub &infix:<%>(NQPInt $x, NQPInt $y) {
    nqp::mod_int($x, $y);
}

sub &infix:<~>($x, $y) {
    nqp::concat($x.Str, $y.Str);
}

sub &infix:<+|>(NQPInt $x, NQPInt $y) {
    nqp::bitwise_or_int($x, $y);
}
sub &infix:<+&>(NQPInt $x, NQPInt $y) {
    nqp::bitwise_and_int($x, $y);
}
sub &infix:<+^>(NQPInt $x, NQPInt $y) {
    nqp::bitwise_xor_int($x, $y);
}

# A basic, fairly bare-bones exception object.
my knowhow NQPException {
    has $!message;
    has $!resumable;

    method new($message) {
        nqp::instance_of(self.WHAT).BUILD(:message($message))
    }

    method BUILD(:$message) {
        $!message := $message;
        $!resumable := 0;
        self;
    }

    method defined() {
        nqp::repr_defined(self)
    }

    method resumable() {
        $!resumable
    }

    method resume() {
        $!resumable := 1;
    }

    method Str() {
        $!message
    }
}

sub die($message) {
    nqp::throw_dynamic(NQPException.new($message), 0)
}

sub substr(NQPStr $str, NQPInt $offset, NQPInt $length?) {
    nqp::repr_defined($length)
        ?? nqp::substr($str, $offset, $length)
        !! nqp::substr($str, $offset)
}

# For tests.
my $count := NQPInt.new();
sub plan($n) {
    print("1..");
    say($n);
}
sub ok($check, $diag?) {
    $count := $count + 1;
    unless $check { print("not ") }
    print("ok ");
    say($count);
}

# Here comes the start of a heavily under construction ClassHOW.
my knowhow NQPClassHOW {
    ##
    ## Attributes
    ##

    # Name of the class.
    has $!name;

    # Attributes, methods, parents and roles directly added.
    has @!attributes;
    has %!methods;
    has @!multi_methods_to_incorporate;
    has @!parents;
    has @!roles;

    # Vtable and mapping of method names to slots.
    has @!vtable;
    has %!method-vtable-slots;

    # Have we been composed?
    has $!composed;

    # Cached MRO (list of the type objects).
    has @!mro;

    # Full list of roles that we do.
    has @!done;

    ##
    ## Declarative.
    ##

    # Creates a new instance of this meta-class.
    method new(:$name) {
        nqp::instance_of(self).BUILD(:name($name))
    }

    method CREATE($obj) {
        nqp::instance_of($obj)
    }

    method BUILD(:$name) {
        $!name := $name;
        $!composed := 0;
        %!methods := NQPHash.new;
        @!multi_methods_to_incorporate := NQPArray.new;
        @!attributes := NQPArray.new;
        @!parents := NQPArray.new;
        self;
    }

    # Create a new meta-class instance, and then a new type object
    # to go with it, and return that.
    # XXX TODO :$repr named parameter defaulting to P6opaque (don't
    # have default values yet implemented).
    method new_type(:$name = '<anon>', :$repr = 'P6opaque') {
        my $metaclass := self.new(:name($name));
        nqp::type_object_for($metaclass, 'P6opaque');
    }

    method add_method($obj, $name, $code_obj) {
        if %!methods{$name}.defined {
            die("This class already has a method named " ~ $name);
        }
        %!methods{$name} := $code_obj;
    }

    method add_multi_method($obj, $name, $code_obj) {
        # We can't incorporate these right away as we don't know all
        # parents yet, maybe, which influences whether we even can
        # have multis, need to generate a proto or worreva. So just
        # queue them up in a todo list and we handle it at class
        # composition time.
        my %todo;
        %todo<name> := $name;
        %todo<code> := $code_obj;
        @!multi_methods_to_incorporate[+@!multi_methods_to_incorporate] := %todo;
        $code_obj;
    }

    method add_attribute($obj, $meta_attr) {
        if $!composed {
            die("NQPClassHOW does not support adding attributes after being composed.");
        }
        my $i := 0;
        while $i != +@!attributes {
            if @!attributes[$i].name eq $meta_attr.name {
                die("Already have an attribute named " ~ $meta_attr.name);
            }
            $i := $i + 1;
        }
        @!attributes[+@!attributes] := $meta_attr;
    }

    method add_parent($obj, $parent) {
        if $!composed {
            die("NQPClassHOW does not support adding parents after being composed.");
        }
        if $obj =:= $parent {
            die("Cannot make a class its own parent.");
        }
        my $i := 0;
        while $i != +@!parents {
            if @!parents[$i] =:= $parent {
                die("Already have " ~ $parent ~ " as a parent class.");
            }
            $i := $i + 1;
        }
        @!parents[+@!parents] := $parent;
    }

    method compose($obj) {
        # XXX TODO: Compose roles (must come before we make MRO,
        # and may provide multi candidates.)

        # If we have no parents and we're not called Mu (XXX that's a
        # tad fragile, I guess...) then add Mu as our parent.
        if +@!parents == 0 && $!name ne 'Mu' {
            self.add_parent($obj, Mu)
        }
        
        # Some things we only do if we weren't already composed once, like
        # building the MRO.
        unless $!composed {
            @!mro := compute_c3_mro($obj);
            $!composed := 1;
        }

        # Incorporate any new multi candidates (needs MRO built).
        self.incorporate_multi_candidates($obj);

        # Publish type cache.
        self.publish_type_cache($obj);
        
        # Compose attributes.
        for @!attributes { $_.compose($obj) }

        $obj
    }

    method incorporate_multi_candidates($obj) {
        my $num_todo := +@!multi_methods_to_incorporate;
        my $i := 0;
        while $i != $num_todo {
            # Get method name and code.
            my $name := @!multi_methods_to_incorporate[$i]<name>;
            my $code := @!multi_methods_to_incorporate[$i]<code>;

            # Do we have anything in the methods table already in
            # this class?
            my $dispatcher := %!methods{$name};
            if $dispatcher.defined {
                # Yes. Only or dispatcher, though? If only, error. If
                # dispatcher, simply add new dispatchee.
                if nqp::is_dispatcher($dispatcher) {
                    nqp::push_dispatchee($dispatcher, $code);
                }
                else {
                    die("Cannot have a multi candidate for $name when an only method is also in the class");
                }
            }
            else {
                # Go hunting in the MRO for a proto.
                my $j := 1;
                my $found := 0;
                while $j != +@!mro && !$found {
                    my $parent := @!mro[$j];
                    my %meths := $parent.HOW.method_table($parent);
                    my $dispatcher := %meths{$name};
                    if $dispatcher.defined {
                        # Found a possible - make sure it's a dispatcher, not
                        # an only.
                        if nqp::is_dispatcher($dispatcher) {
                            # Clone it and install it in our method table.
                            my @new_dispatchees;
                            @new_dispatchees[0] := $code;
                            %!methods{$name} := nqp::create_dispatch_and_add_candidates($dispatcher, @new_dispatchees);
                            $found := 1;
                        }
                        else {
                            die("Could not find a proto for multi $name (it may exist, but an only is hiding it if so)");
                        }
                    }
                    $j := $j + 1;
                }
                unless $found {
                    die("Could not find a proto for multi $name, and proto generation is NYI");
                }
            }
            $i := $i + 1;
        }
    }

    # XXX TODO: Get enough working to bring over the C3 implementation that
    # we run on 6model on Parrot. For now, we only build it for single
    # inheritance since it's obvious how to do it.
    sub compute_c3_mro($obj) {
        # MRO starts with this object.
        my @mro;
        @mro[0] := $obj;
        
        # Now add all parents until we have none.
        my $cur_obj := $obj;
        my @parents := $cur_obj.HOW.parents($cur_obj, :local(1));
        while +@parents {
            if +@parents == 1 {
                @mro.push($cur_obj := @parents[0]);
                @parents := $cur_obj.HOW.parents($cur_obj, :local(1));
            }
            else {
                die("Sorry, multiple inheritance is not yet implemented.");
            }
        }

        # Return MRO.
        @mro;
    }

    method publish_type_cache($obj) {
        # XXX TODO: when we have roles, need these here too.
        nqp::publish_type_check_cache($obj, @!mro)
    }

    ##
    ## Introspecty
    ##

    method attributes($obj, :$local!) {
        @!attributes
    }

    method method_table($obj) {
        %!methods
    }

    method name($obj) {
        $!name
    }

    method parents($obj, :$local!) {
        @!parents
    }

    method defined() {
        nqp::repr_defined(self)
    }

    ##
    ## Czechy
    ##

    method isa($obj, $checkee) {
        my $i := 0;
        my $mro_length := +@!mro;
        while $i != $mro_length {
            if @!mro[$i] =:= $checkee {
                return 1;
            }
            $i := $i + 1;
        }
        0;
    }

    method does($obj, $checkee) {
        0 # XXX TODO
    }

    method type_check($obj, $checkee) {
        self.isa($obj, $checkee) || self.does($obj, $checkee)
    }

    ##
    ## Dispatchy
    ##

    method find_method($obj, $name) {
        my $i := 0;
        my $mro_length := +@!mro;
        while $i != $mro_length {
            my %meths := @!mro[$i].HOW.method_table($obj);
            my $found := %meths{$name};
            if nqp::repr_defined($found) {
                return $found;
            }
            $i := $i + 1;
        }
        die("No method '$name' found in class '" ~ self.name($obj) ~ "'");
    }
}

# A simple attribute meta-object.
my knowhow NQPAttribute {
    has $!name;
    has $!has_accessor;
    has $!has_mutator;
    method new(:$name, :$has_accessor, :$has_mutator) {
        my $obj := nqp::instance_of(self.WHAT);
        $obj.BUILD(:name($name), :has_accessor($has_accessor),
            :has_mutator($has_mutator));
        $obj
    }
    
    method BUILD(:$name, :$has_accessor, :$has_mutator) {
        $!name := $name;
        $!has_accessor := $has_accessor;
        $!has_mutator := $has_mutator;
    }
    method name() {
        $!name
    }
    method has_accessor() {
        $!has_accessor
    }
    method has_mutator() {
        $!has_mutator
    }
    method compose($obj) {
        my $long_name := $!name;
        my $short_name := nqp::substr($!name, 2);
        if $!has_accessor {
            if $!has_mutator {
                $obj.HOW.add_method($obj, $short_name, method ($val?) {
                    nqp::repr_defined($val)
                        ?? nqp::bind_attr(self, $obj.WHAT, $long_name, $val)
                        !! nqp::get_attr(self, $obj.WHAT, $long_name);
                });
            } else {
                $obj.HOW.add_method($obj, $short_name, method () {
                    nqp::get_attr(self, $obj.WHAT, $long_name);
                });
            }
        }
    }
}

my knowhow NQPStash {
    has $!name;
    has $!namespaces;
    has $!entries;
    method new($name?) {
        my $obj := nqp::instance_of(self);
        $obj.BUILD($name);
        $obj
    }
    method BUILD($name) {
        $!name := $name;
        $!namespaces := NQPHash.new();
        $!entries := NQPHash.new();
    }
    method get_namespace($name) {
        my $got := $!namespaces.at_key($name);
        unless $got.defined {
            $got := NQPStash.new($name);
            $!namespaces.bind_key($name, $got);
        }
        $got
    }
    method at_key($name) {
        $!entries.at_key($name)
    }
    method bind_key($name, $value) {
        $!entries.bind_key($name, $value)
    }
    method defined() {
        nqp::repr_defined(self)
    }
}

::GLOBAL := NQPStash.new('GLOBAL');
