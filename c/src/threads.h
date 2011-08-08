/* threads.h */

#if defined( __APPLE__ ) || defined( __linux__ )
    #include <pthread.h>
#elif defined( _WIN32 )
    #include <windows.h>
#endif

/* thread_info */
struct thread_info {
    #if defined( __APPLE__ ) || defined( __linux__ )
        pthread_t thread_id;
    #elif defined( _WIN32 )
        HANDLE threadhandle;
    #endif
};

int thread_create(struct thread_info * info, void * (* function)(void *), void * arg );
int thread_join(struct thread_info * info);

/* end of threads.h */
