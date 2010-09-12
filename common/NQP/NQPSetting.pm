knowhow NQPStr is repr('P6str') {
    method new() {
        nqp::instance_of(self.WHAT);
    }
    method Bool() {
        nqp::logical_not_int(nqp::equal_strs(self, "")) &&
            nqp::logical_not_int(nqp::equal_strs(self, "0"))
    }
    method Str() {
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
    method Str() {
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
    method Str() {
        nqp::coerce_num_to_str(self, NQPStr)
    }
}

knowhow NQPList is repr('P6list') {
    method elems() {
        nqp::lllist_elems(self)
    }
    method at_pos($idx) {
        nqp::lllist_get_at_pos(self, $idx.Int)
    }
}

knowhow NQPArray is repr('P6list') {
}

# This is a little bit of a cheat. We only really need to keep
# hold of a name for the most basic attribute class, so we just
# use the string representation.
knowhow KnowHOWAttribute is repr('P6str') {
    method new(:$name) {
        nqp::box_str(nqp::unbox_str($name), KnowHOWAttribute)
    }
    method name() {
        nqp::box_str(nqp::unbox_str(self), NQPStr)
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

sub &prefix:<!>($x) {
    nqp::logical_not_int($x.Bool)
}

sub &prefix:<?>($x) {
    $x.Bool
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

# For tests.
my $count := NQPInt.new();
sub plan($n) {
    print("1..");
    say($n);
}
sub ok($check, $diag) {
    $count := $count + 1;
    unless $check { print("not ") }
    print("ok ");
    say($count);
}
