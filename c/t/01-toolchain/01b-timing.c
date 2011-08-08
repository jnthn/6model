/* 01b-timing.c */

#include <stdio.h>   /* sprintf */
#ifdef _WIN32
    #include <windows.h>
#else
    #include <sys/time.h>
#endif
#include "../Test.h"  /* is_ii plan */


/* seconds_microseconds_sleep */
void
seconds_microseconds_sleep()
{
    /* long long for 64 bits because on Win32, long is only 32 bits */
    long long seconds1, seconds2, seconds3, timediff;
    int microseconds1, microseconds2, microseconds3;
    char message[80];

    /* Read the clock twice in quick succession, sleep for 1 second, */
    /* then read the clock a third time.  Report the differences */
    /* between the times and verify that the sleep was about 1 sec. */
    #ifdef _WIN32
        FILETIME time1, time2, time3;
        GetSystemTimeAsFileTime(&time1); /* * 100ns since 1601-01-01 */
        GetSystemTimeAsFileTime(&time2);
        Sleep(1000);  /* milliseconds */
        GetSystemTimeAsFileTime(&time3);
        seconds1 = (((long long)time1.dwHighDateTime) << 32) | time1.dwLowDateTime;
        microseconds1 = (seconds1 % 10000000)/10; seconds1 /= 10000000;
        seconds2 = (((long long)time2.dwHighDateTime) << 32) | time2.dwLowDateTime;
        microseconds2 = (seconds2 % 10000000)/10; seconds2 /= 10000000;
        seconds3 = (((long long)time3.dwHighDateTime) << 32) | time3.dwLowDateTime;
        microseconds3 = (seconds3 % 10000000)/10; seconds3 /= 10000000;
    #else
        struct timeval time1, time2, time3;
        gettimeofday(&time1, NULL);
        gettimeofday(&time2, NULL);
        sleep(1);  /* seconds */
        gettimeofday(&time3, NULL);
        seconds1 = time1.tv_sec; microseconds1 = time1.tv_usec;
        seconds2 = time2.tv_sec; microseconds2 = time2.tv_usec;
        seconds3 = time3.tv_sec; microseconds3 = time3.tv_usec;
    #endif
    /* test 1 - time1 is nonzero */
    sprintf(message, "clock returned %lld seconds and %d microseconds",
        seconds1, microseconds1);
    ok(seconds1 > 0L && microseconds1 > 0, message);

    /* test 2 - time2 >= time1 */
    sprintf(message, "clock returned %lld seconds and %d microseconds",
        seconds2, microseconds2);
    ok(seconds2 > seconds1 || microseconds2 >= microseconds1, message);

    /* test 3 - time3 is within 2ms of time2 + 1 second */
    timediff = (seconds3 - seconds2) * 1000000
               + microseconds3 - microseconds2;
    sprintf(message, "one second sleep measured %lld microseconds",
        timediff);
    ok(998000 < timediff && timediff < 1002000, message);
}


/* main */
int
main(int arg, char * argv[])
{
    diag("01b-timing");
    plan(3);
    seconds_microseconds_sleep();
    return 0;
}

/* end of 01b-timing.c */
