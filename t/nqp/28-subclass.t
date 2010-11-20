#! nqp

# class inheritance

plan(8);

class ABC {
    method foo() {
        say('ok 1');
    }

    method bar() {
        say('ok 3');
    }
}

class XYZ is ABC {
    method foo() {
        say('ok 2');
    }
}


my $abc := ABC.new();
my $xyz := XYZ.new();

$abc.foo();
$xyz.foo();
$xyz.bar();
my $xyzhow := $xyz.HOW;
if $xyzhow.isa($xyz, ABC) { say('ok 4') }
if $xyzhow.isa($xyz, XYZ) { say('ok 5') }
say( $abc.HOW.isa($abc, XYZ) ?? 'not ok 6' !! 'ok 6' );

# inherits from Mu
say( $xyzhow.isa($xyz, Mu) ?? 'ok 7' !! 'not ok 7' );

# inherits the Str method from Mu
say( $xyz.Str eq 'XYZ()' ?? 'ok 8' !! 'not ok 8' );
