/* This is how an object that uses the KnowHOWREPR is laid out. */
typedef struct {
    /* Obligatory S-Table pointer. */
    PMC *STable;
    
    /* List of meta-attribute objects. */
    PMC *Attributes;
    
    /* Table of method objects. */
    PMC *Methods;
} KnowHOWInstance;

/* Way to get at the data. */
#define KnowHOWInstanceData(o) ((KnowHOWInstance)PMC_data(o))

/* Sets up the KnowHOW representation and returns an instance
 * of it to work with. */
PMC * REPR_KnowHOW_setup(PARROT_INTERP);
