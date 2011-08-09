/* threads.c */

/* Abstract the various threading interfaces offered by different */
/* operating systems.  This library closely resembles pthreads, so */
/* is a very thin wrapper on Posix systems, and does a lightweight */
/* emulation elsewhere. */

#include "threads.h"  /* thread_create */


/* thread_create */
int
thread_create(struct thread_info * info, void * (* function)(void *), void * arg )
{
    int status;
    #if defined( __APPLE__ ) || defined( __linux__ )
        status = pthread_create(&info->thread_id, NULL, function, arg);
    #elif defined( _WIN32 )
        info->threadhandle = CreateThread(NULL, 0,
            (LPTHREAD_START_ROUTINE) function, arg, 0, NULL);
        status = (info->threadhandle == NULL);
    #endif
    return status;
}


/* thread_join */
int
thread_join(struct thread_info * info)
{
    int status;
    #if defined( __APPLE__ ) || defined( __linux__ )
        status = pthread_join(&info->thread_id, NULL);
    #elif defined( _WIN32 )
        status = WaitForSingleObject(threadhandle[i], INFINITE);
    #endif
    return status;
}


/* See also: */
/* Lawrence Livermore National Laboratory tutorials: */
/* pthreads https://computing.llnl.gov/tutorials/pthreads/ */
/* OpenMP https://computing.llnl.gov/tutorials/openMP/ */

/* end of threads.c */
