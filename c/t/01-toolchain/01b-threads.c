/* 01b-threads.c */
/* Check that threading in the operating system and libraries are ok */


#include <stdio.h>   /* perror printf */
#include <stdlib.h>  /* system */
#include "../Test.h"  /* is_ii plan */

#ifdef _WIN32
    #include <windows.h>
    #define sleep(seconds) Sleep(seconds*1000)
#else
    #include <pthread.h>  /* pthread_create */
#endif


struct threadargs {
    int testnumber;
    int seconds;
    char * description;
};


/* test1_thread */
void *
test1_thread(void * args)
{
    struct threadargs * targs = (struct threadargs *) args;
    int testnumber     = targs->testnumber;
    char * description = targs->description;
    sleep(targs->seconds);
    printf("ok %d - from %s thread\n", testnumber, description);
    return NULL;
}

/* tests_1_4 */
void
tests1_4()
{
    int            status;
	#ifdef _MSC_VER
        HANDLE threadhandle1, threadhandle2;
        DWORD threadId1, threadId2;
	#else
        pthread_t      thread_id;
        pthread_attr_t thread_attr;
	#endif
    int threadnumber, threadstacksize;
    struct threadargs thread1arguments, thread2arguments;

    /* Create the first thread */
    thread1arguments.testnumber  = 3;
    thread1arguments.seconds     = 2;
    thread1arguments.description = "first";
    threadstacksize = 16384;  /* Minimum allowed by Posix threads */
	#ifdef _MSC_VER
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
	#ifdef _MSC_VER
	    threadhandle2 = CreateThread(NULL, threadstacksize,
            (LPTHREAD_START_ROUTINE) test1_thread, &thread2arguments, 0,
             &threadId1);
        status = (threadhandle2 == NULL);
	#else
        status = pthread_create(&thread_id, &thread_attr, test1_thread,
                 &thread2arguments);
    #endif
    is_ii(status, 0, "created second thread");
    #ifndef _MSC_VER
        pthread_attr_destroy(&thread_attr);
    #endif
    
    /* Give both threads enough time to complete */
    sleep(3);
}


/* main */
int
main(int arg, char * argv[])
{
    plan(4);
    tests1_4();  /* two simple child threads */
    return 0;
}
