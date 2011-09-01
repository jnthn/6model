/* mem.c */

/*
These functions manage the standard C library functions malloc(),
realloc() and free() to automatically reclaim unused memory.  They do
not impose an "all your pointers are belong to us" regime.  If you
bypass them at your own risk, use tools such as valgrind to check for
leaks.

The algorithms are distinct from reference counting and tracing (mark
and sweep) whilst incorporating aspects of both.  The data structures
and code aim for most of the benefits of those techniques without the
drawbacks.  In particular, this design tries to maximize processor cache
performance, keeping the working set small by freeing unused memory
early.  There is more documentation at the end of this file.

The code is being written in stages using two techniques:
1. Using simple reference counting to get something usable asap.
2. Using reference tracking with more metadata about references so
   as to detect cycles and free them when they are no longer used.
*/
                         
#include <assert.h>  /* assert */
#include <string.h>  /* memset */
#include <stdlib.h>  /* malloc realloc free */
#include "mem.h"     /* this file's API */


/* mem_init */
void *
mem_init()
{
    struct mem_block    * rootblock;
    struct mem_rootdata * rootdata;
    rootblock = (struct mem_block *) malloc(
        sizeof(struct mem_block) + sizeof(struct mem_rootdata)
    );
    #if defined(REFCOUNT_ONLY)
        rootdata = (struct mem_rootdata *)(rootblock + 1);
        rootblock->rootdata = rootdata; /* only the root is loopy */
    #else
        struct mem_node * node;
        node = (struct mem_node *) malloc(sizeof(struct mem_node));
        node->rootnode = node;  /* only the root is loopy */
        rootdata = ((void *) node) + sizeof(struct mem_node);
    #endif
    rootdata->totalobjects = 0;
    rootdata->totalbytes = 0L;
    /* TODO: start a collector thread */
    return (void *) rootdata;
}


/* mem_final */
void
mem_final(void * rootdata, int freeflag)
{
    struct mem_block * rootblock;
    /* TODO: stop the collector thread */
    if (freeflag) {
        #if defined(REFCOUNT_ONLY)
            rootblock = (struct mem_block *)rootdata - 1;
            free(rootblock);
        #endif
    }
}


/* mem_scalar_new */
void *
mem_scalar_new(void * parent, int size)
{
    struct mem_block * block;
    struct mem_node * node;
    assert( size > 0 );
    block = (struct mem_block *) malloc(sizeof(struct mem_block) + size);
    node = (struct mem_node *) malloc(sizeof(struct mem_node));
    block->node = node;
    node->block = block;
    block->size = size;
    block->rootdata = (struct mem_rootdata *) parent;
    block->rootdata->totalobjects++;
    block->rootdata->totalbytes += size;
    #if defined(REFCOUNT_ONLY)
        block->refcount = 1;
    #endif
    return (void *)(block + 1);
}


/* mem_scalar_resize */
void *
mem_scalar_resize(void * object, int newsize)
{
    int sizedifference;
    struct mem_block * block;
    assert( newsize >= 0);
    block = (struct mem_block *)object - 1;
    sizedifference = newsize - block->size;
    block = realloc(block, sizeof(struct mem_block) + newsize);
    assert( block != NULL );
    block->node->block = block;
    block->size = newsize;
    #if defined(REFCOUNT_ONLY)
        block->rootdata->totalbytes += sizedifference;
    #endif
    return (void *)(block + 1);
}


/* mem_scalar_set_ref */
void
mem_scalar_set_ref(void * object, void * target)
{
    struct mem_block * block;
    struct mem_node * node;
    void ** objectp;
    /* Receive parameters and check them for sanity */
    objectp = object;
    block = (struct mem_block *)object - 1;
    assert( block->size >= -1 );
    node = block->node;
    if (block->size >= 0 ) {
        /* If a scalar currently contains a value, replace that with a pointer */
        if (block->size != sizeof(void *)) {
            /* Currently a different number of bytes is allocated */
            node->block = realloc(node->block, sizeof(struct mem_block) + sizeof(void *));
            assert( node->block != NULL );
            block->node = node;
            object = objectp = (void *)(block + 1);
        }
        block->size = -1;
        * objectp = NULL;
    }
    if (target != * objectp) {
        /* update only if something changes */
        if (* objectp) {
            /* unreference the previously referenced object (this may recurse) */
            ;
        }
        /* assign the pointer */
        * objectp = target;
        if (target) {
            /* reference the new target */
            ;
        }
    }
}


