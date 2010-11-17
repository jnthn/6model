# auto-generatored accessors & mutators
#   but not lvalue as in Perl 6, just "fluent" mutator methods

plan(2);

class Foo3 { has $.bar is rw }

my $foo := Foo3.new();

# doesn't work yet; no viviself for attributes :(
#ok(!$foo.bar());

ok($foo.bar("baz") eq "baz");

ok($foo.bar eq "baz");
