/* Test.h */

/* Lightweight TAP (Test Anything Protocol) emitter in C macros. */

/* Example usage:

#include <string.h>
#include "../Test.h"

int
main(int argc, char * argv[])
{
    char s[7];
    plan(1);
    ok(1==1, "1 is equal to 1");
    is_ii(1+1, 2, "1 plus 1 is equal to 2");
    strcpy(s, "foo"); strcat(s, "bar");
    is_ss(s, "foobar", "strcpy and strcat");
    return 0;
}

*/

#include <stdio.h>

int testnumber=0; /* yes, namespace pollution. patches welcome ;-) */

#define plan(count)  printf("1..%d\n", count)

#define \
ok(flag,desc) \
    printf("%sok %d - %s\n", \
        flag?"":"not ",++testnumber,desc)

#define \
is_ii(got,expected,desc) \
    printf("%sok %d - %s\n", \
        got==expected?"":"not ",++testnumber,desc); \
    if(got!=expected) \
        printf("# got      : %d\n# expected : %d\n", got, expected)

#define \
is_ss(got,expected,desc) \
    printf("%sok %d - %s\n", \
        strcmp(got,expected)?"not ":"",++testnumber,desc)

#define \
diag(message) \
    printf("# %s\n", message)

/* end of Test.h */
