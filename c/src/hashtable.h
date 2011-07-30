/* hashtable.h */

/* Structures */

/* hashtable_entry */
struct hashtable_entry {
    /* A hash entry is a key-value pair.  The key and the value can */
    /* each be an arbitrary array of bytes. */
    /* This is a frequently accessed data structure.  For address */
    /* alignment and efficient memory access all the pointers are */
    /* defined at the beginning and the other members ordered by */
    /* decreasing size and frquency of use after that.  Some C */
    /* compilers may do that anyway, but maintaining the order in the */
    /* source code is only a small effort. */
    void * keypointer;
    void * valuepointer;
    int    keyint;
    int    valueint;
    /* A hash entry can also be empty, for example after a delete. */
    /* The empty entries are arranged in a linked list ordered by */
    /* position in the hash entry table, so that subsequent re-use */
    /* occurs in the entry nearest the start of the table.  The empty */
    /* entry list is doubly linked so that the average addition needs */
    /* to walk only a quarter of the list (single linked would have */
    /* needed to walk half the list on average). */
    /* In an empty entry, keypointer == NULL, keyint == 0, */
    /* (int)valuepointer == forward link subscript (-1 ends), */
    /* valueint == backward link subscript (-1 ends). */
};

/* hashtable_bucket */
struct hashtable_bucket {
    int * list; /* list of indices into hash entry list */
    int   size;
};

/* hashtable */
struct hashtable {
    /* This is a frequently accessed data structure.  For best memory */
    /* alignment, all the pointer fields are at the beginning and the */
    /* other members ordered by decreasing size and popularity after */
    /* that.  Some compilers may do that anyway, but manual ordering */
    /* is only a slight inconvenience. */
    struct hashtable_entry  * entrylist;
    struct hashtable_bucket * bucketlist1;
    struct hashtable_bucket * bucketlist2;
    float loadfactorlow;  /* threshold to halve the number of buckets */
    float loadfactorhigh; /* threshold to double number of buckets */
    int salt;             /* random seed for hashing function */
    int entrylistsize;    /* total including deleted entries */
    int entrycount;       /* number of actual (not deleted) entries */
    int deletedentryhead; /* head of linked list of deleted entries */
    int deletedentrytail; /* tail of linked list of deleted entries */
    int bucketmask1;      /* eg 0x3f when there are 64 buckets */
    int bucketmask2;      /* eg 0x3f when there are 64 buckets */
    int emptybuckets;     /* to decide when to shorten bucket list */
};

/* hashtable_iterator */
struct hashtable_iterator {
    struct hashtable * hashtable;
    int    nextentryindex;  /* >=0 when iterating, -1 when done */
};

/* Function declarations */
struct hashtable * hashtable_new();
int  hashtable_store(struct hashtable * hash, void * keypointer, int keylength, void * valuepointer, int valueint);
int  hashtable_fetch(struct hashtable * hash, void * keypointer, int keylength, void ** valuepointerpointer, int * valueintpointer);
int  hashtable_delete(struct hashtable * hash, void * keypointer, int keylength);
void hashtable_free(struct hashtable * hash);
void hashtable_iterator_init(struct hashtable * hash, struct hashtable_iterator * iter);
int  hashtable_iterator_next(struct hashtable_iterator * iter, struct hashtable_entry * entry);

/* end of hashtable.h */
