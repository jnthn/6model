#! nqp

# Test INIT blocks

INIT plan(4);

our $foo;

ok($foo == 2, 'after second INIT block');

INIT {
    our $foo;
    ok(!$foo.defined, 'first INIT');
    $foo := 1;
}

$foo := 3;

INIT {
    ok($foo, 'after first INIT but before mainline');
    $foo := $foo + 1;
}

ok($foo == 3, 'After everything else');

