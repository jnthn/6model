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
    long long seconds1, seconds2, seconds3, seconds4, seconds5, timediff;
    int microseconds1, microseconds2, microseconds3, microseconds4, microseconds5;
    char message[80];

    /* Read the clock twice in quick succession, sleep for 1 second, */
    /* then read the clock a third time.  Report the differences */
    /* between the times and verify that the sleep was about 1 sec. */
    #ifdef _WIN32
        FILETIME time1, time2, time3, time4, time5;
        GetSystemTimeAsFileTime(&time1); /* * 100ns since 1601-01-01 */
        GetSystemTimeAsFileTime(&time2);
        Sleep(0);     /* 0 milliseconds */
        GetSystemTimeAsFileTime(&time3);
        Sleep(1);     /* 1 millisecond */
        GetSystemTimeAsFileTime(&time4);
        Sleep(1000);  /* 1 second */
        GetSystemTimeAsFileTime(&time5);
        seconds1 = (((long long)time1.dwHighDateTime) << 32) | time1.dwLowDateTime;
        microseconds1 = (seconds1 % 10000000)/10; seconds1 /= 10000000;
        seconds2 = (((long long)time2.dwHighDateTime) << 32) | time2.dwLowDateTime;
        microseconds2 = (seconds2 % 10000000)/10; seconds2 /= 10000000;
        seconds3 = (((long long)time3.dwHighDateTime) << 32) | time3.dwLowDateTime;
        microseconds3 = (seconds3 % 10000000)/10; seconds3 /= 10000000;
        seconds4 = (((long long)time4.dwHighDateTime) << 32) | time4.dwLowDateTime;
        microseconds4 = (seconds4 % 10000000)/10; seconds4 /= 10000000;
        seconds5 = (((long long)time5.dwHighDateTime) << 32) | time5.dwLowDateTime;
        microseconds5 = (seconds5 % 10000000)/10; seconds5 /= 10000000;
    #else
        struct timeval time1, time2, time3, time4, time5;
        struct timespec ns = {0, 0}, ns_remain;
        gettimeofday(&time1, NULL);
        gettimeofday(&time2, NULL);
        nanosleep(&ns, &ns_remain); /* 0 nanoseconds */
        gettimeofday(&time3, NULL);
        ns.tv_nsec = 1000000; nanosleep(&ns, &ns_remain); /* 1 millisecond */
        gettimeofday(&time4, NULL);
        sleep(1); /* 1 second */
        gettimeofday(&time5, NULL);
        seconds1 = time1.tv_sec; microseconds1 = time1.tv_usec;
        seconds2 = time2.tv_sec; microseconds2 = time2.tv_usec;
        seconds3 = time3.tv_sec; microseconds3 = time3.tv_usec;
        seconds4 = time4.tv_sec; microseconds4 = time4.tv_usec;
        seconds5 = time5.tv_sec; microseconds5 = time5.tv_usec;
    #endif
    /* test 1 - time1 is nonzero */
    sprintf(message, "clock returned %lld seconds and %d microseconds",
        seconds1, microseconds1);
    ok(seconds1 > 0L, message);

    /* test 2 - time2 >= time1 */
    sprintf(message, "clock returned %lld seconds and %d microseconds",
        seconds2, microseconds2);
    ok(seconds2 > seconds1 || microseconds2 >= microseconds1, message);

    /* test 3 - time3 is within 1ms of time2 */
    timediff = (seconds3 - seconds2) * 1000000
               + microseconds3 - microseconds2;
    sprintf(message, "zero sleep measured %lld microseconds",
        timediff);
    ok(0 <= timediff && timediff < 1000, message);

    /* test 4 - time4 is within 0.15ms of time3 + 1 millisecond */
    timediff = (seconds4 - seconds3) * 1000000
               + microseconds4 - microseconds3;
    sprintf(message, "one millisecond sleep measured %lld microseconds",
        timediff);
    ok(1000 <= timediff && timediff < 1150, message);

    /* test 5 - time5 is within 2ms of time4 + 1 second */
    timediff = (seconds5 - seconds4) * 1000000
               + microseconds5 - microseconds4;
    sprintf(message, "one second sleep measured %lld microseconds",
        timediff);
    ok(998000 < timediff && timediff < 1002000, message);
}


/* main */
int
main(int arg, char * argv[])
{
    diag("01b-timing");
    plan(5);
    seconds_microseconds_sleep();
    return 0;
}

/* end of 01b-timing.c */
