#! nqp

plan(6);

my $count := 0;

my $x := -> $a, $b { ok($a == ($count := $count + 1), $b); }

$x(1, 'basic pointy block');

#my $y := -> $a, $b = 2 { ok($b == ($count := $count + 1), $a); }

say("ok 2 #SKIP argument defaults");
#$y('pointy block with optional');

say("ok 3 #SKIP argument defaults");
#$y('pointy block with optional + arg', 3);

say("ok 4 #SKIP for loops");
say("ok 5 #SKIP for loops");
say("ok 6 #SKIP for loops");
#for <4 pointy4 5 pointy5 6 pointy6> -> $a, $b { ok($a == ($count := $count + 1), $b); }
