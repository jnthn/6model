/* 02a-threads.c */

#include <assert.h>   /* assert */
#include "../../src/threads.h"
#include "../../src/timing.h"
#include "../Test.h"  /* diag is plan */

#ifdef __cplusplus
extern "C" {
#endif


/* test1args */
struct test1args {
    int milliseconds;
    char * message;
};


/* test1func */
void *
test1func(void * arg)
{
    char message[80];
    struct test1args * targ;  targ = (struct test1args *) arg;
    sprintf(message, "from %s sleep", targ->message);
    millisleep(targ->milliseconds);
    ok(1, message);
    return NULL;
}


/* tests1_1 */
void
tests1_1(void)
{
    struct thread_info tinfo[2];
    struct test1args   targs[2];
    targs[0].milliseconds = 10; targs[0].message = "10ms";
    targs[1].milliseconds = 20; targs[1].message = "20ms";
    ok( thread_create(tinfo+0, test1func, targs+0) == 0, "start 10ms sleep" );
    ok( thread_create(tinfo+1, test1func, targs+1) == 0, "start 20ms sleep" );
    millisleep(75);
}


/* main */
int main(int argc, char *argv[])
{
    diag("02a-threads");
    plan(4);
    tests1_1();
}

#ifdef __cplusplus
}
#endif

/* end of 02a-threads.c */
