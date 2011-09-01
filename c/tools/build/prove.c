/* prove.c */
/* Lightweight C version of a TAP (Test Anything Protocol) harness */

/* TODO: support for TODO tests (that are expected to fail) */
/* TODO: ensure the '1..n' (plan) output is either first or last line */
/* TODO: check exit status from child process */

/* Functions are placed in this file at the point just before they */
/* are needed, but all global variables are declared at the start. */

#include <assert.h>  /* assert */
#include <stdio.h>   /* FILE fprintf printf stderr */
#include <stdlib.h>  /* atoi exit free malloc qsort realloc */
#include <string.h>  /* strcat strcpy strlen */
#if defined( _WIN32 )
    #include <windows.h>
    #define pclose _pclose
    #define popen  _popen
    #define DIRECTORY_SEPARATOR "\\"
#else
    #include <dirent.h>  /* opendir readdir */
    #define DIRECTORY_SEPARATOR "/"
#endif

#define LINEBUFFERSIZE 128

/* Global variables */
char * program_name;
char * executable_program;
char * filename_extension;
int tests_passed=0, tests_failed=0, todos_passed=0, total_files=0;


/* options */
/* Process command line options, returning how many were processed */
/* Re-invent the getopt() wheel, because MSVC does not have unistd.h */
int
options(int argc, char * argv[])
{
    int argindex = 0;
    int scanning_args = 0;
    program_name = argv[0];
    executable_program = NULL; /* should be "perl6" ;-) */
    filename_extension = NULL; /* should be ".t" */
    if (argc < 2) {
        fprintf(stderr,
            "Usage: %s [-e \"cmd\"] [--ext \"extension\"] test_directory\n",
             argv[0]
        );
        exit(1);
    }
    scanning_args = 1;
    while (++argindex<argc && *argv[argindex]=='-' && scanning_args) {
        /* not reusable code, match only program specific options */
        if (strcmp(argv[argindex], "-e")==0) {
            executable_program = argv[++argindex];
        }
        else {
            if (strcmp(argv[argindex], "--ext")==0) {
                filename_extension = argv[++argindex];
            }
            else {
                fprintf(stderr, "%s: invalid option '%s'\n"
                    "  (the only usable options are -e and --ext)\n",
                    program_name, argv[argindex]);
                exit(1);
            }
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


/* split_str */
int
split_str(char * str, char * delim, char *** substrings)
{
    int substringcount = 0, substringlen, delimiterlen;
    char * p1, * p2, * substr;
    delimiterlen = strlen(delim);
    assert( delimiterlen>0 );
    p1 = str;
    while ((p2=strstr(p1, delim)) != NULL) {
        substringlen = p2 - p1;
        /* Create or extend the list of pointers to strings */
        if (substringcount++ == 0) {
            * substrings = (char **) malloc(sizeof(char **));
        }
        else {
            * substrings = (char **) realloc(* substrings, substringcount * sizeof(char **));
        }
        /* Push the substring onto the end of the list */
        substr = (char *) malloc(substringlen+1);
        strncpy(substr, p1, substringlen);
        substr[substringlen] = '\0';
        (* substrings)[substringcount-1] = substr;
        /* Move the search pointer past the delimiter */
        p1 += substringlen + delimiterlen;
    }
    /* There is often another substring after the last delimiter */
    if ((substringlen=strlen(p1))>0) {
        /* Create or extend the list of pointers to strings */
        if (substringcount++ == 0) {
            * substrings = (char **) malloc(sizeof(char **));
        }
        else {
            * substrings = (char **) realloc(* substrings, substringcount * sizeof(char **));
        }
        /* Push the substring onto the end of the list */
        substr = (char *) malloc(substringlen+1);
        strncpy(substr, p1, substringlen);
        substr[substringlen] = '\0';
        (* substrings)[substringcount-1] = substr;
    }
    return substringcount;
}


#if ! defined( _WIN32 )
/* scandirectory_comparenames */
int
scandirectory_comparenames(const void * a, const void * b)
{
    /* Hard to understand this casting, but it works. See 'man qsort' */
    return strcmp(* (char * const *) a, * (char * const *) b);
}
#endif


/* scandirectory */
/* Almost an os-independent re-invention of scandir() */
int
scandirectory(char * dirname, char *** filenamelist) /* yes, triple pointer */
{
    int found_a_file, filename_extension_len = 0, filenamecount = 0;
    char * filename;
    #if defined( _WIN32 )
        WIN32_FIND_DATA dir;
        HANDLE hFind;
    #else
        DIR * dir;
        struct dirent * direntry;
        char * nametail;
    #endif
    if (filename_extension != NULL) {
        filename_extension_len = strlen(filename_extension);
    }

    #if defined( _WIN32 )
        filename = (char *) malloc(strlen(dirname) + 2
            + filename_extension_len + 1);
        strcpy(filename, dirname);
        strcat(filename, "\\*");
        if (filename_extension != NULL)
            strcat(filename, filename_extension);
        hFind = FindFirstFile(filename, &dir);
        found_a_file = (hFind != INVALID_HANDLE_VALUE);
    #else
        dir = opendir(dirname);
        if (dir == NULL) {
            fprintf(stderr, "%s: cannot open directory '%s'\n",
                program_name, dirname);
            exit(1);
        }
        direntry = readdir(dir);
        found_a_file = (direntry != NULL);
    #endif
    while (found_a_file) {
        #if defined( _WIN32 )
            filename = dir.cFileName;
        #else
            filename = direntry->d_name;
            if (filename_extension != NULL) {
                nametail = filename+strlen(filename)-strlen(filename_extension);
                if (strcmp(nametail, filename_extension)!=0)
                    filename = NULL;
            }
        #endif
        /* Exclude filenames we don't want */
        if (filename && * filename == '.')
            filename = NULL;
        /* Keep the names we do want */
        if (filename) {
            if (++filenamecount == 1) {
                * filenamelist = malloc(sizeof(char *));
            }
            else {
                * filenamelist = realloc(* filenamelist,
                    filenamecount * sizeof(char *));
            }
            (* filenamelist)[filenamecount-1] = (char *)
                malloc(strlen(filename)+1);
            strcpy((* filenamelist)[filenamecount-1], filename);
        }
        #if defined( _WIN32 )
            found_a_file = FindNextFile(hFind, &dir);
        #else
            direntry = readdir(dir);
            found_a_file = (direntry != NULL);
        #endif
    }
    #if defined( _WIN32 )
        FindClose(hFind);
    #else
        closedir(dir);
        /* sort the found names */
        if (filenamecount > 1) {
            qsort(* filenamelist, filenamecount, sizeof(char *),
                scandirectory_comparenames);
        }
    #endif
    return filenamecount;
}


/* filecollection */
/* a list of directory names containing file names */
struct filecollection {
    int dircount;
    struct filecollection_dir {
        char *  dirname;
        int     filecount;
        char ** filenames;
    } * dirs;
};


/* filecollection_free */
/* Frees all the memory allocated to a filecollection */
void
filecollection_free(struct filecollection * coll)
{
    int i, j;
    for (i=0; i<coll->dircount; ++i) {
        free(coll->dirs[i].dirname);
        for (j=0; j<coll->dirs[i].filecount; ++j) {
            free(coll->dirs[i].filenames[j]);
        }
        free(coll->dirs[i].filenames);
    }
    free(coll->dirs);
    free(coll);
}


/* scandirectories */
/* Takes a list of directory names and returns a filecollection which */
/* is a list of those directory names and also either all the files */
/* in each directory or those that have a specified extension. */
struct filecollection *
scandirectories(int argc, char * argv[])
{
    int i, j, filenamecount;
    char ** filenamelist, * dirname, * s1, * s2;
    struct filecollection * filecoll = NULL;
    struct filecollection_dir * filedir;

    /* The argument list is a series of directory names */
    for (i=0; i<argc; ++i) {
        dirname = argv[i];
        filenamelist = NULL;
        filenamecount = scandirectory(dirname, & filenamelist);
        if (filenamecount) {
            /* Create a new filecollection if it does not yet exist */
            if (filecoll == NULL) {
                filecoll = (struct filecollection *) malloc(sizeof(struct filecollection));
                filecoll->dircount = 0;
                filecoll->dirs = NULL;
            }
            /* Add this directory to the filecollection */
            if (filecoll->dircount++ == 0) {
                filecoll->dirs = (struct filecollection_dir *)
                    malloc(sizeof(struct filecollection_dir));
            }
            else {
                filecoll->dirs = (struct filecollection_dir *)
                    realloc(filecoll->dirs, filecoll->dircount *
                        sizeof(struct filecollection_dir));
            }
            filedir = & filecoll->dirs[filecoll->dircount-1];
            filedir->filecount = 0;
            filedir->filenames = NULL;
            /* Make a copy of the directory name without any trailing */
            /* slashes. */
            s1 = dirname; s2 = dirname + (strlen(dirname) - 1);
            while (* s2 == '/' && s2 > s1+1)
                --s2;
            filedir->dirname = (char *) malloc((s2-s1)+2);
            strncpy( filedir->dirname, dirname, (s2-s1)+2 ); /* remember strncpy does not always copy a '\0' */

            /* Add the found file names to the list of tests to be run */
            for (j=0; j<filenamecount; ++j ) {
                if (filedir->filecount++ == 0) {
                    filedir->filenames = (char **) malloc(sizeof(char *));
                }
                else {
                    filedir->filenames = (char **) realloc(filedir->filenames, filedir->filecount * sizeof(char *));
                }
                filedir->filenames[filedir->filecount-1] =
                    (char *) malloc(strlen(filenamelist[j])+1);
                strcpy(filedir->filenames[filedir->filecount-1], filenamelist[j]);
                free(filenamelist[j]);
            }
        }
        free(filenamelist);
    }
    return filecoll;
}


/* TAP_Parser */
void
TAP_Parser(FILE * program_output)
{
    int planned=0, passed=0, failed=0, testnumber, testnumber_expected;
    int len;
    char line[LINEBUFFERSIZE];

    testnumber_expected = 1;
    while (fgets(line, LINEBUFFERSIZE, program_output)) {
        if (strncmp(line, "1..", 3)==0) { /* plan() output */
            planned = atoi(line+3);
        }
        else {
            if (strncmp(line, "ok ", 3)==0) { /* passed test */
                testnumber = atoi(line+3);
                if (testnumber == testnumber_expected) {
                    ++testnumber_expected;
                }
                else {
                    fprintf(stderr,
                        "tap_parser expected test number %d but got %d\n",
                        testnumber_expected, testnumber );
                    testnumber_expected = testnumber + 1;
                }
                ++passed;
            }
            else {
                if (strncmp(line, "not ok ", 7)==0) { /* failed test */
                    testnumber = atoi(line+7);
                    if (testnumber == testnumber_expected) {
                        ++testnumber_expected;
                    }
                    else {
                        fprintf(stderr,
                            "tap_parser expected test number %d but got %d\n",
                            testnumber_expected, testnumber );
                        testnumber_expected = testnumber + 1;
                    }
                    ++failed;
                }
                else {
                    if (strncmp(line, "#", 1)==0) { /* comment */
                        ;
                    }
                    else {
                        fprintf(stderr, "tap_parser: unexpected line: %s\n", line);
                    }
                }
            }
        }
        /* Display a running X/Y count of results as they arrive */
        len = printf(" %d/%d", passed, planned ? planned : passed+failed);
        while (len--)
            printf("\b");
        fflush(stdout);
    }
    printf("%d/%d %sok", passed,
        (planned > passed+failed ? planned : passed+failed),
        (passed!=planned || failed!=0) ? "some not " : "");
    if (planned != passed + failed ) {
        printf(" (%d planned)", planned);
    }
}


/* runtest */
void
runtest(char * command)
{
    FILE * childprocess;

    /* Run the test and capture its standard output */
    childprocess = popen(command, "r");
    /* Analyze the output for planned number of tests, passes and fails */
    TAP_Parser(childprocess);
    /* Clean up */
    pclose(childprocess);
}


/* runtestdirs */
void
runtestdirs(struct filecollection * coll)
{
    int i, j, commandlen;
    char * command, * tap_output;
    for (i=0; i<coll->dircount; ++i) {
        for (j=0; j<coll->dirs[i].filecount; ++j) {
            commandlen = (executable_program ? strlen(executable_program) + 1 : 0)
                         + strlen(coll->dirs[i].dirname) + 1
                         + strlen(coll->dirs[i].filenames[j]) + 1;
            command = (char *) malloc(commandlen);
            * command = '\0';
            if (executable_program) {
                strcpy(command, executable_program);
                strcat(command, " ");
            }
            strcat(command, coll->dirs[i].dirname);
            strcat(command, DIRECTORY_SEPARATOR );
            strcat(command, coll->dirs[i].filenames[j]);
            printf("%s%s%s ", coll->dirs[i].dirname,
                DIRECTORY_SEPARATOR, coll->dirs[i].filenames[j]);
            fflush(stdout);
            /* Run each test and parse its output */
            runtest(command);
            printf("\n");
            free(command);
        }
    }
}


/* main */
int
main(int argc, char * argv[])
{
    int argi;
    struct filecollection * dirs_and_files;

    /* Get command line options and process them */
    argi = options(argc, argv);

    /* Scan the remaining non-option arguments as directory names */
    dirs_and_files = scandirectories(argc-argi, argv+argi);
    /* argi hides what options() took from argv */

    /* Perform each test and parse its TAP output */
    runtestdirs(dirs_and_files);

    /* Clean up when finished */
    filecollection_free(dirs_and_files);
    return 0;
}

/* See also: */
/* perldoc prove, TAP::Harness, TAP::Parser::Aggregator, */
/* TAP::Parser::Grammar, TAP::Formatter::Console */

/* end of prove.c */
