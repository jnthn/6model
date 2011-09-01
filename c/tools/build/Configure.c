/* Configure.c */
/* Compiled and run by 6model/c/Configure.{sh,bat} */

/* This program (Configure) uses environment variables and C macros */
/* to autodetect the operating system, compiler and other utilities */
/* that can be used to build your software.  It then creates your */
/* Makefile based on a template (Makefile.in).  To also work with */
/* non-GNU systems such as Microsoft C/C++, it follows the style of */
/* Automake and Autoconf, but is written only in C and does not rely */
/* on other tools such as M4. */

/* This work will never be complete, because autodetection is hard. */
/* New software emerges, environments and tools evolve, users make */
/* unforeseen choices.  Monitor the changes through regular testing. */

/* This project uses only a standard C compiler, currently verified to
 * work with:
 * GCC, the GNU Compiler Collection on Linux and OS X.
 * http://www.gnu.org/software/gcc/
 *
 * MinGW, Minimalist GNU for Windows
 * http://mingw.org/
 * Currently based on GCC 4.5.2, 85MB disk.
 * Targets Win32 libraries, no Posix emulation or dlopen.
 * (older version bundled with Git full install)

 * Microsoft Visual C++ Express Edition 1-2GB RAM
 * Registration required to avoid de-activation after 30 days.  Downloads a 3.2MB web installer.
 * http://www.microsoft.com/express/vc/ do not need optional SQL express.
 * Also installs Windows Installer 4.5, .NET Framework 4, SQL Server Compact 3.5, Help Viewer 1.0
 * (download 146MB, disk space 2.3GB)
 */

#include <stdio.h>   /* fclose fgets FILE fopen fprintf printf stderr */
#include <stdlib.h>  /* exit free getenv malloc realloc */
#include <string.h>  /* memmove memcpy strcpy strlen strstr */
#if defined( __APPLE__ )
    #include <sys/sysctl.h>
#elif defined( __linux__ )
    #include <unistd.h>   /* sysconf */
#elif defined( _WIN32 )
    #include <windows.h>  /* GetSystemInfo */
    /* note Microsoft C 2010 needs -wd4820 -wd4668 -wd4255 */
#endif
#if defined( _OPENMP )
    #include <omp.h> /* on Debian/Ubuntu install gcc-4.4-source */
#endif

#define LINEBUFFERSIZE 128
/* Subscript names for scanned evidence.  Almost like a perl hash ;) */
enum { OS_VAR,
       DETECTED_END /* This one must always be last */};
char * detected[DETECTED_END] = {""};
/* Subscript names for configuration strings.  Almost like a hash ;) */
enum { CC, EXE, LDL, MAKE_COMMAND, OPENMP, OUTFILE, RM_RF, THREADS,
       CONFIG_END /* this one must always be last */ };
       /* note the words and OUT clash with MinGW */
char * config[CONFIG_END] = {"", "", "", "", "", "", ""};
/* forward references to internal functions */
void detect(void);
char * slurp(char * filename);
void squirt(char * text, char * filename);
void trans(char ** text, char * search, char * replace);


