class Mu {
    method new() {
        self.CREATE()
    }

    method CREATE() {
        nqp::instance_of(self)
    }
}
