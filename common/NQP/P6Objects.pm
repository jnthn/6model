
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
    has $.cap;
    method new() {
        $!cap := nqp::instance_of(NQPCapture);
    }
    method at_pos($pos) {
        nqp::llcap_get_at_pos($!cap, $pos)
    }
    method at_key($key) {
        nqp::llcap_get_at_key($!cap, $key)
    }
    method bind_pos($pos, $val) {
        nqp::llcap_bind_at_pos($!cap, $pos, $val)
    }
    method bind_key($key, $val) {
        nqp::llcap_bind_at_key($!cap, $key, $val)
    }
}

# the following was originally ported/transliterated directly from the PIR
# in nqp-rx, the source of which was copyright 2009 The Perl Foundation,
# and authored by Patrick Michaud <pmichaud@pobox.com>

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
    # XXX make const
    my $FALSE := 0;
    my $TRUE := 1;
    my $CURSOR_FAIL := -1;
    my $CURSOR_FAIL_GROUP := -2;
    my $CURSOR_FAIL_RULE := -3;
    my $CURSOR_FAIL_MATCH := -4;
    
    method new_match() {
        Match.new()
    }
    
    method new_array() {
        NQPArray.new()
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
                            if $_ ~~ Regex::Cursor {
                                $subcur := $_;
                                $names := $subcur.names;
                                if nqp::repr_defined($names) {
                                    $submatch := $subcur.MATCH();
                                    if nqp::index_str($names, "=") >= 0 {
                                        @namelist := nqp::split_str($names, "=")
                                    } else {
                                        @namelist := [];
                                        @namelist.push($names)
                                    }
                                    for @namelist {
                                        $keyint := nqp::is_cclass_str($_, "Numeric");
                                        if nqp::repr_defined(@caparray)
                                          && nqp::repr_defined(%caphash{$_}) {
                                            if $keyint {
                                                $match.pos_at($_).push($submatch);
                                            } else {
                                                $match.key_at($_).push($submatch);
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
    
    # Parse C<target> in the current grammar starting with C<regex>.
    # If C<regex> is omitted, then use the C<TOP> rule for the grammar.
    method parse($target, :$rule?, :$actions?, :$rxtrace?, *%options) {
        $rule := 'TOP' unless nqp::repr_defined($rule);
        if $rule ~~ NQPStr {
            $rule := self.HOW.find_method($rule);
        }
        my $*ACTIONS := $actions;
        my $cur := self.cursor_init($target, :p(%options{'p'}), :c(%options{'c'}));
        if $rxtrace {
            $cur.DEBUG
        }
        my $cap := Capture.new();
        $cap.bind_pos(0, $cur);
        Ops.invoke($rule, $cap).MATCH;
    }
    
    # Return the next match from a successful Cursor.
    method next() {
        $!cursor_next().MATCH
    }
    
    # Create a new cursor for matching C<target>.
    method cursor_init($target, $p, $c) {
        my $cur := nqp::instance_of(self);
        $cur.target($target);
        if nqp::repr_defined($c) {
            $cur.from($CURSOR_FAIL);
            $cur.pos($c);
        } else {
            $cur.from($p);
            $cur.pos($p);
        }
        $cur
    }
    
    # Create and initialize a new cursor from C<self>.  If C<lang> is
    # provided, then the new cursor has the same type as lang.
    method cursor_start($lang?) {
        $lang := self unless nqp::repr_defined($lang);
        my $cur := nqp::instance_of(self);
        my $regex := $!regex;
        $cur.from($!from);
        $cur.target($!target);
        $cur.debug($!debug);
        if nqp::repr_defined($regex) {
            $cur.pos($CURSOR_FAIL);
            if nqp::repr_defined(@!cstack) {
                my @cstack := NQPArray.new();
                for @!cstack {
                    @cstack.push($_);
                }
                $cur.cstack(@cstack);
            }
            if nqp::repr_defined(@!bstack) {
                my @bstack := NQPArray.new();
                for @!bstack {
                    @bstack.push($_);
                }
                $cur.bstack(@bstack);
            }
        } else {
            $cur.pos($!from);
        }
        [$cur, $!from, $!target, 0]
    }
    
    # Permanently fail this cursor.
    method cursor_fail() {
        $!pos := $CURSOR_FAIL_RULE;
        $!match := Mu;
        @!bstack := Mu;
        @!cstack := Mu;
    }
    
    # Set the Cursor as passing at C<pos>; calling any reduction action
    # C<name> associated with the cursor.  This method simply sets
    # C<$!match> to a boolean true value to indicate the regex was
    # successful; the C<MATCH> method above replaces this boolean true
    # with a "real" Match object when requested.
    method cursor_pass($pos, $name) {
        $!pos := $pos;
        $!match := $TRUE;
        self.reduce($name) if nqp::repr_defined($name);
        self
    }
    
    # Configure this cursor for backtracking via C<!cursor_next>.
    method cursor_backtrack() {
        $!regex := nqp::get_caller_sub(0);
    }
    
    # Log a debug message
    method cursor_debug() {
        # XXX finish
    }
    
    # Push a new backtracking point onto the cursor with the given
    # C<rep>, C<pos>, and backtracking C<mark>.  (The C<mark> is typically
    # the address of a label to branch to when backtracking occurs.)
    method mark_push($rep, $pos, $mark, $subcur?) {
        my $cptr;
        my @bstack := @!bstack;
        if !nqp::repr_defined(@bstack) {
            @!bstack := @bstack := [];
            $cptr := 0;
        } elsif ($cptr := @bstack.Int) > 0 {
            $cptr := $cptr - 1;
        } else {
            $cptr := 0;
        }
        if nqp::repr_defined($subcur) {
            $!match := Mu;
            my @cstack := @!cstack;
            unless nqp::repr_defined(@cstack) {
                @!cstack := @cstack := [];
            }
            @cstack[$cptr] := $subcur;
            $cptr = $cptr + 1;
        }
        @bstack.push($mark);
        @bstack.push($pos);
        @bstack.push($rep);
        @bstack.push($cptr);
    }
    
    # Return information about the latest frame for C<mark>.
    # If C<mark> is zero, return information about the latest frame.
    method mark_peek($tomark) {
        my @bstack := @!bstack;
        my $bptr;
        if nqp::repr_defined(@bstack) && ($bptr := @bstack.Int) >= 0 {
            $mark := @bstack[$bptr := $bptr - 4] while $tomark != 0 && $mark != $tomark;
            return [@bstack[$bptr + 2], @bstack[$bptr + 1], $mark,
              $bptr, @bstack, $cptr];
        }
        [0, $CURSOR_FAIL_GROUP, 0, 0, @bstack, 0]
    }
    
    # Remove the most recent C<mark> and backtrack the cursor to the
    # point given by that mark.  If C<mark> is zero, then
    # backtracks the most recent mark.  Returns the backtracked
    # values of repetition count, cursor position, and mark (address).
    method mark_fail($mark) {
        my @frame := self.mark_peek($mark);
        my $rep := @frame[0];
        my $pos := @frame[1];
        $mark := @frame[2];
        my $bptr := @frame[3];
        my @bstack := @frame[4];
        my $cptr := @frame[5];
        
        $!match := Mu;
        
        my $subcur;
        
        if nqp::repr_defined(@bstack) {
            if $cptr <= 0 {
                my $cstack := @!cstack;
                $cptr := $cptr - 1;
                $subcur := @cstack[$cptr];
                nqp::lllist_truncate_to(@cstack, $bptr > 0
                    ?? @bstack[$bptr - 1]
                    !! 0;
            }
            nqp::lllist_truncate_to(@bstack, $bptr);
        }
        [$rep, $pos, $mark, $subcur]
    }
    
    # Like C<!mark_fail> above this backtracks the cursor to C<mark>
    # (releasing any intermediate marks), but preserves the current
    # capture states.
    method mark_commit($mark) {
        my @frame := self.mark_peek($mark);
        my $rep := @frame[0];
        my $pos := @frame[1];
        $mark := @frame[2];
        my $bptr := @frame[3];
        my @bstack := @frame[4];
        my $cptr;
        if nqp::repr_defined(@bstack) && ($cptr := @bstack.Int - 1) > -1 {
            my $i0;
            my $i1;
            nqp::lllist_truncate_to(@bstack, $bptr);
            if $cptr > 0 {
                if $bptr > 0 && @bstack[$bptr - 3] < 0 {
                    @bstack[$bptr - 1] := $cptr;
                } else {
                    @bstack.push(0);
                    @bstack.push($CURSOR_FAIL);
                    @bstack.push(0);
                    @bstack.push($cptr);
                }
            }
        }
        [$rep, $pos, $mark]
    }
    
    method reduce($name, $key?, $match?) {
        my $actions := $*ACTIONS;
        if nqp::repr_defined($actions) {
            if $actions.can($name) {
                $match := self.MATCH unless nqp::repr_defined($match);
                if nqp::repr_defined($key) {
                    actions."$name"($match, $key);
                } else {
                    actions."$name"($match);
                }
            }
        }
    }
}















