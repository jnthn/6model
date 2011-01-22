# auto-generated accessors & mutators
#   but not lvalue as in Perl 6, just "fluent" mutator methods

plan(4);

class Foo3 { has $.bar is rw }

my $foo := Foo3.new();

# doesn't work yet; no viviself for attributes :(
#ok(!$foo.bar());

ok($foo.bar("baz") eq "baz");

ok($foo.bar eq "baz");

class Foo4 {
    has $.foo;

    method set_foo($val) {
        $.foo := $val
    }
}

my $foo4 := Foo4.new();

ok($foo4.set_foo(99) == 99);

ok($foo4.foo == 99);

