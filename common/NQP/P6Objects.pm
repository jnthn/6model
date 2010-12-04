
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
        self.HOW.isa(self, $type)
    }
    
    method Bool() {
        nqp::repr_defined(self)
    }
}

class Capture {
    has $.cap;
    method new() {
        my $obj := self.CREATE;
        $obj.BUILD;
        $obj;
    }

    method CREATE() {
        nqp::instance_of(self)
    }
    
    method BUILD() {
        $!cap := nqp::instance_of(NQPCapture);
    }
    method pos_at($pos) {
        nqp::llcap_get_at_pos($!cap, $pos)
    }
    method key_at($key) {
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
    has $.off is rw;
    has $.match is rw;
    has $.names is rw;
    has $.debug is rw;
    has @.bstack is rw;
    has @.cstack is rw;
    has @.caparray is rw;
    has $.regex is rw;
    has $.special is rw;
    
    my $generation := 0;
    # XXX make const
    my $FALSE := 0;
    my $TRUE := 1;
    my $CURSOR_FAIL := -1;
    my $CURSOR_FAIL_GROUP := -2;
    my $CURSOR_FAIL_RULE := -3;
    my $CURSOR_FAIL_MATCH := -4;
    
    method new_match() {
        Match.new
    }
    
    method new_array() {
        []
    }
    
    method new() {
        self.CREATE();
    }
    
    method CREATE() {
        nqp::instance_of(self)
    }
    
    method from_from($set?) {
        my $res;
        if $!from.WHAT =:= NQPInt {
            #say('from is an integer!');
            $!from := $set if nqp::repr_defined($set);
            $res := $!from
        } else {
            $res := -1
        }
        $res
    }
    
    # index representing 'off the end of the string'
    # (or 1-based length)
    method eos() {
        nqp::length_str($!target)
    }
    
    # Return this cursor's current Match object, generating a new one
    # for the Cursor if one hasn't been created yet.
    method MATCH() {
        my $match := $!match;
        if $match.WHAT =:= NQPInt {
            # First, create a Match object and bind it
            $match := self.new_match;
            $!match := $match;
            $match.cursor(self);
            $match.target($!target);
            $match.to($!pos);
            $match.from($!from);
            
            # Create any arrayed subcaptures.
            if +@!caparray {
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
        $match
    }
    
    # Parse C<target> in the current grammar starting with C<regex>.
    # If C<regex> is omitted, then use the C<TOP> rule for the grammar.
    method parse($target, :$rule?, :$actions?, :$rxtrace?, :$p?, :$c?) {
        #say("in parse");
        $rule := 'TOP' unless nqp::repr_defined($rule);
        if $rule.WHAT =:= NQPStr {
            $rule := self.HOW.find_method($rule);
        }
        my $*ACTIONS := $actions;
        my $cur := nqp::repr_defined($c)
            ?? self.cursor_init($target, nqp::repr_defined($p) ?? $p !! 0, $c)
            !! self.cursor_init($target, nqp::repr_defined($p) ?? $p !! 0);
        #if nqp::repr_defined($rxtrace) {
        #    $cur.DEBUG
        #}
        #$cur.from(-1); # XXX ??????
        my $cap := Capture.new;
        $cap.bind_pos(0, $cur);
        $rule($cur, $cap).MATCH
    }
    
    # Return the next match from a successful Cursor.
    method next() {
        $!cursor_next().MATCH
    }
    
    # Create a new cursor for matching C<target>.
    method cursor_init($target, $p, $c?) {
        #say("in cursor_init");
        my $cur := nqp::instance_of(self);
        $cur.target($target);
        $cur.off(0); # not actually used in this impl (yet?)
        #$cur.debug(1);
        if nqp::repr_defined($c) {
            # means "has continuation"
            $cur.from($CURSOR_FAIL);
            $cur.pos($c);
        } else {
            $cur.from($p);
            $cur.pos($p);
        }
        $cur.special(-1);
        $cur
    }
    
    # Create and initialize a new cursor from C<self>.  If C<lang> is
    # provided, then the new cursor has the same type as lang.
    method cursor_start($lang?) {
        $lang := self unless nqp::repr_defined($lang);
        my $cur := nqp::instance_of(self);
        my $regex := $!regex;
        $!from := 0 unless $!from.WHAT =:= NQPInt;
        $cur.from($!from);
        $cur.target($!target);
        $cur.debug($!debug);
        my $ret;
        if $regex {
            #say('there was a regex');
            $cur.pos($CURSOR_FAIL);
            if nqp::repr_defined(@!cstack) {
                my @cstack := [];
                for @!cstack {
                    @cstack.push($_);
                }
                $cur.cstack(@cstack);
            }
            if nqp::repr_defined(@!bstack) {
                my @bstack := [];
                for @!bstack {
                    @bstack.push($_);
                }
                $cur.bstack(@bstack);
            }
            $ret := [$cur, $!from, $!target, 1];
        } else {
            $cur.pos($!from);
            $cur.from($!from);
            $cur.target($!target);
            $cur.debug($!debug);
            $ret := [$cur, $!from, $!target, 0];
        }
        $ret
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
    method cursor_debug($tag, *@args) {
        if $!debug {
            say("from: $!from; $tag " ~ join(' ', @args))
        }
    }
    
    # Push a new backtracking point onto the cursor with the given
    # C<rep>, C<pos>, and backtracking C<mark>.  (The C<mark> is typically
    # the address of a label to branch to when backtracking occurs.)
    method mark_push($rep, $pos, $mark, $subcur?) {
        #say("in mark_push");
        my $cptr;
        my @bstack := @!bstack;
        if ($cptr := +@bstack) > 0 {
            $cptr := $cptr - 1;
        } else {
            $cptr := 0;
        }
        #say("cptr is $cptr");
        if nqp::repr_defined($subcur) {
            $!match := Mu;
            my @cstack := @!cstack;
            unless nqp::repr_defined(@cstack) {
                @!cstack := @cstack := [];
            }
            @cstack[$cptr] := $subcur;
            $cptr := $cptr + 1;
        }
        #say("pos is $pos");
        @bstack.push($mark);
        @bstack.push($pos);
        @bstack.push($rep);
        @bstack.push($cptr);
        #say("bstack now has " ~ +@bstack ~ " items");
        @!bstack := @bstack;
    }
    
    # Return information about the latest frame for C<mark>.
    # If C<mark> is zero, return information about the latest frame.
    method mark_peek($tomark) {
        #say("in mark_peek");
        my @bstack := @!bstack;
        $tomark := +$tomark;
        my $bptr := +@bstack - 4;
        my $mark;
        my $res;
        while !nqp::repr_defined($res) {
            #say("bptr is $bptr");
            if $bptr >= 0 {
                $mark := @bstack[$bptr];
                #say("tomark is " ~ +$tomark);
                #say("mark is " ~ +$mark );
                if $tomark == 0 || $mark == $tomark {
                    # rep, pos, mark, bptr, bstack, cptr
                    #say("setting result");
                    $res := [@bstack[$bptr + 2], @bstack[$bptr + 1], $mark,
                        $bptr, @bstack, @bstack[$bptr + 3]]
                #} else {
                #    say("NOT setting result");
                }
                $bptr := $bptr - 4
            } else {
                $res := [0, $CURSOR_FAIL_GROUP, 0, 0, @bstack, 0]
            }
        }
        #say('returning ' ~ join(' ', $res));
        $res;
    }
    
    # Remove the most recent C<mark> and backtrack the cursor to the
    # point given by that mark.  If C<mark> is zero, then
    # backtracks the most recent mark.  Returns the backtracked
    # values of repetition count, cursor position, and mark (address).
    method mark_fail($mark) {
        #say("in mark_fail");
        my @frame := self.mark_peek($mark);
        my $rep := @frame[0];
        #say("rep is $rep");
        my $pos := @frame[1];
        #say("pos is $pos");
        $mark := @frame[2];
        #say("mark is $mark");
        my $bptr := @frame[3];
        #say("bptr is $bptr");
        my @bstack := @frame[4];
        my $cptr := @frame[5];
        #say("cptr is $cptr");
        
        $!match := Mu;
        
        my $subcur;
        
        if +@bstack {
            #say('bstack was defined');
            if $cptr > 0 {
                my @cstack := @!cstack;
                $cptr := $cptr - 1;
                $subcur := @cstack[$cptr] if +@cstack > $cptr;
                nqp::lllist_truncate_to(@cstack, $bptr > 0
                    ?? @bstack[$bptr - 1]
                    !! 0);
            }
            nqp::lllist_truncate_to(@bstack, $bptr);
        }
        [$rep, $pos, $mark, $subcur]
    }
    
    # Like C<!mark_fail> above this backtracks the cursor to C<mark>
    # (releasing any intermediate marks), but preserves the current
    # capture states.
    method mark_commit($mark) {
        #say("in mark_commit");
        my @frame := self.mark_peek($mark);
        my $rep := @frame[0];
        my $pos := @frame[1];
        $mark := @frame[2];
        my $bptr := @frame[3];
        my @bstack := @frame[4];
        my $cptr;
        if nqp::repr_defined(@bstack) && ($cptr := +@bstack - 1) > -1 {
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
    
    method DEBUG($arg?) {
        $!debug := nqp::repr_defined($arg) ?? $arg !! $TRUE;
        1
    }
}

class Regex::Regex {
    has $!regex_block;
    method new($regex_block) {
        my $obj := nqp::instance_of(self);
        $obj.BUILD($regex_block);
        $obj
    }
    method BUILD($regex_block) {
        $!regex_block := $regex_block;
    }
    method ACCEPTS($target) {
        Regex::Cursor.parse($target, :rule($!regex_block))
    }
}















