plan(2);

class Foo {
    has $!x;
    method y() {
        if $!x.defined {
            say("ok 2 - got value")
        } else {
            say("ok 1 - viviself, not null deference")
        }
    }
    method set() { $!x := 42 }
}
my $f := Foo.new;
$f.y;
$f.set;
$f.y;