knowhow NQPInt is repr('P6int') {
    method new() {
        nqp::instance_of(self.WHAT);
    }
    method Bool() {
        self != 0
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
        self ne ""
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
        self != 0.0
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
