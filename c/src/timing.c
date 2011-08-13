/* timing.c */

/* This timing library abstracts the different ways that operating */
/* systems give the current time and do delays. */

#ifdef _WIN32
    #include <windows.h>   /* FILETIME GetSystemTimeAsFileTime Sleep */
#else
    #include <stdlib.h>    /* NULL */
    #include <sys/time.h>  /* gettimeofday nanosleep */
#endif
#include "timing.h"


/* gettime */
/* Returns UTC seconds from 1970-01-01 in the upper 44 bits and */
/* microseconds in the lower 20 bits of a 64 bit value. */
long long
gettime()
{
    int microseconds;
    long long seconds, result;
    #ifdef _WIN32
        FILETIME now;
        GetSystemTimeAsFileTime(&now);
        seconds = (((long long)now.dwHighDateTime) << 32)
                             | now.dwLowDateTime;
        seconds -= 11644473600LL; /* from 1601-01-01 to 1970-01-01 */
        microseconds = (seconds % 10000000)/10; seconds /= 10000000;
    #else
        struct timeval now;
        gettimeofday(&now, NULL);
        seconds      = now.tv_sec;
        microseconds = now.tv_usec;
    #endif
    return (seconds << 20) | microseconds;
}


/* millisleep */
void
millisleep(int milliseconds)
{
    #ifdef _WIN32
        Sleep(milliseconds);
    #else
        struct timespec request, remainder;
        request.tv_sec  = milliseconds / 1000;
        request.tv_nsec = (milliseconds % 1000) * 1000000;
        nanosleep(&request, &remainder);  /* ignore possible errors */
    #endif
}

/* See also: */
/* UTC, TAI, and UNIX time http://cr.yp.to/proto/utctai.html */
/* Online time converter: http://www.silisoftware.com/tools/date.php */

/* end of timing.c */
