/* hashtable.c */

/* This file defines a general purpose set of hash table routines. */
/* Hash tables are a form of associative array used by 6model/c both */
/* at compile time in the parser, and often at run time by compiled */
/* Perl 6 programs. */

/* The hash table contains a series of key and value pairs.  A key */
/* must be unique and may not be null.  The value may be duplicated */
/* or null.  To be general purpose, each key and value is handled as */
/* an opaque variable size array of bytes.  The hash table contains */
/* only pointers to the keys and values, not their contents, so if */
/* those are moved in memory their entries in the hash table must be */
/* updated.  The struct hashtable_entry defines each key and value */
/* pair, and struct hashtable has an entrylist that points to the */
/* variable size array of all of them. */

/* Hashing is the algorithm used to quickly find an entry given its */
/* key.  A hashing function reads the key bytes and computes a hash */
/* code, which selects a hash bucket.  The struct hashtable points to */
/* hash bucket lists stored separately from the entrylist, so that */
/* they can be resized and/or rebuilt by separate threads.  Hash */
/* bucket lists are variable size arrays of pointers to, and sizes */
/* of, hash buckets.  Each hash bucket is a variable size list of */
/* indices of members of the entrylist whose keys hash to the same */
/* bucket number.  Ideally each bucket would contain only one */
/* pointer, and the number of buckets would equal the number of keys. */
/* In practice some buckets are empty and others contain several */
/* pointers to hash table entries. */

/* hash --> struct hash  +--------------->+--------------->key0----->
              |          |                |                value0--->
            entrylist ---+  +--->entry0---|---+  +-------->key1----->
            seed            |    entry1---|---|--|--+      value1--->
            bucketlist---+  |    entry2---+   |  |  |  +-->key2----->
        +----------------+  |                 |  |  |  |   value2--->
        |                   |             +---|--|--|--|-->key3----->
        +--->bucket0--------+             |   |  |  |  |   value3--->
             bucket1-size-list-->entry0---|---|--+  +--|-->key4----->
             bucket2---x                  |   |        |   value4--->
             bucket3-size-list-->entry0---+   +--------|-->key5----->
               etc               entry1----------------+   value5--->
                                                             etc

/* The hashing algorithm is like the PERL_HASH() macro in Perl 5, see */
/* 'perldoc perlguts'.  The number of hash buckets is 2 raised to an */
/* integral power so that the modulo arithmetic that converts a hash */
/* code to a bucket number simplifies to a bit mask (bitwise and) */
/* operation instead of division.  The hashing algorthm has one */
/* change from Perl 5, an initial hashcode salt instead of a 0 to stave */
/* off potential denial of service attacks.  The salt is a random */
/* integer that is replaced when a re-hash adds new buckets (see the */
/* next point). */

/* The ratio of keys to buckets is called the load factor.  During a */
/* store operation, if the load factor rises above a hard coded limit */
/* (eg 0.75 or 1.25) the software builds a new hash index with twice */
/* the number of buckets, and then re-hashes every key to put it into */
/* the right bucket.  After the new index is complete, it replaces */
/* the old one which then gets freed.  An advisory lock prevents other */
/* threads changing keys during the re-hash process.  Changing values */
/* of existing keys is fine during re-hashing. */

/* The re-hashing is amortized over subsequent store operations to */
/* avoid a stop-the-world type reorganization.  Instead, there are */
/* two bucket lists, the old and the new. */
/* If a new list is already being created, it must be finished before */
/* another one new one may be started. */

/* During a delete operation, if the load factor reduces below some */
/* other hard coded limit (eg 0.3 or 0.5) the software will halve the */
/* number of buckets.  There is no need to re-hash the keys, each */
/* pair of successive buckets can simply be merged, again under the */
/* protection of an advisory lock and possibly in a separate thread. */

/* To avoid another stop-the-world type operation, the bucket merging */
/* is also done incrementally, with an old and a new bucket list. */
/* If a new list is already being created, it must be finished before */
/* another one may be started. */

/* The payload of a hash table, the values, can be whatever you want. */
/* Remember that a key and a value are each expressed as an address */
/* and a "length".  Only the application accesses the value.  It is */
/* therefore also fine if the "length" is put to some other use of an */
/* int, for example an enum identifying a type.  In such cases, the */
/* application should manage the size of the data at the address, for */
/* example with structs. */

