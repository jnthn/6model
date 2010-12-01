# This is a very basic optimizer for NQP. It works over the PAST tree. It makes
# various assumptions that are NQP-specific. In particular (please update this
# list if you add optimizations):
#  * There is no eval that can see outer lexicals.
#  * All returning is done through a PAST::Op node with :pasttype('return')

class NQPOptimizer {
    method optimize($past) {
        # Set up a block stack with a fake block.
        my @*BLOCK_STACK;
        @*BLOCK_STACK.push(PAST::Block.new());

        # Run the optimizer.
        optimizer($past);

        $past
    }
}

our multi optimizer(PAST::Block $block) {
    # Mark that our parent block has an inner block and unshift ourself
    # onto the block stack.
    annotate(@*BLOCK_STACK[0], 'has_nested_blocks', 1);
    @*BLOCK_STACK.unshift($block);

    # Go through the block and call the optimizer on everything in it.
    for @($block) {
        optimizer($_);
    }

    # If we have a return handler but nothing marked it used, toss it.
    if $block.control && !get_annotation($block, 'return_used') {
        $block.control('');
    }

    # If there's no inner blocks, we can demote lexicals to constants.
    unless get_annotation($block, 'has_nested_blocks') {
        demote_lexicals_to_locals($block);
    }

    # Remove this block from the block stack.
    @*BLOCK_STACK.shift();
}

our multi optimizer(PAST::Stmts $stmts) {
    # Need to do nothing other than loop through children.
    for @($stmts) {
        optimizer($_);
    }
}

our multi optimizer(PAST::Op $op) {
    # Need to visit the children.
    for @($op) {
        optimizer($_);
    }

    # If it's a return node, go find something with a return handler
    # and annotate that the return is used.
    if $op.pasttype eq 'return' {
        for @*BLOCK_STACK {
            if $_.control {
                annotate($_, 'return_used', 1);
                last;
            }
        }
    }
}

our multi optimizer(PAST::Var $var) {
    # May need to visit the viviself as well as children.
    if $var.viviself ~~ PAST::Node {
        optimizer($var.viviself);
    }
    for @($var) {
        optimizer($_);
    }
}

our multi optimizer(PAST::Val $stmts) {
    # Nothing to do at all :-)
}

our multi optimizer(PAST::Regex $regex) {
    # XXX Don't know about this yet really, but at least visit the children.
    for @($regex) {
        optimizer($_);
    }
}

our multi sub optimizer($any) {
    if pir::isa($any, 'String') || pir::isa($any, 'Integer') || pir::isa($any, 'Float') {
        # Literals - nothing to do.
    }
    else {
        pir::die("optimizer() is missing a candidate for " ~ pir::typeof__SP($any) ~ "(" ~ $any ~ ")");
    }
}

# Annotates a node with a given note.
sub annotate($node, $key, $value) {
    $node{'nqp_opt_' ~ $key} := $value;
}

# Gets an annotation made on a node.
sub get_annotation($node, $key) {
    $node{'nqp_opt_' ~ $key}
}

# Demotes lexicals to local variables.
sub demote_lexicals_to_locals($block) {
    my %*demoted;
    my $*counter := 0;
    for @($block) {
        demote_lexicals_worker($_);
    }
}
our multi sub demote_lexicals_worker(PAST::Stmts $stmts) {
    for @($stmts) {
        demote_lexicals_worker($_);
    }
}
our multi sub demote_lexicals_worker(PAST::Var $var) {
    if %*demoted{$var.name} {
        $var.scope('register');
        $var.name(%*demoted{$var.name});
    }
    elsif $var.isdecl && $var.scope eq 'lexical' {
        # Make sure it's not a context var.
        my $twigil := pir::substr($var.name, 1, 1);
        if $twigil ne '*' && $twigil ne '?' {
            # Good demotion candidate.
            my $new_name := 'demoted_lexical_' ~ $*counter;
            $*counter := $*counter + 1;
            %*demoted{$var.name} := $new_name;
            $var.name($new_name);
            $var.scope('register');
        }
    }
    if $var.viviself {
        demote_lexicals_worker($var.viviself);
    }
    for @($var) {
        demote_lexicals_worker($_);
    }
}
our multi sub demote_lexicals_worker(PAST::Op $op) {
    for @($op) {
        demote_lexicals_worker($_);
    }
}
our multi sub demote_lexicals_worker($any) {
    # Nothing to do for this node type
}
