/* 01a-cc.c */
/* Verify that the C compiler and dynamic link loader are working */

#include <dlfcn.h>   /* dlclose dlerror dlopen dlsym */
#include <stdio.h>   /* fclose fopen fprintf perror printf */
#include <stdlib.h>  /* system */
#include <unistd.h>  /* unlink */


/* create_exe */
void
create_exe()
{
    FILE * testexe_sourcefile;
    int status;
    testexe_sourcefile = fopen("testexe.c", "w");
    if (testexe_sourcefile==NULL) {
        perror("01a-cc error 1: ");
        exit(1);
    }
    fprintf(testexe_sourcefile, /* instead of a "heredoc" in C */
        "/* testexe.c */\n"
        "#include <stdio.h>\n"
        "int\n"
        "main(int argc, char *argv[])\n"
        "{\n"
        "    printf(\"%%s - %%s\\n\", argv[1], argv[2]);\n"
        "    return 0;\n"
        "}\n"
    );
    if ( (status=fclose(testexe_sourcefile)) != 0) {
        perror("01a-cc error 2:");
        exit(2);
    }
    else
        printf("ok 1 - create testexe.c\n");
    status = system("cc -o testexe.exe testexe.c");
    if (status) {
        perror("01a-cc error 3:");
        exit(3);
    }
    else
        printf("ok 2 - compile testexe.c to testexe.exe\n");
}


/* run_exe */
void
run_exe()
{
    int status, c;
    FILE * childfile;
    char inputbuffer[80], * pbuffer, * pbufferend;
//  status = system("./testexe.exe 2 'run testexe.exe child'");
    /* start testexe.exe as a child process */
    childfile = popen("./testexe.exe foo bar", "r");
    if (childfile==NULL) {
        perror("01a-cc error 3:");
        exit(3);
    }
    else
        printf("ok 3 - popen testexe.exe parent\n");
    /* read the output of the child process into inputbuffer */
    pbuffer = inputbuffer;  pbufferend = inputbuffer + 80 - 1;
    while ((c=getc(childfile))!=EOF) {
        * pbuffer = c;
        if (++pbuffer >= pbufferend) {
            fprintf(stderr,"not ok 4 - buffer overflow\n");
            exit(4);
        }
    }
    * pbuffer = '\0';
    /* Compare the inputbuffer with the expected text */
    if (strcmp(inputbuffer,"foo - bar\n")!=0) {
        fprintf(stderr,"not ok 4 - verify testexe.exe output\n");
        fprintf(stderr,"got %s\n", inputbuffer);
    }
    else
        printf("ok 4 - verify testexe.exe output\n");
    /* close the child process file handle */
    if ( (status=pclose(childfile)) != 0) {
        perror("01a-cc error 3:");
        exit(3);
    }
    else
        printf("ok 5 - pclose testexe.exe\n");
}


/* remove_exe */
void
remove_exe()
{
    int status;
    status = unlink("testexe.c");
    if (status) {
        perror("01a-cc error 6:");
        exit(6);
    }
    else
        printf("ok 6 - unlink testexe.c\n");
    status = unlink("testexe.exe");
    if (status) {
        perror("01a-cc error 7:");
        exit(5);
    }
    else
        printf("ok 7 - unlink testexe.exe\n");
}


/* create_lib */
void
create_lib()
{
    FILE * testlib_sourcefile;
    int status;
    testlib_sourcefile = fopen("testlib.c", "w");
    if (testlib_sourcefile==NULL) {
        perror("01a-cc error 1: ");
        exit(1);
    }
    fprintf(testlib_sourcefile, /* instead of a "heredoc" in C */
        "/* testlib.c */\n"
        "#include <stdio.h>\n"
        "int\n"
        "testfunction(int testnumber, char * description)\n"
        "{\n"
        "    printf(\"ok %%d - %%s\\n\", testnumber, description);\n"
        "    return testnumber+42;\n"
        "}\n"
    );
    fclose(testlib_sourcefile);
    /* Can the following two system calls be combined?  Portably? */
    status = system("cc -c -fPIC -o testlib.o testlib.c");
    if (status) {
        perror("01a-cc error 8:");
        exit(8);
    }
    else
        printf("ok 8 - create testlib.c and testlib.o\n");
    status = system("cc -shared -s -o testlib.so testlib.o");
    if (status) {
        perror("01a-cc error 9:");
        exit(9);
    }
    else
        printf("ok 9 - create testlib.so\n");
}


/* load_lib */
void
load_lib()
{
    void * testlib;
    char * error;
    int (* pfunction)(int, char *), result;
    testlib = dlopen("./testlib.so", RTLD_LAZY);
    if (testlib==NULL) {
        fprintf(stderr, "01a-cc error 10: %s\n", dlerror());
        exit(10);
    }
    else
        printf("ok 10 - dlopen testlib.so\n");
    dlerror(); /* clear any possible error */
    pfunction = dlsym(testlib, "testfunction");
    if( (error = dlerror()) != NULL ) {
        fprintf(stderr, "01a-cc error 11: %s\n", error);
        exit(11);
    }
    else
        printf("ok 11 - dlsym testfunction\n");
    result = (* pfunction)(12, "call testfunction"); /* prints "ok 12" */
    if (result == 42+12)
        printf("ok 13 - testfunction result\n");
    else
        printf("not ok 13 - testfunction result\n");
    if ( (result = dlclose(testlib)) != 0 ) {
        fprintf(stderr, "01a-cc error 14: %s\n", error);
        exit(EXIT_FAILURE);
    }
    else
        printf("ok 14 - dlclose\n");
}


/* remove_lib */
void
remove_lib()
{
    int status;
    status = unlink("testlib.c");
    if (status) {
        perror("01a-cc error 15:");
        exit(15);
    }
    else
        printf("ok 15 - unlink testlib.c\n");
    status = unlink("testlib.o");
    if (status) {
        perror("01a-cc error 16:");
        exit(16);
    }
    else
        printf("ok 16 - unlink testlib.o\n");
    status = unlink("testlib.so");
    if (status) {
        perror("01a-cc error 17:");
        exit(17);
    }
    else
        printf("ok 17 - unlink testlib.so\n");
}


/* main */
int
main(int argc, char * argv[])
{
    printf("1..17\n"); /* tests */
    create_exe();  /* 1-2 make testexe.c and testexe.exe */
    run_exe();     /* 2-5 run testexe.exe */
    remove_exe();  /* 6-7 remove testexe.c and testexe.exe */
    create_lib();  /* 8-9 make testlib.c and testlib.so */
    load_lib();    /* 10-14 load and call testfunction */
    remove_lib();  /* 15-17 remove testexe.c .o and .so */
    return 0;
}
