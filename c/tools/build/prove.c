/* prove.c */
/* Lightweigt Test Anything Protocol (TAP) harness */

#include <stdio.h>   /* fclose fgets FILE fopen fprintf printf stderr */
#include <stdlib.h>  /* exit free getenv malloc realloc */
#include <string.h>  /* memmove memcpy strcpy strlen strstr */

#ifdef _WIN32
#define pclose _pclose
#define popen  _popen
#endif
#define LINEBUFFERSIZE 128

char * executable_program;
char * filename_extension;


/* options */
/* Process command line options, returning how many were processed */
/* Re-invent the getopt() wheel, because MSVC does not have unistd.h */
int
options(int argc, char * argv[])
{
    int argindex = 0;  /* will skip argv[0], it is the program name */
    int scanning_args = 0;
    executable_program = NULL;
    filename_extension = NULL;
    if (argc < 2) {
        fprintf(stderr, "Usage: %s test_directory\n", argv[0]);
        exit(1);
    }
    scanning_args = 1;
    while (++argindex<argc && *argv[argindex]=='-' && scanning_args) {
        /* not reusable code, match only program specific options */
        if (strcmp(argv[argindex], "-e")==0) {
            executable_program = argv[++argindex];
        }
        if (strcmp(argv[argindex], "--ext")==0) {
            filename_extension = argv[++argindex];
        }
    }
    return argindex;
}

/* qx */
/* Imitate the Perl qx operator, returning the results of running the */
/* command passed as a parameter */
char *
qx(char * command)
{
    int linesize, textsize = 0;
    char linebuffer[LINEBUFFERSIZE];
    char * returntext = NULL;

    FILE * childprocess;
    childprocess = popen(command, "r");
    while (fgets(linebuffer, LINEBUFFERSIZE, childprocess)) {
        linesize = strlen(linebuffer);
        returntext = textsize
            ? realloc(returntext, textsize+linesize+1)
            : malloc(linesize+1);     /* +1 for '\0' at end of string */
        strcpy(returntext+textsize, linebuffer);
        textsize += linesize;
    }
    pclose(childprocess);
    return returntext;
}


/* main */
int
main(int argc, char * argv[])
{
    char * tap_output;
    int argi;
    argi = options(argc, argv);
    printf("exe=%s ext=%s arg=%s\n", executable_program, filename_extension, argv[argi]);
    tap_output = qx("dir");
    printf("tap_output=%s\n", tap_output);
    return 0;
}
