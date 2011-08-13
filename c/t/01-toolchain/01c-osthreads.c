/* 01c-osthreads.c */
/* Check that threading in the operating system and libraries work ok */

#include <assert.h>   /* assert */
#include <stdio.h>    /* perror printf */
#include <stdlib.h>   /* system */
#include <string.h>   /* strlen */
#include "../Test.h"  /* diag is_ii ok plan */
#if defined( _OPENMP )
    #include <omp.h>
#endif

#ifdef _WIN32
    #include <windows.h>  /* FILETIME GetSystemTimeAsFileTime Sleep */
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
        pthread_t      thread_id1, thread_id2;
        pthread_attr_t thread_attr;
	#endif
    int threadnumber, threadstacksize;
    struct test1_threadargs thread1arguments, thread2arguments;

    /* Create the first thread */
    thread1arguments.testnumber   = 3;
    thread1arguments.seconds      = 2;
    thread1arguments.description  = "first";
    threadstacksize = 16384;  /* Minimum allowed by Posix threads */
	#ifdef _WIN32
	    threadhandle1 = CreateThread(NULL, threadstacksize,
            (LPTHREAD_START_ROUTINE) test1_thread, &thread1arguments, 0,
             &threadId1);
        status = (threadhandle1 == NULL);
	#else
        status = pthread_attr_init(&thread_attr);
        pthread_attr_setstacksize(&thread_attr, threadstacksize);
        status = pthread_create(&thread_id1, &thread_attr, test1_thread,
                 &thread1arguments);
	#endif
    is_ii(status, 0, "created first thread");
    sleep(1); /* Let the first thread run immediately */

    /* Create the second thread */
    thread2arguments.testnumber   = 4;
    thread2arguments.seconds      = 2;
    thread2arguments.description  = "second";
	#ifdef _WIN32
	    threadhandle2 = CreateThread(NULL, threadstacksize,
            (LPTHREAD_START_ROUTINE) test1_thread, &thread2arguments, 0,
             &threadId1);
        status = (threadhandle2 == NULL);
	#else
        status = pthread_create(&thread_id2, &thread_attr, test1_thread,
                 &thread2arguments);
    #endif
    is_ii(status, 0, "created second thread");
    #ifndef _WIN32
        pthread_attr_destroy(&thread_attr);
    #endif
    
    /* Give both threads enough time to complete */
    sleep(3);

    /* Clean up the thread zombies */
    pthread_join(thread_id1, NULL);
    pthread_join(thread_id2, NULL);
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


/* tests6_8vectordotproduct */
void
tests6_8vectordotproduct()
{
    /* Calculate a vector dot product three ways, first in a single */
    /* thread for comparison, then with native OS threads, and then */
    /* with OpenMP threads.  Test with 10, 1000 and 1000000 elements. */
    int i, j, vectorsize[3] = {10,1000,1000000}, elements, chunk = 3;
    long long time1, time2, * v1, * v2, dp1, dp2;
//  double * u, * v, dotproduct1, dotproduct2;
    char message[80];
    #if defined( _WIN32 )
        FILETIME now;
    #else
        struct timeval now;
    #endif

    /* Do the tests three times, with 10, 100 and 100000 elements */
    for (i=0; i<3; ++i) {
        elements = vectorsize[i];
//      u = (double *) malloc(elements * sizeof(double)); assert(u!=NULL);
//      v = (double *) malloc(elements * sizeof(double)); assert(v!=NULL);
        v1 = (long long *) malloc(elements * sizeof(long long)); assert(v1!=NULL);
        v2 = (long long *) malloc(elements * sizeof(long long)); assert(v2!=NULL);
        /* Fill the two vectors with data */
        for (j=0; j<elements; ++j) {
//          u[j] = (double)(j);
//          v[j] = (double)(elements - j);
            v1[j] = (long long)(j);
            v2[j] = (long long)(elements - j);
        }
        /* Calculate the dot product with just the main thread */
        /* Get the system time in microseconds into time1 */
        #if defined( _WIN32 )
            GetSystemTimeAsFileTime(&now);
            time1 = ((((long long)now.dwHighDateTime) << 32) | now.dwLowDateTime)/10;
        #else
            gettimeofday(&now, NULL);
            time1 = now.tv_sec * 1000000LL + now.tv_usec;
        #endif
//      dotproduct1 = 0.0;
        dp1 = 0;
        for (j=0; j<elements; ++j) {
//          dotproduct1 += u[j] * v[j];
            dp1 += v1[j] * v2[j];
        }
        /* Get the system time in microseconds into time2 */
        #if defined( _WIN32 )
            GetSystemTimeAsFileTime(&now);
            time2 = ((((long long)now.dwHighDateTime) << 32) | now.dwLowDateTime)/10;
        #else
            gettimeofday(&now, NULL);
            time2 = now.tv_sec * 1000000LL + now.tv_usec;
        #endif
        sprintf(message, "single threaded%9d elements%7d microseconds", elements, (int)(time2-time1));
        diag(message);
        /* Calculate the dot product with OpenMP threads, if available */
        #if defined( _OPENMP )
            /* Get the system time in microseconds into time1 */
            #if defined( _WIN32 )
                GetSystemTimeAsFileTime(&now);
                time1 = ((((long long)now.dwHighDateTime) << 32) | now.dwLowDateTime)/10;
            #else
                gettimeofday(&now, NULL);
                time1 = now.tv_sec * 1000000LL + now.tv_usec;
            #endif
//          dotproduct2 = 0.0;
            dp2 = 0;
//          #pragma omp parallel for default(shared) private(j) schedule(static) reduction(+:dotproduct2) reduction(+:dp2)
            #pragma omp parallel for default(shared) private(j) schedule(static) reduction(+:dp2)
            for (j=0; j < elements; j++) {
//              dotproduct2 = dotproduct2 + (u[j] * v[j]);
                dp2 = dp2 + (v1[j] * v2[j]);
            }

            /* Get the system time in microseconds into time2 */
            #if defined( _WIN32 )
                GetSystemTimeAsFileTime(&now);
                time2 = ((((long long)now.dwHighDateTime) << 32) | now.dwLowDateTime)/10;
            #else
                gettimeofday(&now, NULL);
                time2 = now.tv_sec * 1000000LL + now.tv_usec;
            #endif
            sprintf(message, "OpenMP threaded%9d elements%7d microseconds", elements, (int)(time2-time1));
            diag(message);
//          sprintf(message, "OpenMP dot products double %lf %lf", dotproduct1, dotproduct2);
//          diag(message);
//          sprintf(message, "OpenMP dot products long long %lld %lld", dp1, dp2);
//          diag(message);
//          ok(dotproduct2 == dotproduct1, "dot products match double");
            ok(dp2 == dp1, "dot products match");
        #else
            diag("OpenMP not available");
        #endif
//      free(u); free(v);
        free(v1); free(v2);
    }
}

/* main */
int
main(int arg, char * argv[])
{
    int testplan = 5;
    diag("01c-osthreads");
    #if defined( _OPENMP )
        testplan += 3;
    #endif
    plan(testplan);
    tests1_4sleeps();     /* two threads that sleep and print */
    tests5_6charcount();  /* four threads returning integers */
    tests6_8vectordotproduct();
    return 0;
}

/* end of 01c-osthreads.c */