/* detection */
/* Find and show differences between compilers and operating systems */
void
detection(void)
{
    int processors = 0;
    #if defined( _WIN32 ) && ! defined( _OPENMP )
        SYSTEM_INFO sysinfo;  /* declare up here because MSC hates it lower */
    #endif
    printf("Configure detects the following:\n");

    /* Operating system */
    printf("  Operating system in C predefined macro: ");
    #if defined( __APPLE__ )
        printf("__APPLE__");
    #endif
    #if defined( __linux__ )
        printf("__linux__");
    #endif
    #if defined( _WIN32 )
        printf("_WIN32");
	    config[EXE] = ".exe";
    #else
	    config[EXE] = "";
    #endif
    #if !(defined(__APPLE__) || defined(__linux__) || defined(_WIN32))
        printf("unknown\n  (not __APPLE__ __linux__ or _WIN32)\n");
    #endif
    printf("\n");

    /* C compiler */
    printf("  C compiler in predefined macro: ");
    #if defined( __GNUC__ )
        printf("__GNUC__");
    #endif
    #if defined( _MSC_VER )
        printf("_MSC_VER");
    #endif
    #if !(defined( __GNUC__ ) || defined( _MSC_VER ))
        printf("unknown\n  (not __GNUC__ or _MSC_VER)");
    #endif
    printf("\n");

    printf("  OpenMP: ");
    #if defined( _OPENMP )
        processors = omp_get_num_procs();
            printf("v%d max processors/threads %d/%d %.9lfs ticks\n",
            _OPENMP, processors, omp_get_max_threads(), omp_get_wtick());
        config[OPENMP] =
        #if defined( __GNUC__ )
            "-fopenmp";
        #else
            "-openmp";
        #endif
    #else
        printf("not enabled in the C compiler\n");

        /* Number of processors */
        /* from http://stackoverflow.com/questions/150355/programmatically-find-the-number-of-cores-on-a-machine */
        #if defined( __APPLE__ )
            int mib[4] = {CTL_HW, HW_NCPU, 0, 0};
            size_t size = sizeof(processors);
            sysctl(mib, 2, &processors, &size, NULL, 0);
        #elif defined( __linux__ )
            processors = sysconf(_SC_NPROCESSORS_ONLN);
        #elif defined( _WIN32 )
            GetSystemInfo( &sysinfo );
            processors = sysinfo.dwNumberOfProcessors;
        #endif
        printf("  Processor count from operating system: %d\n", processors);
    #endif

    /* Sizes of built in data types */
    printf("Data sizes:  char short int long long_long float double pointer\n");
    printf("      bytes:  %3d  %3d  %3d  %3d       %3d   %3d    %3d     %3d\n",
           (int)sizeof(char), (int)sizeof(short), (int)sizeof(int),
           (int)sizeof(long), (int)sizeof(long long), (int)sizeof(float),
           (int)sizeof(double), (int)sizeof(void *) );
    printf("      bits:   %3d  %3d  %3d  %3d       %3d   %3d    %3d     %3d\n",
           (int)sizeof(char)*8, (int)sizeof(short)*8, (int)sizeof(int)*8,
           (int)sizeof(long)*8, (int)sizeof(long long)*8, (int)sizeof(float)*8,
           (int)sizeof(double)*8, (int)sizeof(void *)*8);
}

/* config_set */
/* Use environment variables and other clues to assign values to */
/* members of the config[] array */
void
config_set(void)
{
    /* C compiler */
    #if defined( _MSC_VER )
        /* See http://msdn.microsoft.com/en-US/library/b0084kay%28v=VS.100%29.aspx */
        config[CC] = "cl ";
	    config[OUTFILE] = "-Fe";
    #elif defined( __GNUC__ )
        #if defined( _WIN32 )
            config[CC] = "mingw32-gcc ";
        #else
            config[CC] = "gcc -ldl ";
        #endif
        config[OUTFILE] = "-o";
    #endif

    /* File delete command */
    config[RM_RF] =
    #if (defined( __APPLE__ ) || defined( __linux__ ))
	    "rm -rf";
    #elif defined( _WIN32 )
	    "del /F /Q /S";
    #else
        "error: no file delete command";
    #endif

    /* Threads */
    config[THREADS] =
    #if (defined( __APPLE__ ) || defined( __linux__ ))
	    "-pthread";  /* Posix threads */
    #elif ( defined( _WIN32 ) && defined( __GNUC__ ))
	    "-mthreads"; /* MinGW */
    #else
        "";
    #endif

    /* Make utility */
    config[MAKE_COMMAND] =
    #if defined( _WIN32 )
        #if defined( __GNUC__ )
            "mingw32-make";
        #else
            "nmake";
        #endif
    #else
	    "make";
    #endif
    /* TODO: verify that the chosen make command actually works */
}


/* makefile_convert */
void
makefile_convert(char * programfilename, char * templatefilename,
    char * outputfilename)
{
    char * makefiletext;
    printf("    %s: reading from %s\n", programfilename, templatefilename);
    makefiletext = slurp(templatefilename);
    trans(&makefiletext, "# Makefile.in",    "# Makefile");
    trans(&makefiletext, "This is the file", "This is NOT the file");
    trans(&makefiletext, "@cc@",        config[CC]);
    trans(&makefiletext, "@exe@",       config[EXE]);
    trans(&makefiletext, "@openmp@",    config[OPENMP]);
    trans(&makefiletext, "@outfile@",   config[OUTFILE]);
    trans(&makefiletext, "@rm_rf@",     config[RM_RF]);
    trans(&makefiletext, "@threads@",   config[THREADS]);
    #if defined( _WIN32 )
        trans(&makefiletext, "src/",             "src\\");
        trans(&makefiletext, "tools/build/",     "tools\\build\\");
        trans(&makefiletext, "t/01-toolchain/",  "t\\01-toolchain\\");
        trans(&makefiletext, "t/01-toolchain",   "t\\01-toolchain");
        trans(&makefiletext, "t/02-components/", "t\\02-components\\");
        trans(&makefiletext, "t/02-components",  "t\\02-components");
    #endif
    #if defined( _MSC_VER )
        trans(&makefiletext, "$(OUTFILE) ",      "$(OUTFILE)");
    #endif
    printf("    %s: writing to %s\n", programfilename, outputfilename);
    squirt(makefiletext, outputfilename);
    free(makefiletext);
}