/* When a key-value pair is deleted from the entrylist, it becomes a */
/* hole.  To save time, remaining entries are not moved.  All the */
/* holes are connected in a double linked list ordered from lowest to */
/* highest address.  Thus subsequent new insertions re-use the lowest */
/* available address.  In a hole, the "key" address and size fields */
/* are null.  The "value" address field is the forward link and the */
/* "value" integer field is the backward link. */

#include <assert.h>   /* assert */
#include <stdlib.h>   /* NULL rand srand */
#include <stdio.h>    /* printf */
#include <string.h>   /* memmove */
#ifdef _MSC_VER
    #include <time.h> /* gettimeofday */
	#include <winsock2.h>  /* struct timeval */
#else
    #include <sys/time.h> /* gettimeofday */
#endif
#include "hashtable.h" /* hashtable */

#ifdef __cplusplus
extern "C" {
#endif


/* hashtable_new */
struct hashtable *
hashtable_new()
{
    struct hashtable * hash;
    struct timeval tv;  /* microsecond clock to seed random number */
    /* fprintf(stderr,"enter hashtable_new\n"); */
	/* Seed the random number generator with values from the system time */
	#ifndef _MSC_VER
        assert( gettimeofday(&tv,NULL)==0 ); /* read clock */
        srand(tv.tv_sec ^ tv.tv_usec);
	#endif
    /* Initialize the hash */
    hash = (struct hashtable *) malloc(sizeof(struct hashtable));
    assert( hash != NULL );
    hash->loadfactorlow  = 0.3;
    hash->loadfactorhigh = 1.25;
    hash->salt = rand();
    hash->entrycount = 0;
    /* Initialize the hashtable entry list with one deleted entry (a hole) */
    /* to simplify the code required in hashtable_store(), which */
    /* is called much more frequently. */
    hash->entrylistsize = 1;
    hash->entrylist = (struct hashtable_entry *) calloc(hash->entrylistsize, sizeof(struct hashtable_entry));
    assert( hash->entrylist != NULL );
    hash->entrylist->keyint = -1;
    hash->entrylist->valueint = -1;
    hash->entrylist->valuepointer = (void *) -1;
        /* terminate the back linked list */
        /* The zeroes that calloc() put in there are correct for most */
        /* fields, but a zero in the backward linked list would be a */
        /* valid subscript for another hole, causing in some infinite */
        /* loops. */
    hash->deletedentryhead = 0;
    hash->deletedentrytail = 0;
    /* Initialize the first bucket list with a single empty bucket */
    hash->bucketmask1 = 0; /* (number of buckets) - 1 */
    hash->bucketlist1 = (struct hashtable_bucket *) malloc(sizeof(struct hashtable_bucket));
    assert( hash->bucketlist1 != NULL );
    hash->bucketlist1->size = 0;
    hash->bucketlist1->list = NULL;
    /* Initially there is no second bucket list */
    hash->bucketlist2= NULL;
    hash->bucketmask2= 0;
    return hash;
}


/* hashtable_store */
int
hashtable_store(struct hashtable * hash, void * keypointer,
    int keylength, void * valuepointer, int valueint)
{
    /* A store operation either inserts a new key and value or keeps */
    /* the existing key and overwrites the existing value. */
    struct hashtable_bucket * bucket;
    struct hashtable_entry  * entry, * entrylist = hash->entrylist;
    char * hashingpointer;
    int hashinglength, hashcode, i, entryindex, * bucketentry;
    /* fprintf(stderr,"store '%*s' => '%*s'\n", -keylength, (char *)keypointer, -valueint, (char*)valuepointer); */
    /* Search for the entry by hashing the key and scanning a bucket */
    hashcode = hash->salt;
    hashinglength  = keylength;
    hashingpointer = (char *) keypointer;
    while (hashinglength--)
        hashcode = (hashcode * 33) + * hashingpointer ++;
    hashcode += hashcode >> 5;
    bucket = hash->bucketlist1 + (hashcode & hash->bucketmask1);
    for (bucketentry = bucket->list, entryindex = 0, i=bucket->size; --i >= 0; ++entryindex) {
        entry = entrylist + * bucketentry++;
        /* fprintf(stderr,"?'%*s'? ", -entry->keyint, (char *) entry->keypointer); */
        if (entry->keyint==keylength && (memcmp(
            entry->keypointer,keypointer,keylength)==0)) {
            i = -1; /* becomes -2, terminates bucket scan with found */
        }
    }
    /* If the key is not found via the first bucket list, it may be */
    /* because it should be found via the second bucket list that is */
    /* being constructed. */
    if ((i==-1) && hash->bucketlist2) {
        bucket = hash->bucketlist2 + (hashcode & hash->bucketmask2);
        for (bucketentry = bucket->list, entryindex = 0, i=bucket->size; --i >= 0; ++entryindex) {
            /* fprintf(stderr,"?'%*s'? ", -entry->keyint, (char *) entry->keypointer); */
            if (entry->keyint==keylength && (memcmp(
                entry->keypointer,keypointer,keylength)==0)) {
                i = -1; /* becomes -2, terminates bucket scan with found */
            }
        }
    }
    /* fprintf(stderr,"scan result %d, %s key '%*s'. ", i, i == -1 ? "new" : "existing", -keylength, (char *) keypointer ); */
    /* After the search i==-1 means not found and i==-2 means found. */
    switch (i) {
        case -1:
            /* Add a new entry with key and value. */
            ++ (hash->entrycount);
            /* If there are deleted entries, recycle the first one */
            if (hash->deletedentryhead >= 0) {
                /* There is a linked list of deleted entries */
                entry = hash->entrylist + hash->deletedentryhead;
                assert( entry->valueint == -1 );
                /* detach the first empty entry from the empties list */
                if ((hash->deletedentryhead=(long)entry->valuepointer)>=0) {
                    /* there is are more empties in the list */
                    hash->entrylist[(long)entry->valuepointer].valueint = -1;
                    /* fprintf(stderr,"more empty entries\n"); */
                }
                else {
                    /* entry was the only one in the list */
                    hash->deletedentryhead = -1;
                    hash->deletedentrytail = -1;
                    /* fprintf(stderr,"last empty entry\n"); */
                }
            }
            else {
                /* There is no empty entry, extend the entry list. */
                /* Add the required entry plus one empty entry. */
                /* fprintf(stderr,"extend entry list to %d\n", (hash->entrylistsize)+1); */
                hash->entrylistsize += 1;
                hash->entrylist = (struct hashtable_entry *)
                    realloc(hash->entrylist, hash->entrylistsize *
                        sizeof(struct hashtable_entry));
                assert( hash->entrylist != NULL );
                /* initialize the empty entry */
//              hash->deletedentryhead = hash->entrylistsize - 1;
//              hash->deletedentrytail = hash->entrylistsize - 1;
                entry = hash->entrylist + hash->entrylistsize - 1;
                entry->keyint = 0;
                entry->valueint = -1;
                entry->keypointer = NULL;
                entry->valuepointer = (void *) -1;
                /* point to the new entry to be used */
                entry = hash->entrylist + hash->entrylistsize - 1;
            }
            /* TODO: consider doubling the number of buckets */
            /* put the key into the entry */
            entry->keypointer = keypointer;
            entry->keyint     = keylength;
            /* Insert this new hashtable list entry into the front of this bucket */
//          ++ (bucket->size);
            bucket->list = (int *) realloc( bucket->list, (++bucket->size) * sizeof(int) );
            assert( bucket->list != NULL );
//          fprintf(stderr, "bucket size %d at %x\n", bucket->size, bucket->list);
            memmove(bucket->list+1, bucket->list, (bucket->size-1)*sizeof(int));
            * bucket->list = entryindex;
            /* no break: fall through to the update case */
        case -2:
            /* update the existing entry in place with a new value */
            entry->valuepointer = valuepointer;
            entry->valueint     = valueint;
            break;
        default:
            fprintf(stderr,"hashtable_store internal error\n");
            exit(1);
            break;
    }
    return hashcode;
}


/* hashtable_fetch */
int
hashtable_fetch(struct hashtable * hash, void * keypointer,
    int keylength, void ** valuepointerpointer, int * valueintpointer)
{
    struct hashtable_bucket * bucket;
    struct hashtable_entry ** listitem;
    struct hashtable_entry  * entry, * entrylist = hash->entrylist;
    char * hashingpointer;
    int hashinglength, hashcode, i, entryindex, * bucketentry;
    /* fprintf(stderr,"fetch '%*s'\n", -keylength, (char *) keypointer); */
    /* Search for the entry by hashing the key and scanning a bucket */
    hashcode = hash->salt;
    hashinglength  = keylength;
    hashingpointer = (char *) keypointer;
    while (hashinglength--)
        hashcode = (hashcode * 33) + * hashingpointer ++;
    hashcode += hashcode >> 5;
    bucket = hash->bucketlist1 + (hashcode & hash->bucketmask1);
    for (bucketentry = bucket->list, entryindex = 0, i=bucket->size; --i >= 0; ++entryindex) {
        entry = entrylist + * bucketentry++;
        /* fprintf(stderr,"?'%*s'? ", -entry->keyint, (char *) entry->keypointer); */
        if (entry->keyint==keylength && (memcmp(
            entry->keypointer,keypointer,keylength)==0)) {
            i = -1; /* becomes -2, terminates bucket scan with found */
        }
    }
    /* If the key is not found via the first bucket list, it may be */
    /* because it should be found via the second bucket list that is */
    /* being constructed. */
    if ((i==-1) && hash->bucketlist2) {
        bucket = hash->bucketlist2 + (hashcode & hash->bucketmask2);
        for (bucketentry = bucket->list, entryindex = 0, i=bucket->size; --i >= 0; ++entryindex) {
            /* fprintf(stderr,"?'%*s'? ", -entry->keyint, (char *) entry->keypointer); */
            if (entry->keyint==keylength && (memcmp(
                entry->keypointer,keypointer,keylength)==0)) {
                i = -1; /* becomes -2, terminates bucket scan with found */
            }
        }
    }
    /* fprintf(stderr,"scan result %d, %s key '%*s'. ", i, i == -1 ? "new" : "existing", -keylength, (char *) keypointer ); */
    /* After the search i==-1 means not found and i==-2 means found. */
    switch (i) {
        case -1:
            * valuepointerpointer = NULL;
            * valueintpointer = 0;
            i = 0;
            break;
        case -2:
            * valuepointerpointer = entry->valuepointer;
            * valueintpointer     = entry->valueint;
            i = 1;
            break;
        default:
            fprintf(stderr,"hashtable_fetch internal error\n");
            exit(2);
            break;
    }
    return i;
}


/* hashtable_delete */
int
hashtable_delete(struct hashtable * hash, void * keypointer,
    int keylength)
{
    /* A delete operation empties a hashtable entry, links it into the */
    /* empty entry list, and deletes the pointer to the entry from */
    /* its bucket. */
    struct hashtable_bucket * bucket;
    struct hashtable_entry ** listitem, * entry, * entrylist = hash->entrylist;
    char * hashingpointer;
    int hashinglength, hashcode, i, entryindex, * bucketentry;
    /* Search for the entry by hashing the key and scanning a bucket */
    hashcode = hash->salt;
    hashinglength  = keylength;
    hashingpointer = (char *) keypointer;
    while (hashinglength--)
        hashcode = (hashcode * 33) + * hashingpointer ++;
    hashcode += hashcode >> 5;
    bucket = hash->bucketlist1 + (hashcode & hash->bucketmask1);
    for (bucketentry = bucket->list, entryindex = 0, i=bucket->size; --i >= 0; ++entryindex) {
        entry = entrylist + * bucketentry++;
        /* fprintf(stderr,"?'%*s'? ", -entry->keyint, (char *) entry->keypointer); */
        if (entry->keyint==keylength && (memcmp(
            entry->keypointer,keypointer,keylength)==0)) {
            i = -1; /* becomes -2, terminates bucket scan with found */
        }
    }
    /* If the key is not found via the first bucket list, it may be */
    /* because it should be found via the second bucket list that is */
    /* being constructed. */
    if ((i==-1) && hash->bucketlist2) {
        bucket = hash->bucketlist2 + (hashcode & hash->bucketmask2);
        for (bucketentry = bucket->list, entryindex = 0, i=bucket->size; --i >= 0; ++entryindex) {
            /* fprintf(stderr,"?'%*s'? ", -entry->keyint, (char *) entry->keypointer); */
            if (entry->keyint==keylength && (memcmp(
                entry->keypointer,keypointer,keylength)==0)) {
                i = -1; /* becomes -2, terminates bucket scan with found */
            }
        }
    }
    /* fprintf(stderr,"scan result %d, %s key '%*s'. ", i, i == -1 ? "new" : "existing", -keylength, (char *) keypointer ); */
    /* After the search i==-1 means not found and i==-2 means found. */
    switch (i) {
        case -1:
            break;
        case -2:
            /* TODO: consider halving the number of buckets */
            break;
        default:
            fprintf(stderr,"hashtable_delete internal error\n");
            exit(3);
            break;
    }
    return hashcode;
}


/* hashtable_free */
void
hashtable_free(struct hashtable * hash)
{
    /* What this routine cannot do is free the memory used by the */
    /* values in the hashtable.  The calling code that knows more about */
    /* the structure of the values should do that beforehand, or */
    /* (worse) leave the litter scattered around the heap for some */
    /* unfortunate garbage collector to pick up (the usual managed */
    /* memory cop-out). */
    int i;
    struct hashtable_bucket ** bucketpointerpointer;
    struct hashtable_bucket  * bucketpointer;
    bucketpointer = hash->bucketlist1;
    for (i=0; i<=hash->bucketmask1; ++i)
        free(bucketpointer->list);
    free(hash->entrylist);
    free(hash->bucketlist1);
    free(hash);
}


/* hashtable_iterator_next */
int
hashtable_iterator_next(struct hashtable_iterator *
    iter, struct hashtable_entry * entry)
{
    struct hashtable_entry * list;
    int i, size, status;
    /* first check whether the iterator is still active */
    if ((i=iter->nextentryindex) >= 0) {
        list = iter->hashtable->entrylist;
        size = iter->hashtable->entrylistsize;
        /* skip any deleted entries */
        while (i < size-1 && list[i].keypointer == NULL) {
            ++i;
        }
        if (i <= size-1 && list[i].keypointer != NULL) {
            entry->keypointer   = list[i].keypointer;
            entry->valuepointer = list[i].valuepointer;
            entry->keyint       = list[i].keyint;
            entry->valueint     = list[i].valueint;
            iter->nextentryindex = i+1;
            status = 1;
        }
        else {
            entry->keypointer = entry->valuepointer = NULL;
            entry->keyint     = entry->valueint     = 0;
            iter->nextentryindex = -1;
            status = 0;
        }
    }
    else {
        entry->keypointer = entry->valuepointer = NULL;
        entry->keyint     = entry->valueint     = 0;
        status = 0;
    }
    return status;
}


/* hashtable_iterator_init */
void
hashtable_iterator_init(struct hashtable * hash,
    struct hashtable_iterator * iter)
{
    iter->hashtable      = hash;
    iter->nextentryindex = 0;
}


#ifdef __cplusplus
}
#endif

