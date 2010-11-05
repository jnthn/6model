plan(5);

proto foo($a, $b?) { * }
multi foo($a) { 1 }
multi foo($a, $b) { 2 }
ok(foo('omg') == 1);
ok(foo('omg', 'wtf') == 2);

proto bar($a?) { 'omg' ~ {*} ~ 'bbq' }
multi bar() { 'wtf' }
multi bar($a) { 'lol' }
ok(bar() eq 'omgwtfbbq');
ok(bar(42) eq 'omglolbbq');

proto klozhur($a) { return { {*} + 2 } }
multi klozhur($a) { 2 * $a }
my $c := klozhur(20);
ok($c() == 42);
