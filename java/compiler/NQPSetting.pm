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
        nqp::lllist_get_at_pos(self, $idx.Int)
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
    method at_pos($idx) {
        nqp::lllist_get_at_pos(self, $idx.Int)
    }
    method bind_pos($idx, $value) {
        nqp::lllist_bind_at_pos(self, $idx.Int, $value)
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
        nqp::llmapping_get_at_key(self, $key.Str)
    }
    method bind_key($key, $value) {
        nqp::llmapping_bind_at_key(self, $key.Str, $value)
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