/* TODO:
LHF: Track the total number of keys, total number of bytes in all the
keys, total number of bytes in all the values.  The value total might be
meaningless because the int value might be being used as a flag instead
of a size.  These totals should have negligible overhead.  Prove it by
making a #define that optionally includes or excludes almost all the
code.  It should also be possible to verify the totals in an assert() as
part of the re-hash that occurs when the number of buckets doubles.

MHF: If it can be done in O(1) time, track the highest number of entries
in any bucket (the problem is what to do after a delete without
resorting to an O(bucketmask) scan).  A (single/double) linked list
ordering the buckets by size may work.  The information is not
necessary, only interesting for the curious, so the CPU overhead must
be negligible.  Prove it by making the code #define optional.

MHF: make variable the threshold load factors that trigger doubling and
halving of the number of buckets.

HHF: Try converting hashtable->entrylist into a list of lists, in order to
reduce the size of the memory blocks that need to be copied during the
realloc() that sometimes occurs when adding a new key.  Again,
negligible added CPU time please, although there is a bit more tolerance
in this change because it should speed up some inserts a little when
hashes contain many (perhaps over 1000) entries.  Prove the difference
if possible with a #define.

MHF: Currently the entries in a bucket are ordered from most recently
used (MRU) to least recently used (LRU).  Every access can update the
order, and searching is sequential, favouring short lists.  As an
alternative try keeping the entries in key order and search with
bsearch() instead.  The would update the order only when keys are added
or deleted, and would favour longer lists.  Would this perform better or
worse with typical Perl 6 scripts?  Use #define again to make this
change optional to find out.  With larger bucket sizes this would behave
like a hash of sorted arrays instead of a simple hash table.
*/

/* See also:
Hash table overview http://en.wikipedia.org/wiki/Hash_table
Perl 5 hash source http://cpansearch.perl.org/src/RJBS/perl-5.12.3/hv.c
*/

/* end of hashtable.c */
