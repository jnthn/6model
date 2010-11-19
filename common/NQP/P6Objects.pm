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
    
    method isa($type) {
        return self.HOW.isa(self, $type);
    }
}

class Capture {
    has $.list is rw;
    has $.hash is rw;
    method new() {
        $!list := NQPList.new();
        $!hash := NQPHash.new();
        self
    }
    method at_pos($pos) {
        $!list.at_pos($pos)
    }
    method bind_pos($pos, $val) {
        $!list.bind_pos($pos, $val)
    }
    method at_key($key) {
        $!hash.at_key($key)
    }
    method bind_key($key, $val) {
        $!hash.bind_key($key, $val)
    }
    method Int() {
        $!list.Int
    }
    method Numeric() {
        self.Int
    }
}

class Match is Capture {
    has $.target is rw;
    has $.from is rw;
    has $.to is rw;
    has $.ast is rw;
    has $.cursor is rw;
    
    method chars() {
        $!to - $!from
    }
    
    method CURSOR() {
        $!cursor;
    }
    
    method Str() {
        substr($!target.Str, $!from, $!to - $!from)
    }
    method Bool() {
        $!to >= $!from
    }
    method Int() {
        $!to - $!from
    }
    method make($obj) {
        $!ast := $obj
    }
}

# Regex::Cursor is used for managing regular expression control flow
# and is also a base class for grammars.
class Regex::Cursor {
    has $.target is rw;
    has $.from is rw;
    has $.pos is rw;
    has $.match is rw;
    has $.names is rw;
    has $.debug is rw;
    has @.bstack is rw;
    has @.cstack is rw;
    has @.caparray is rw;
    has $.regex is rw;
    
    my $generation := 0;
    my $FALSE := 0;
    my $TRUE := 1;
    
    method new_match() {
        Match.new()
    }
    
    method new_array() {
        NQPList.new()
    }
    
    # Return this cursor's current Match object, generating a new one
    # for the Cursor if one hasn't been created yet.
    method MATCH() {
        my $match := $!match;
        if !nqp::repr_defined($match) || !$match {
            if !nqp::repr_defined($match) {
                # First, create a Match object and bind it
                $match := self.new_match();
                self.match($match);
                $match.cursor(self);
                $match.target($!target);
                $match.to($!to);
                $match.from($!from);
                
                # Create any arrayed subcaptures.
                if nqp::repr_defined(@!caparray) {
                    my @caparray := @!caparray;
                    my %caphash := NQPHash.new();
                    my @arr;
                    my $keyint;
                    for @caparray {
                        @arr := self.new_array();
                        %caphash{$_} := @arr;
                        if nqp::is_cclass_str_index("Numeric", $_, 0) {
                            $match.bind_pos($_, @arr);
                        } else {
                            $match.bind_key($_, @arr);
                        }
                    }
                    # If it's not a successful match, or if there are
                    # no saved subcursors, we're done.
                    my @cstack := @!cstack;
                    if $!to >= $!from || !nqp::repr_defined(@cstack) || !@cstack {
                        my $subcur;
                        my $submatch;
                        my $names;
                        my @namelist;
                        for @cstack {
                            if $_.isa(Regex::Cursor) {
                                $subcur := $_;
                                $names := $subcur.names;
                                if nqp::repr_defined($names) {
                                    $submatch := $subcur.MATCH();
                                    if nqp::index_str($names, "=") >= 0 {
                                        @namelist := nqp::split_str($names, "=")
                                    } else {
                                        @namelist := ();
                                        @namelist.push($names)
                                    }
                                    for @namelist {
                                        $keyint := nqp::is_cclass_str($_, "Numeric");
                                        if nqp::repr_defined(@caparray)
                                          && nqp::repr_defined(%caphash{$_}) {
                                            if $keyint {
                                                $match.key_at($_).push($submatch);
                                            } else {
                                                $match.pos_at($_).push($submatch);
                                            }
                                        } else {
                                            if $keyint {
                                                $match.bind_pos($_, $submatch);
                                            } else {
                                                $match.bind_key($_, $submatch);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        $match
    }
}













