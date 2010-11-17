class Mu {
    method new() {
        self.CREATE()
    }

    method CREATE() {
        nqp::instance_of(self)
    }

    proto method Str() { * }
    multi method Str(Mu:U $self:) {
        self.HOW.name(self) ~ '()'
    }

    proto method ACCEPTS($topic) { * }
    multi method ACCEPTS(Mu:U $self: $topic) {
        nqp::type_check($topic, self.WHAT)
    }
    method defined() {
        nqp::repr_defined(self)
    }
}

class Capture is Mu {
    has $.target is rw;
}

class Match is Mu {
    has $.target;
    has $.from;
    has $.pos;
    
    method chars() {
        $!pos - $!from;
    }
    
    multi method Str() {
        substr($!target.Str, $!from, $!pos - $!from)
    }
}
