/* Configure.c */
/* Compiled and run by 6model/c/Configure.(sh|bat) */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define LINEBUFFERSIZE 128


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


/* subst */
/* Perform a global search and replace on a string */
void
subst(char ** text, char * search, char * replace)
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


/* main */
int
main(int argc, char * argv[])
{
    char * makefiletext;
    makefiletext = slurp(argv[1]);
    subst(&makefiletext, "# Makefile.in",    "# Makefile");
    subst(&makefiletext, "This is the file", "This is NOT the file");
    squirt(makefiletext, argv[2]);
    free(makefiletext);
    return 0;
}
