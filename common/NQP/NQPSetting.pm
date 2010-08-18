knowhow NQPInt is repr('P6int') {
    method new() {
        nqp::instance_of(self.WHAT);
    }
    method Bool() {
        nqp::logical_not_int(nqp::equal_ints(self, 0, NQPInt), NQPInt)
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

knowhow NQPStr is repr('P6str') {
    method new() {
        nqp::instance_of(self.WHAT);
    }
    method Bool() {
        nqp::logical_not_int(nqp::equal_strs(self, "", NQPInt), NQPInt) &&
            nqp::logical_not_int(nqp::equal_strs(self, "0", NQPInt), NQPInt)
    }
    method Str() {
        self
    }
}

knowhow NQPNum is repr('P6num') {
    method new() {
        nqp::instance_of(self.WHAT);
    }
    method Bool() {
        nqp::logical_not_int(nqp::equal_nums(self, 0.0, NQPInt), NQPInt)
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

## XXX All of these should become multi when we can do that.

sub &infix:<==>($x, $y) {
    nqp::equal_nums($x.Num, $y.Num, NQPInt)
}

sub &infix:<!=>($x, $y) {
    !nqp::equal_nums($x.Num, $y.Num, NQPInt)
}

sub &infix:<eq>($x, $y) {
    nqp::equal_strs($x.Str, $y.Str, NQPInt)
}

sub &infix:<ne>($x, $y) {
    !nqp::equal_strs($x.Str, $y.Str, NQPInt)
}

sub &prefix:<!>($x) {
    nqp::logical_not_int($x.Bool.Int, NQPInt)
}

sub &prefix:<?>($x) {
    $x.Bool
}

sub &infix:<+>($x, $y) {
    nqp::add_int($x.Int, $y.Int, NQPInt);
}

sub &infix:<->($x, $y) {
    nqp::sub_int($x.Int, $y.Int, NQPInt);
}

sub &infix:<*>($x, $y) {
    nqp::mul_int($x.Int, $y.Int, NQPInt);
}

sub &infix:</>($x, $y) {
    nqp::div_int($x.Int, $y.Int, NQPInt);
}

sub &infix:<%>($x, $y) {
    nqp::mod_int($x.Int, $y.Int, NQPInt);
}

sub &infix:<~>($x, $y) {
    nqp::concat($x.Str, $y.Str, NQPStr);
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
