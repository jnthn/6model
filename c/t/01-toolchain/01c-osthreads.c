/* 01c-osthreads.c */
/* Check that threading in the operating system and libraries is ok */

#include <assert.h>   /* assert */
#include <stdio.h>    /* perror printf */
#include <stdlib.h>   /* system */
#include <string.h>   /* strlen */
#include "../Test.h"  /* diag is_ii ok plan */

#ifdef _WIN32
    #include <windows.h>
    #define sleep(seconds) Sleep(seconds*1000)
#else
    #include <pthread.h>  /* pthread_create pthread_join */
#endif


struct test1_threadargs {
    int testnumber;
    int seconds;
    char * description;
};


/* test1_thread */
void *
test1_thread(void * args)
{
    int testnumber;
    char * description;
    char message[80];
    struct test1_threadargs * targs;

    targs = (struct test1_threadargs *) args;
    testnumber  = targs->testnumber;
    description = targs->description;
    sleep(targs->seconds);
    sprintf(message, "from %s thread", description);
    ok(1, message);
    return NULL;
}


/* tests_1_4 */
void
tests1_4sleeps()
{
    int            status;
	#ifdef _WIN32
        HANDLE threadhandle1, threadhandle2;
        DWORD threadId1, threadId2;
	#else
        pthread_t      thread_id;
        pthread_attr_t thread_attr;
	#endif
    int threadnumber, threadstacksize;
    struct test1_threadargs thread1arguments, thread2arguments;

    /* Create the first thread */
    thread1arguments.testnumber  = 3;
    thread1arguments.seconds     = 2;
    thread1arguments.description = "first";
    threadstacksize = 16384;  /* Minimum allowed by Posix threads */
	#ifdef _WIN32
	    threadhandle1 = CreateThread(NULL, threadstacksize,
            (LPTHREAD_START_ROUTINE) test1_thread, &thread1arguments, 0,
             &threadId1);
        status = (threadhandle1 == NULL);
	#else
        status = pthread_attr_init(&thread_attr);
        pthread_attr_setstacksize(&thread_attr, threadstacksize);
        status = pthread_create(&thread_id, &thread_attr, test1_thread,
                 &thread1arguments);
	#endif
    is_ii(status, 0, "created first thread");
    sleep(1); /* Let the first thread run immediately */

    /* Create the second thread */
    thread2arguments.testnumber  = 4;
    thread2arguments.seconds     = 2;
    thread2arguments.description = "second";
	#ifdef _WIN32
	    threadhandle2 = CreateThread(NULL, threadstacksize,
            (LPTHREAD_START_ROUTINE) test1_thread, &thread2arguments, 0,
             &threadId1);
        status = (threadhandle2 == NULL);
	#else
        status = pthread_create(&thread_id, &thread_attr, test1_thread,
                 &thread2arguments);
    #endif
    is_ii(status, 0, "created second thread");
    #ifndef _WIN32
        pthread_attr_destroy(&thread_attr);
    #endif
    
    /* Give both threads enough time to complete */
    sleep(3);
}


/* charcount_args */
/* Input and output parameters for the threaded charcount test */
struct charcount_args {
    char * textdata;
    int  textlength;
    char sought;
    int  finds;
};


/* charcount */
void *
charcount(void * argptr)
{
    char * s, sought;
    int len, count;
    struct charcount_args * args;
    args   = (struct charcount_args *) argptr;
    s      = args->textdata;
    len    = args->textlength;
    sought = args->sought;
    count  = 0;
    while (len--) {
        if (* s++ == sought) {
            ++count;
        }
    }
    args->finds = count;
    return NULL;
}


/* tests5_6charcount */
/* */
void
tests5_6charcount()
{
    char message[80], s01[] =
"Mostly, we're just a bunch of ants all cooperating (sort of) to haul"
"food toward the nest (on average).  There are many groups of people"
"working on various bits and pieces as they see fit, since this is"
"primarily a volunteer effort.";
    struct charcount_args args[4];
    int i, sublength, threadstacksize, status, totalfinds;
    #ifdef _WIN32
        HANDLE threadhandle[4];
        DWORD threadId[4];
    #else
        pthread_t      thread_id[4];
        pthread_attr_t thread_attr;
    #endif

    /* Prepare search arguments for four search threads each dealing */
    /* with a substring */
    sublength = strlen(s01) / 4;
    for (i=0; i<4; ++i) {
        args[i].textdata   = s01 + i * sublength;
        args[i].textlength = i < 3 ? sublength : (strlen(s01) - 3 * sublength);
        args[i].sought     = ' ';
        args[i].finds      = -1;
    }

    /* Start the four search threads */
    threadstacksize = 16384;  /* Minimum allowed by Posix threads */
    for (i=0; i<4; ++i) {
	    #ifdef _WIN32
	        threadhandle[i] = CreateThread(NULL, threadstacksize,
                (LPTHREAD_START_ROUTINE) charcount, &args[i], 0,
                 &threadId[i]);
            status = (threadhandle[i] == NULL);
	    #else
            status = pthread_attr_init(&thread_attr);
            pthread_attr_setstacksize(&thread_attr, threadstacksize);
            status = pthread_create(&thread_id[i], &thread_attr,
                         charcount, &args[i]);
	    #endif
	    assert( status == 0 );
    }

    /* Wait for the four threads to finish */
    totalfinds = 0;
    for (i=0; i<4; ++i) {
	    #ifdef _WIN32
	        status = WaitForSingleObject(threadhandle[i], INFINITE);
	    #else
  	        status = pthread_join(thread_id[i], NULL);
	    #endif
	    totalfinds += args[i].finds;
	    assert( status == 0 );
    }

    /* Perform a single search in the main thread */
    args[0].textdata   = s01;
    args[0].textlength = strlen(s01);
    args[0].sought     = ' ';
    args[0].finds      = -1;
    charcount(&args[0]);
    sprintf(message, "found %d occurrences of '%c' both single and "
        "multi threaded", args[0].finds, args[0].sought);
    ok(args[0].finds == totalfinds, message);
}


/* main */
int
main(int arg, char * argv[])
{
    diag("01c-osthreads");
    plan(5);
    tests1_4sleeps();  /* two threads that sleep and print */
    tests5_6charcount();  /* four threads returning integers */
    return 0;
}

/* end of 01c-osthreads.c */
