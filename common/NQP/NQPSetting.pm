knowhow NQPStr is repr('P6str') {
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
}

knowhow NQPInt is repr('P6int') {
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
}

knowhow NQPNum is repr('P6num') {
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
}

knowhow NQPList is repr('P6list') {
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
        nqp::lllist_get_at_pos(self, $idx.Int)
    }
}

knowhow NQPArray is repr('P6list') {
    method new() {
        nqp::instance_of(self.WHAT)
    }
    method Numeric() {
        self.elems
    }
    method elems() {
        nqp::lllist_elems(self)
    }
    method at_pos($idx) {
        nqp::lllist_get_at_pos(self, $idx.Int)
    }
    method bind_pos($idx, $value) {
        nqp::lllist_bind_at_pos(self, $idx.Int, $value)
    }
}

knowhow NQPHash is repr('P6mapping') {
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
        nqp::llmapping_get_at_key(self, $key.Str)
    }
    method bind_key($key, $value) {
        nqp::llmapping_bind_at_key(self, $key.Str, $value)
    }
}

knowhow NQPCode is repr('RakudoCodeRef') {
    method leave($with) {
        nqp::leave_block(self, $with)
    }
}

## XXX Need coercive Any fallbacks too.

proto sub &infix:<==>($x, $y) {
    nqp::multi_dispatch_over_lexical_candidates("&infix:<==>");
}
multi sub &infix:<==>(NQPInt $x, NQPInt $y) {
    nqp::equal_ints($x, $y)
}
multi sub &infix:<==>(NQPNum $x, NQPNum $y) {
    nqp::equal_nums($x, $y)
}

proto sub &infix:<!=>($x, $y) {
    nqp::multi_dispatch_over_lexical_candidates("&infix:<!=>");
}
multi sub &infix:<!=>(NQPInt $x, NQPInt $y) {
    nqp::logical_not_int(nqp::equal_ints($x, $y))
}
multi sub &infix:<!=>(NQPNum $x, NQPNum $y) {
    nqp::logical_not_int(nqp::equal_nums($x, $y))
}

sub &infix:<eq>($x, $y) {
    nqp::equal_strs($x.Str, $y.Str)
}

sub &infix:<ne>($x, $y) {
    !nqp::equal_strs($x.Str, $y.Str)
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

proto sub &infix:<+>($x, $y) {
    nqp::multi_dispatch_over_lexical_candidates("&infix:<+>");
}
multi sub &infix:<+>(NQPInt $x, NQPInt $y) {
    nqp::add_int($x, $y);
}

proto sub &infix:<->($x, $y) {
    nqp::multi_dispatch_over_lexical_candidates("&infix:<->");
}
multi sub &infix:<->(NQPInt $x, NQPInt $y) {
    nqp::sub_int($x, $y);
}

proto sub &infix:<*>($x, $y) {
    nqp::multi_dispatch_over_lexical_candidates("&infix:<*>");
}
multi sub &infix:<*>(NQPInt $x, NQPInt $y) {
    nqp::mul_int($x, $y);
}

proto sub &infix:</>($x, $y) {
    nqp::multi_dispatch_over_lexical_candidates("&infix:</>");
}
multi sub &infix:</>(NQPInt $x, NQPInt $y) {
    nqp::div_int($x, $y);
}

proto sub &infix:<%>($x, $y) {
    nqp::multi_dispatch_over_lexical_candidates("&infix:<%>");
}
multi sub &infix:<%>(NQPInt $x, NQPInt $y) {
    nqp::mod_int($x, $y);
}

sub &infix:<~>($x, $y) {
    nqp::concat($x.Str, $y.Str);
}

# A basic, fairly bare-bones exception object.
knowhow NQPException {
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

# XXX Bad hack, we'll replace this later.
knowhow Any {
}

# GLOBAL stash.
# (XXX Really want one per compilation unit and unify, but this will get us
# started. Also really want a stash type that knows its name rather than just
# a hash, I guess.)
::GLOBAL := NQPHash.new();
