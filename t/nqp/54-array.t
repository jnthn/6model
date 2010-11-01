#! nqp

plan(6);

# test push, pop, unshift, shift

my $arr := [];
$arr.unshift(6);
$arr.push(5);
$arr.unshift(4);
$arr.push(3);
$arr.unshift(2);
$arr.push(1);
print("ok "); say($arr.pop);
print("ok "); say($arr.shift);
print("ok "); say($arr.pop);
print("ok "); say($arr.shift);
print("ok "); say($arr.pop);
print("ok "); say($arr.shift);
