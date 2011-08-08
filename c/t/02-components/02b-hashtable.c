/* 02b-hashtable.c */
/* Create several hashes and store, fetch and delete a large number */
/* of random data values.  Count the number of such random operations */
/* (not exactly repeatable) that can be performed in a 10 second */
/* interval.  Free everything so that Valgrind can verify that there */
/* are no memory leaks. */

#include <assert.h>   /* assert */
#include <stdio.h>    /* printf */
#include <stdlib.h>   /* malloc */
#include <string.h>   /* memmove strlen */
#include "../../src/hashtable.h"
#include "../Test.h"  /* diag is_ii plan */

/* The number of allocations is O((STRINGCOUNT ** 2) * MAXKEYLENGTH), */
/* so be careful when increasing it.  5000 strings use 250MiB. */
#define STRINGCOUNT  5000 /* number of strings to store into hash table */
#define MAXKEYLENGTH   40 /* maximum number of characters per key */

#ifdef __cplusplus
extern "C" {
#endif

/* hashtable_dump */
void
hashtable_dump(struct hashtable * hashtable)
{
    int i, j;
    struct hashtable_entry  * entry;
    struct hashtable_bucket * bucket;
    fprintf(stderr,"{hashtable at %lx size %d at %lx %d keys\n",
        (long) hashtable, hashtable->entrylistsize,
        (long) hashtable->entrylist, hashtable->entrycount
    );
    for (i=0; i<hashtable->entrylistsize; ++i) {
        entry = hashtable->entrylist + i;
        if (entry->keyint) {
            fprintf(stderr," entry %d at %lx '%*s' => '%*s'\n",
                i, (long) entry,
                -entry->keyint,   (char *) entry->keypointer,
                -entry->valueint, (char *) entry->valuepointer
            );
        }
        else {
            fprintf(stderr," entry %d at %lx empty forward %lx back %d\n",
                i, (long) entry,
                (long)(entry->valuepointer), entry->valueint
            );
            assert( entry->keypointer == NULL );
        }
    }
    fprintf(stderr," %d bucket%s at %lx\n", hashtable->bucketmask1 + 1,
        (hashtable->bucketmask1 == 0) ? "" : "s", (long) hashtable->bucketlist1
    );
    for (i=0; i <= hashtable->bucketmask1; ++i ) {
        bucket = hashtable->bucketlist1;
        if (bucket->size) {
            fprintf(stderr," bucket %d %d at %lx", i,
                bucket->size, (long) bucket->list
            );
            for (j=0; j < bucket->size; ++j) {
                entry = hashtable->entrylist + bucket->list[j];
                fprintf(stderr," %*s", -entry->keyint, (char *) entry->keypointer);
            }
            fprintf(stderr,"\n");
        }
        else {
            fprintf(stderr," bucket %d empty\n", i );
            assert( bucket->list == NULL );
        }
    }
    fprintf(stderr,"}\n", i );
}

char *
random_string(int maxlength)
{
    int i;
    int length = (rand() % maxlength) + 1;
    char * s = (char *) malloc(length+1);
    for (i=0; i<length; ++i) {
        s[i] = (rand() % 26) + 'a';
    }
    s[length] = '\0';
    return s;
}


/* main */
int main(int argc, char *argv[])
{
    struct hashtable           * hashtable;
    struct hashtable_iterator    iter;
    struct hashtable_entry       entry;
    void * valuepointer;
    int valueint, seed = 0, stringcount = 0, stringlength, key_bytes = 0,
        value_bytes = 0, entrynumber, collisions = 0, delete_count;
    char * source, * destination, * value;

    diag("02b-hashtable");
    plan(4);
    hashtable = hashtable_new();
    srand(seed);  /* TODO: get a portable seed from for example current time */
    while (stringcount<STRINGCOUNT) { /* nondeterministic because of collisions */
        char * key = random_string(MAXKEYLENGTH);
        /* create a value consisting of the key reversed followed by */
        /* the original key, for example 'abc' -> 'cbaabc' */
        stringlength = strlen(key);
        value = (char *) malloc(2 * stringlength + 1);
        destination=value+stringlength;
        * destination -- = '\0';
        for (source=key; stringlength-->0; ) {
            * destination -- = * source ++;
        }
        strcat( value, key );
        /* test whether the key is already in the hashtable */
        if ( hashtable_fetch(hashtable, key, strlen(key), & valuepointer, & valueint) ) {
            /* it is already in the hash table, free these values */
            free(key);
            free(value);
            ++ collisions;
        }
        else {
            /* it is not already in the hash table, add it */
            hashtable_store(hashtable, key, strlen(key), value, strlen(value));
            key_bytes   += strlen(key);
            value_bytes += strlen(value);
            ++ stringcount;
        }
    }
    is_ii( stringcount, STRINGCOUNT, "created a hash with 5000 entries");
    srand(seed);

    /* Test 2 - iterate the entries and delete them */
    hashtable_iterator_init(hashtable, & iter);
    delete_count = 0;
    while (hashtable_iterator_next(& iter, & entry)) {
        key_bytes   -= strlen(entry.keypointer);
        value_bytes -= strlen(entry.valuepointer);
        /* fprintf(stderr,"iter A '%s' => '%s'\n", (char *) entry.keypointer, (char *) entry.valuepointer); */
        free(entry.keypointer);
        free(entry.valuepointer);
        ++delete_count;
    }
    is_ii(delete_count, stringcount, "iterate 5000 entries and delete");

    /* Test 3 - verify total number of bytes in keys */
    is_ii(key_bytes, 0, "all bytes in keys reclaimed");

    /* Test 4 - verify total number of bytes in keys */
    is_ii(value_bytes, 0, "all bytes in values reclaimed");

    /* Cannot test this internally, but Valgrind should show that no */
    /* bytes remain allocated on the heap. */
    hashtable_free(hashtable);
    return 0;
}

#ifdef __cplusplus
}
#endif

/* end of 02b-hashtable.c */
