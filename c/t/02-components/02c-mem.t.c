/* 02c-mem.t.c */
/* Test memory management, check for memory leaks in blocks and lists */

#include <stdio.h>   /* printf */
#include <string.h>  /* strcpy strlen */
#include "../../src/mem.h"
#include "../Test.h"  /* is_ii i_ll ok plan */

#define TESTOBJECTCOUNT 10000
#define LINEBUFFERSIZE 128


/* test1_7scalars */
/* Creates many strings, resizes them all, then deletes them. */
void
test1_7scalars()
{
    int i, totalblocks = 0;
    long totalbytes = 0L;
    void *  rootblock;
    void ** testobjects;
    char line[LINEBUFFERSIZE];

    rootblock = mem_init();
    ok(rootblock!=NULL, "create root node");  /* test 1 */

    testobjects = mem_scalar_new(rootblock, TESTOBJECTCOUNT * sizeof(void *));
    totalbytes += TESTOBJECTCOUNT * sizeof(void *);
    totalblocks++;
    sprintf(line, "allocated array for %d test scalars", TESTOBJECTCOUNT);
    ok(testobjects!=NULL, line);  /* test 2 */

    /* Create TESTOBJECTCOUNT objects on the heap */
    for (i=0; i<TESTOBJECTCOUNT; ++i) {
        testobjects[i] = mem_scalar_new(rootblock, 100+i);
        totalbytes += 100+i;
        totalblocks++;
    }
    sprintf(line, "%d total scalar blocks", totalblocks);
    is_ii(mem_objects(rootblock), totalblocks, line);  /* test 3 */
    sprintf(line, "%ld total scalar bytes", totalbytes);
    is_ll(mem_bytes(rootblock), totalbytes, line);  /* test 4 */

    /* Resize the even numbered objects up and the odd ones down */
    for (i=0; i<TESTOBJECTCOUNT; ++i) {
        testobjects[i] = mem_scalar_resize(testobjects[i], 20+2*i );
        totalbytes += -(100+i) + (20+2*i);
    }
    sprintf(line, "after reallocation %ld total bytes", totalbytes);
    is_ll(mem_bytes(rootblock), totalbytes, line);  /* test 5 */

    /* Unreference all the objects that were created on the heap */
    /* This is how you "free" memory allocations */
    for (i=0; i<TESTOBJECTCOUNT; ++i) {
        mem_refdel(rootblock, testobjects[i]);
        totalbytes -= (20+2*i);
        totalblocks--;
    }
    sprintf(line, "release array leaves %ld total bytes", totalbytes);
    is_ll(mem_bytes(rootblock), totalbytes, line);  /* test 6 */
    sprintf(line, "release array leaves %d total nodes", totalblocks);
    is_ii(mem_objects(rootblock), totalblocks, line);  /* test 7 */

    /* Release the array containing the test objects */
    mem_refdel(rootblock, testobjects);

    mem_final(rootblock, 1);
}


/* test8_9lists */
/* Create and use a list containing two integers and two strings. */
void
test8_9lists_of_lists()
{
    void * root, * list0, * list1, * scalar0, * scalar1;
    char * answer = "forty-two";
    root = mem_init();
    scalar0 = mem_scalar_new(root, sizeof(int));
    * (int *) scalar0 = 42;
    scalar1 = mem_scalar_new(root, strlen(answer)+1);
    strcpy(scalar1, answer);
    list0 = mem_list_new(root);
    mem_list_put(list0, 0, scalar0);
    mem_list_put(list0, 1, scalar1);
    is_ii(* (int *) mem_list_get(list0, 0), 42, "list0[0] is 42" );
    is_ss((char *) mem_list_get(list0, 1), answer, "list0[1] is \"forty-two\"" );
    /* Unreference the memory to let garbage collection recycle it. */
    mem_refdel(root, list0);
    mem_final(root, 1);
}


/* main */
int
main(int argc, char * argv[])
{
    plan(9);
    test1_7scalars();
    test8_9lists_of_lists();
    return 0;  /* aftwerwards Valgrind should find no memory leaks */
}

/* end of 02c-mem.t.c */
