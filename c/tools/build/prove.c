/* prove.c */
/* Lightweight TAP (Test Anything Protocol) harness */

/* TODO: parse the test script output looking for 'ok', 'not ok' etc */
/* and output a summary instead of every test result. */

#include <glob.h>    /* glob globfree */
#include <stdio.h>   /* FILE fprintf printf stderr */
#include <stdlib.h>  /* exit free malloc realloc */
#include <string.h>  /* strcat strcpy strlen */

#ifdef _WIN32
    #define pclose _pclose
    #define popen  _popen
#endif
#define LINEBUFFERSIZE 128

char * executable_program = NULL;
char * filename_extension = NULL;


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


/* run_tests_in_dir */
void
run_tests_in_dir(char * dir)
{
    int patternlength, glob_flags, status, pathindex;
    char * glob_pattern, * tap_output, * errormessage;
    int (* glob_errfunc) (const char * epath, int eerrno);
    glob_t globbuf;

    /* Scan the specified directory for test files */
    patternlength = strlen(dir) + strlen(filename_extension) + 3;
    glob_pattern = (char *) malloc(patternlength);
    strcpy(glob_pattern, dir);
    strcat(glob_pattern, "/*");
    strcat(glob_pattern, filename_extension);
    glob_flags = 0;
    glob_errfunc = NULL;
    status = glob(glob_pattern, glob_flags, glob_errfunc, &globbuf);
    free(glob_pattern);
    if (status) {
        switch (status) {
            case GLOB_NOSPACE:
                errormessage = "out of memory";
                break;
            case GLOB_ABORTED:
                errormessage = "read error";
                break;
            case GLOB_NOMATCH:
                errormessage = "no files found";
                break;
        }
        fprintf(stderr,
            "scanning directory '%s' ended unexpectedly with %s\n",
            dir, errormessage);
        exit(1);
    }

    /* Run each test file found in the directory and scan the output */
    for (pathindex=0; pathindex<globbuf.gl_pathc; pathindex++) {
        // printf("found path %s\n", globbuf.gl_pathv[pathindex]);
        tap_output = qx(globbuf.gl_pathv[pathindex]);
        printf("%s\n", tap_output);
        free(tap_output);
    }
}


/* main */
int
main(int argc, char * argv[])
{
    int i, argi;

    /* Get command line options and process them */
    argi = options(argc, argv);

    /* The remaining arguments are all expected to be directories */
    for (i=argi; i<argc; ++i)
        run_tests_in_dir(argv[i]);
    return 0;
}