/* mem_scalar_get_ref */
void *
mem_scalar_get_ref(void * object)
{
    void ** objectp;
    /* Receive parameters and check them for sanity */
    objectp = object;
    return * objectp;
}


/* mem_list_new */
void *
mem_list_new(void * parent)
{
    struct mem_node * node;
    struct mem_block * block;
    struct mem_listblock * listblock;
    void ** list;
    /* Allocate three structs on the heap */
    node = (struct mem_node *) malloc(sizeof(struct mem_node));
    block = (struct mem_block *) malloc(sizeof(struct mem_block) + sizeof(struct mem_listblock));
    list = calloc(1,sizeof(void *));
    /* Link these structs to each other */
    listblock = (struct mem_listblock *)(block + 1);
    listblock->list = list;
    listblock->listsize = 1;  /* because calloc made one NULL pointer */
    block->node = node;
    node->block = block;
    block->size = -2;  /* "magic" value indicating list */
    block->rootdata = (struct mem_rootdata *) parent;
    block->rootdata->totalobjects++;
    #if defined(REFCOUNT_ONLY)
        block->refcount = 1;
    #endif
    return (void *) listblock;
}


/* mem_list_put */
void
mem_list_put(void * object, int offset, void * target)
{
    struct mem_block * block;
    struct mem_listblock * listblock;
    assert( object != NULL );
    block = (struct mem_block *)object - 1;
    assert( block->size == -2 );
    listblock = (struct mem_listblock *) object;
    if (offset >= listblock->listsize) {
        listblock->list = realloc(listblock->list, (offset+1) * sizeof(void *));
        memset(listblock->list+listblock->listsize,0,(offset+1-listblock->listsize)*sizeof(void *));  /* set all the new pointers to NULL */
        listblock->listsize = offset + 1;
    }
    assert( offset*sizeof(void *) < block->size );
    listblock->list[offset] = target;
}


/* mem_list_get */
void *
mem_list_get(void * object, int offset)
{
    struct mem_block * block;
    struct mem_listblock * listblock;
    /* Convert and sanity check the parameters */
    assert( object != NULL );
    assert( (block=(struct mem_block *)object-1, block->size == -2) );
    assert( offset >= 0 );
    listblock = (struct mem_listblock *) object;
    assert( offset < listblock->listsize );
    /* The actual work is so simple, it could be inlined. */
    return listblock->list[offset];
}


/* mem_refdel */
void
mem_refdel(void * referrer, void * object)
{
    /* TODO: Add this node to a queue for a background thread to process */
    int i;
    struct mem_block * block;
    struct mem_listblock * listblock;
    void * child;
    block = (struct mem_block *)object - 1;
    if (block->size >= -1) { /* scalar value or scalar reference */
        #if defined(REFCOUNT_ONLY)
            block->refcount--;
            if (block->refcount == 0) {
                if (block->size == -1) {
                    child = (void *) * (void **)object;
                    if (child != NULL) {
                        mem_refdel(referrer, child);  /* recurse */
                    }
                }
                block->rootdata->totalbytes -= block->size;
                block->rootdata->totalobjects--;
                free(block->node); free(block);
            }
        #endif
    }
    else { /* list */
        assert( block->size == -2 );  /* "magic" number for a list */
        #if defined(REFCOUNT_ONLY)
            block->refcount--;
            if (block->refcount == 0) {
                /* Un-reference the list's children */
                listblock = (struct mem_listblock *) object;
                for (i=0; i<listblock->listsize; ++i) {
                    child = listblock->list[i];
                    if (child != NULL) {
                        mem_refdel(referrer, child);  /* recurse */
                    }
                }
                /* Free the list's own memory */
                free(listblock->list);
                free(block->node);
                free(block);
            }
        #endif
    }
}


