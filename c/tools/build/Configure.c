/* Configure.c */
/* Compiled and run by 6model/c/Configure.(sh|bat) */

/*
 * TODO

 * Evaluate potential Win32 C compilers and development environments
 * http://www.thefreecountry.com/compilers/cpp.shtml
 * Most of them alias cc to their own filenames.

 * msysGit
 * The full install is a 39MB download instead of the 13MB Git install, but it expands to 1.3GB instead of
 * about 200MB, and includes MinGW, which includes GCC.

 * MinGW
 * http://www.mingw.org/
 * Also bundled with the Git full install.  It includes bash, so use Configure.sh rather than Configure.bat
 * The libraries do not include dlopen.

 * lcc-win32
 * http://www.cs.virginia.edu/~lcc-win32/

 * Microsoft Visual C++ Express Edition 1-2GB RAM, 3GB disk
 * Registration required to avoid de-activation after 30 days.  Downloads a 3.2MB web installer.
 * http://www.microsoft.com/express/vc/ do not need optional SQL express.
 * Also installs Windows Installer 4.5, .NET Framework 4, SQL Server Compact 3.5, Help Viewer 1.0
 * (download 146MB, disk space 2.3GB)

 * Borland
 * Registration required

 * Tiny C Compiler
 * http://bellard.org/tcc/

 * OpenWatCom
 * http://www.openwatcom.org/index.php/Download

 * Digital Mars
 * http://www.digitalmars.com/download/freecompiler.html

*/

#include <stdio.h>   /* fclose fgets FILE fopen fprintf printf stderr */
#include <stdlib.h>  /* exit free getenv malloc realloc */
#include <string.h>  /* memmove memcpy strcpy strlen strstr */


#define LINEBUFFERSIZE 128
/* Subscript names for configuration strings.  Almost like a hash ;) */
enum { CC, EXE, LDL, MAKE_COMMAND, OS_TYPE, OUT, RM_RF,
       CONFIG_END /* this one must always be last */ };
char * config[CONFIG_END] = {"", "", "", "", "", "", ""};
/* forward references to internal functions */
void detect(void);
char * slurp(char * filename);
void squirt(char * text, char * filename);
void trans(char ** text, char * search, char * replace);


/* config_set */
/* Use environment variables and other clues to assign values to */
/* members of the config[] array */
void
config_set(void)
{
    char * s;
    /* Operating system */
    if ((s=getenv("OS")) && strcmp(s,"Windows_NT")==0) { /* any Windows system */
	    config[OS_TYPE] = "Windows";
	    config[EXE] = ".exe";
    }
	else {  /* non Windows operating systems default to Unix settings */
	    config[OS_TYPE] = "Unix";
	    config[EXE] = "";
    }
    /* C compiler */
    if ((s=getenv("COMPILER")) && strcmp(s,"MSVC")==0) {
        config[CC] = "cl -DMSVC ";
	    config[OUT] = "-Fe";
	    config[RM_RF] = "del /F /Q /S";
    }
    if ((s=getenv("COMPILER")) && strcmp(s,"GCC")==0) {
        if ((s=getenv("OS")) && strcmp(s,"Windows_NT")==0)
            config[CC] = "cc -DGCC ";
        else
            config[CC] = "cc -DGCC -ldl ";
	    config[OUT] = "-o";
	    config[RM_RF] = "rm -rf";
    }
    /* Make utility */
    if ((s=getenv("COMPILER")) && strcmp(s,"GCC")==0) {
	    config[MAKE_COMMAND] = "make";
    } else {
	    config[MAKE_COMMAND] = "nmake";
    }
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
    trans(&makefiletext, "@out@",       config[OUT]);
    trans(&makefiletext, "@rm_rf@",     config[RM_RF]);
    if (strcmp(config[OS_TYPE], "Windows")==0 && strcmp(getenv("COMPILER"),"MSVC")==0) {
        trans(&makefiletext, "tools/build/",     "tools\\build\\");
        trans(&makefiletext, "t/01-toolchain/",  "t\\01-toolchain\\");
        trans(&makefiletext, "t/01-toolchain",   "t\\01-toolchain");
        trans(&makefiletext, "t/02-components/", "t\\02-components\\");
        trans(&makefiletext, "t/02-components",  "t\\02-components");
    }
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
	config_set();  /* Figure out the configuration settings */
    makefile_convert(argv[0], argv[1], argv[2]);
    printf("Use '%s' to build and test 6model/c\n", config[MAKE_COMMAND]);
    return 0;
}

/* end of Configure.c */
