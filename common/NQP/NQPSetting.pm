knowhow NQPInt is repr('P6int') {
    method new() {
        nqp::instance_of(self.WHAT);
    }
    method Int() {
        self
    }
    method Str() {
        nqp::coerce_int_to_str(self, NQPStr)
    }
}

knowhow NQPStr is repr('P6str') {
    method new() {
        nqp::instance_of(self.WHAT);
    }
    method Str() {
        self
    }
}

knowhow NQPNum is repr('P6num') {
    method new() {
        nqp::instance_of(self.WHAT);
    }
    method Num() {
        self
    }
    method Str() {
        nqp::coerce_num_to_str(self, NQPStr)
    }
}
