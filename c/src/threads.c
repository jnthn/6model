/* threads.c */

/* Abstract the different threading interfaces present in different */
/* operating systems.  This library closely resembles pthreads, so */
/* is a very thin wrapper on Posix systems, and does a lightweight */
/* emulation on the others. */

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

/* end of threads.c */