/* mem_objects */
int
mem_objects(void * allocation)
{
    struct mem_block * block;
    struct mem_rootdata * rootdata;
    block = (struct mem_block *)allocation - 1;
    #if defined(REFCOUNT_ONLY)
        rootdata = block->rootdata;
    #else
        struct mem_node * rootnode;
        rootnode = block->rootnode;
    #endif
    return rootdata->totalobjects;
}


/* mem_bytes */
long
mem_bytes(void * allocation)
{
    struct mem_block * block;
    struct mem_rootdata * rootdata;
    block = (struct mem_block *)allocation - 1;
    #if defined(REFCOUNT_ONLY)
        rootdata = block->rootdata;
    #else
        struct mem_node * rootnode;
        rootnode = block->rootnode;
    #endif
    return rootdata->totalbytes;
}


/*

The system manages two kinds of application objects in memory: scalars
and lists.  They are more primitive than Perl scalars and lists, but are
the foundation upon which those, and hashes, will be built.  A scalar
may contain from 0 to MAXINT bytes of opaque data, or a single reference
to one other memory object.  A list contains from 0 to MAXINT references
to other memory objects.  The maxima are actually a tad short of MAXINT,
but let's not spoil a good story with hard facts.

A reference is just a (void *) pointer but there is a fixed size memory
management struct at a fixed offset before the pointed to address.  It
is called mem_block but application code should not access it.  Memory
objects may move if resized (they use realloc()), but tracking via
pointers is easier with stationary data, so each memory object also has
a stationary mem_node struct.  A possible future optimization would be
for the case of fixed size memory objects, to concatenate these structs
in a single stationary memory allocation, but this is a low priority.

Reference counting structures, for example three scalars:

+----------------+           +-------------+           +-------------+
| rootdata       |           | node 1      |           | block 1     |
|   totalobjects |<--------------rootdata  |<--------------node      |
|   totalbytes   |           |   block---------------->|   size      |
|   node next--------------->|   refcount  |           +-------------+
|   node prev    |    NULL<------node prev |<-----+    | object 1    |
+----------------+           |   node next-----+  |    | (user data) |
        ^ ^                  +-------------+   |  |    +-------------+
        | |                                    |  |
        | |                  +-------------+   |  |    +-------------+
        | |                  | node 2 etc  |<--+  |    | block 2     |
        | +----------------------rootdata  |<--------------node      |
        |                    |   block---------------->|   size      |
        |                    |   refcount  |      |    +-------------+
        |                    |   node prev--------+    | object 2    |
        |                    |   node next-----+<-+    | (user data) |
        |                    +-------------+   |  |    +-------------+
        |                                      |  |
        |                    +-------------+   |  |    +-------------+
        |                    | node 3 etc  |<--+  |    | block 3     |
        +------------------------rootdata  |<--------------node      |
                             |   block---------------->|   size      |
                             |   refcount  |      |    +-------------+
                             |   node prev--------+    | object 3    |
                             |   node next---->NULL    | (user data) |
                             +-------------+           +-------------+

An array containing a scalar and an array (prev and next links omitted):

+----------------+           +-------------+           +-------------+
| rootdata       |       +-->| node 2      |           | block 2     |
|   totalobjects |       |   | rootdata    |<--------------node      |
|   totalbytes   |       |   |   block---------------->|   size      |
|   node prev    |       |   |   refcount  |           +-------------+
|   node next--------+   |   |   node prev |           | object 2    |
+----------------+   |   |   |   node next |           | (user data) |
                     |   |   +-------------+           +-------------+
                     |   |
                     |   +---------------------------------------------+
                     |                                                 |
+-------------+      |       +-------------+           +-----------+   |
| node 1      |<---=-+       | block 1     |      +--->| element 0-----+
|   rootdata  |<-----------------node      |      |    +-----------+
|   block------------------->|   size      |      |    | element 1-----+
|   refcount  |              +-------------+      |    +-----------+   |
|   node prev |              | listblock 1 |      |    | element 2 |   |
|   node next |              |   listsize  |      |    +-----------+   |
+-------------+              |   list-------------+    |    etc    |   |
                             +-------------+           +-----------+   |
                                                                       |
                     +-------------------------------------------------+
                     |
+-------------+      |       +-------------+           +-----------+
| node 3      |<-----+       | block 3     |      +--->| element 0 |
|   rootdata  |<-----------------node      |      |    +-----------+
|   block------------------->|   size      |      |    | element 1 |
|   refcount  |              +-------------+      |    +-----------+
|   node prev |              | listblock 3 |      |    |    etc    |
|   node next |              |   listsize  |      |    +-----------+
+-------------+              |   list-------------+
                             +-------------+

If an object contains a reference, the user data is a pointer to the
node struct of (usually another) object, although the user code would
have requested it by passing a pointer to the user data object.

The automatic memory management works by tracking references, each from
a parent node to a child node.  A parent can have any number of children
and a child has one or more parents.  A recycler frees orphaned children
and orphaned cycles of children.  Background threads collect garbage
incrementally.

The API has function names like mem_malloc() with referrer
and referent arguments where needed.  Like other heap managers, the API
has no mem_free().  The mem_refdel() function deletes a
reference and leaves the reachability checking to a background thread.

References to nodes are tracked in ordered single linked lists, not
counted.  The order is by minimum hop count from a root node, to detect
and correctly handle reference cycles.  Every node except the root must
have at least one reference from another node.  Application code can
create or delete a reference between any pair of referrer and
referent nodes.  



A node contains either opaque scalar data (eg numeric, string, function
pointer) or a list, which is a resizeable array of references to other
nodes.  More complex structures such as objects with named members will
have to be built on top of these.  The linked lists live in a node
header structure, so the space overhead is only in node size, costing no
extra items on the heap.

References connect nodes in a hierarchy from a root, a bit like inodes
in a Unix file system.  Each node is referenced by one or more other
nodes and contains typed data and a variable size list of references to
other nodes.  This is the node header layout:

Name       Type     Description
----       ----     -----------
referrers  pointer  start of a linked list of other nodes that point to
           to node  this one.  Only the root node has a null here.
ref_next   pointer  next referrer node that points to the same referent
           to node  (node being pointed to).
destructor pointer  function to free memory related to this node.
           to function
level      int      number of links in shortest path from root to here.
datatype   int      application defined data description (enum)
datasize   int      number of bytes excluding this header
[data]     void     not a struct member, just the opaque contents

In addition to being a general purpose heap manager, this code also aims
to support 6model/c, an experimental C version of 6model, a platform for
dynamic languages such as Perl 6.  To do so requires hashes as well as
the already provided scalars and lists.  By combining with a hashtable
library, a 6model/c hash is a list with the data in entry 0 pointing to
a hashtable whose keys are any sequences of bytes and whose values are
list subscripts greater than 0.

This design keeps the mem code isolated from the hashtable code,
but requires hash nodes to point to a hashtable destructor function.

See also:

Connectivity based garbage collection (simulation, Java)
http://www-plan.cs.colorado.edu/diwan/cbgc.pdf

Ulterior Reference Counting: Fast Garbage Collection without a Long Wait
http://cs.anu.edu.au/~Steve.Blackburn/pubs/papers/urc-oopsla-2003.pdf

Beltway: Getting Around Garbage Collection Gridlock
ftp://ftp.cs.umass.edu/pub/osl/papers/pldi2002.ps.gz

The Treadmill: Real-Time Garbage Collection Without Motion Sickness
http://home.pipeline.com/~hbaker1/NoMotionGC.html

The Garbage Collection Handbook The art of automatic memory management
by Richard Jones, Antony Hosking, Eliot Moss 1420082795 978-1420082791
http://www.gchandbook.org/contents.html

Garbage Collection Page (by Richard Jones) - excellent resources
http://www.cs.kent.ac.uk/people/staff/rej/gc.html

Concurrent Cycle Collection in Reference Counted Systems
(by David F. Bacon and V.T. Rajan) - promising but hard to implement.
http://www.research.ibm.com/people/d/dfb/papers/Bacon01Concurrent.pdf
*/

/* end of mem.c */
