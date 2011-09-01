/* mem.h */

/* For initial testing while the harder bits are still incomplete, */
/* uncomment the following #define.  It makes the code the simplest */
/* thing that could possibly work, but it leaks reference cycles. */

#define REFCOUNT_ONLY 1 /* Warning - commenting out now causes errors */


/* mem_block */
struct mem_block {
    /* The size field has special meanings for certain values: */
    /* 0 to INT_MAX: size in bytes, any scalar opaque data (like malloc) */
    /* -1: scalar pointer (reference) to another object */
    /* -2: list of pointers (references) to other objects */
    int size;
    struct mem_node * node;
    #if defined(REFCOUNT_ONLY)
        struct mem_rootdata * rootdata;
        int refcount;
    #else
        struct mem_node * rootnode;
        struct mem_node * referrers;
        struct mem_node * ref_next;
        int level;    /* number of hops from root */
    #endif
};


/* mem_node */
/* Connectivity information about each memory object. */
struct mem_node {
    struct mem_block * block;
    struct mem_node * rootnode;
    struct mem_node * referrers;
    struct mem_node * ref_next;
    #if ! defined(REFCOUNT_ONLY)
        int level;    /* number of hops from root */
    #endif
};


/* mem_listblock */
/* Stationary base for list contents */
struct mem_listblock {
    int listsize;
    void ** list;
};


/* mem_rootdata */
struct mem_rootdata {
    #if defined(REFCOUNT_ONLY)
        struct mem_block * blocknext;
        struct mem_block * blockprev;
    #else
        struct mem_node * first;
        struct mem_node * last;
    #endif
    int  totalobjects;
    long totalbytes;
};


/* Function declarations (same whether REFCOUNT_ONLY defined or not) */
void mem_refdel(void * referrer, void * allocation);

void * mem_init();
void   mem_final(void * root, int freeflag);
void * mem_scalar_new(void * parent, int size);
void * mem_scalar_resize(void * object, int newsize);
void   mem_scalar_set_ref(void * object, void * target);
void * mem_scalar_get_ref(void * object);
void * mem_list_new(void * parent);
void   mem_list_put(void * list, int item, void * target);
void * mem_list_get(void * list, int item);
int    mem_size(void * object);
long   mem_bytes(void * root);
int    mem_objects(void * root);

/* end of mem.h */