/* slurp */
/* Read an entire file into a string allocated on the heap */
char *
slurp(char * filename)
{
    FILE * infile;
    int linesize, textsize = 0;
    char linebuffer[LINEBUFFERSIZE];
    char * filetext = NULL;

    infile = fopen(filename, "r");
    while (fgets(linebuffer, LINEBUFFERSIZE, infile)) {
        linesize = strlen(linebuffer);
        filetext = textsize
            ? realloc(filetext, textsize+linesize+1)
            : malloc(linesize+1);     /* +1 for '\0' at end of string */
        strcpy(filetext+textsize, linebuffer);
        textsize += linesize;
    }
    return filetext;
}


/* squirt */
/* Write a string to a file */
void
squirt(char * text, char * filename)
{
    FILE * outfile;
    outfile = fopen(filename, "w");
    fputs(text, outfile);
    fclose(outfile);
}


/* trans */
/* Transliterate (find without a regular expression, then replace) */
/* text in a string */
void
trans(char ** text, char * search, char * replace)
{
    int textindex, searchlength, replacelength, foundcount = 0;
    int pass = 1, textlength, lengthdifference;
    char * textend, * found;
    textlength       = strlen(* text);
    searchlength     = strlen(search);
    replacelength    = strlen(replace);
    lengthdifference = replacelength - searchlength;
    textend          = (* text) + textlength; /* point to '\0' at end */
    /* To reallocate the text memory only once, if replace is longer */
    /* than search, first do a pass to count matches and resize the */
    /* text. */
    /* If the text will not become longer, skip the search pass. */
    if (lengthdifference <= 0) {
        pass = 2;  /* no need to count before replacing */
    }
    while (pass <= 2) {
        textindex = 0;
        while ( (found=strstr((* text)+textindex, search)) != NULL) {
            switch (pass) {
                case 1:
                    /* first pass is only for counting */
                    ++ foundcount;
                    textindex = found - (* text) + searchlength;
                    break;
                case 2:
                    /* do the replacement */
                    if (lengthdifference)
                        memmove(found+replacelength, found+searchlength,
                                    (textend-found)-searchlength+1);
                    memcpy(found,replace,replacelength);
                    textlength += lengthdifference;
                    textend    += lengthdifference;
                    textindex = (found - (* text)) + replacelength;
                    break;
            }
        }
        if ((pass==1) && foundcount) {
            /* The text will become longer, so re-allocate once only */
            * text = realloc(* text, textlength + 1
                                     + foundcount * lengthdifference );
        }
        ++pass;
    }
    /* Check whether the text should now be shorter, and make it so */
    if (lengthdifference < 0 && foundcount) {
        * text = realloc(* text, textlength + 1
                                 + foundcount * lengthdifference );
    }
}


/* main */
int
main(int argc, char * argv[])
{
    if (argc < 2) {
        fprintf(stderr, "Usage: %s path/to/Makefile.in path/to/Makefile\n", argv[0]);
        exit(1);
    }
    detection();
	config_set();  /* Figure out the configuration settings */
    makefile_convert(argv[0], argv[1], argv[2]);
    printf("Use '%s' to build and test 6model/c\n", config[MAKE_COMMAND]);
    return 0;
}

/*
 * TODO
 *
 * Explore more C compilers and toolchains
 * http://www.thefreecountry.com/compilers/cpp.shtml
 * lcc-win32 http://www.cs.virginia.edu/~lcc-win32/
 * Borland (Registration required)
 * Tiny C Compiler http://bellard.org/tcc/
 * OpenWatcom http://www.openwatcom.org/index.php/Download
 * Digital Mars http://www.digitalmars.com/download/freecompiler.html
 *
 */


/* See also: */
/* Autoconf http://www.gnu.org/software/autoconf */
/* Automake http://www.gnu.org/software/automake */

/* end of Configure.c */
