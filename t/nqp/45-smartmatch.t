# This is a slightly simplified version of the nqp-rx one, primarily
# to make sure the basic bits of regexes in place at the time it was
# added didn't get regressed.

plan(8);

my $match := 'abcdef' ~~ / abc /;
ok( $match, "simple smart match, scanning form" );
ok( $match.from == 0, "match has correct .from" );
ok( $match.to == 3, "match has correct .to");
ok( $match eq 'abc', "match has correct string value" );

$match := 'abcdef' ~~ / '' /;
ok( $match, "successfully match empty string");

ok(("hi" ~~ /i/) eq 'i', "basic non-anchored scanning works");
ok(("hihi" ~~ /i hi/) eq 'ihi', "rule concatenation works");
ok(("hihi" ~~ /i [h i]/) eq 'ihi', "non-capturing groups work");
