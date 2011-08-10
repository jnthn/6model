/* 01a-cc.c */
/* Verify that the C compiler and dynamic link loader are working */

/* This test will be redundant after more advanced parts are verified */
/* to work, because they will contain similar functionality.  That is */
/* why it is located in the 01-toolchain directory of optional tests. */
/* It is being retained to help do the following: */
/* - Diagnose regressions related to compilers and operating systems */
/* - Add new features to or refactor existing features in 6model/c */
/* - Build and run with other tools such as C++ compilers or LLVM */
/* - Port 6model/c to other operating systems such as Hurd or Plan 9 */

/* See also http://en.wikipedia.org/wiki/Dynamic_loading */

/* TODO */
/* - remove a few remaining temporary generated test files */
/* - make library code access main program data */
/* - make library code access main program code (callback) */

#include <stdio.h>   /* fclose fopen fprintf perror printf */
#include <stdlib.h>  /* system */

#if defined( _WIN32 )
    #include <windows.h>
    #define pclose _pclose
    #define popen  _popen
    #define EXT_EXE    ".exe"
    #define EXT_OBJ    ".obj"
    #define EXT_DYNLIB ".dll"
#else
    #include <dlfcn.h>   /* dlclose dlerror dlopen dlsym */
    #include <unistd.h>  /* unlink */
    #define EXT_EXE    ""
    #define EXT_OBJ    ".o"
    #define EXT_DYNLIB ".so"
#endif


/* create_exe */
void
create_exe()
{
    FILE * testexe_sourcefile;
    int status;

    /* test 1: create main program source file */
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
        exit(1);
    }
    printf("ok 1 - create testexe.c\n");

    /* test 2: compile the source file to an executable */
    #if defined( _MSC_VER )
        status = system("cl -WX -Fetestexe.exe -nologo testexe.c >nul");
        if (status==0)
            status = unlink("testexe" EXT_OBJ);
    #else
        status = system("gcc -o testexe testexe.c");
    #endif
    if (status) {
        perror("01a-cc compiling testexe.c error 2:");
        exit(2);
    }
    printf("ok 2 - compile testexe.c to testexe" EXT_EXE "\n");
}


/* run_exe */
void
run_exe()
{
    int status, c;
    FILE * childfile;
    char inputbuffer[80], * pbuffer, * pbufferend, * testcommand;
    #if defined( _WIN32 )
        testcommand = ".\\testexe foo bar";
    #else
        testcommand = "./testexe foo bar";
    #endif
    /* test 3: start testexe as a child process */
    childfile = popen(testcommand, "r");
    if (childfile==NULL) {
        perror("01a-cc error 3:");
        exit(3);
    }
    printf("ok 3 - popen testexe.exe parent\n");

    /* test 4: read the output of the child process into inputbuffer */
    pbuffer = inputbuffer;  pbufferend = inputbuffer + 80 - 1;
    while ((c=getc(childfile)) != EOF) {
        * pbuffer = c;
        if (++pbuffer >= pbufferend) {
            fprintf(stderr,"not ok 4 - buffer overflow\n");
            exit(4);
        }
    }
    * pbuffer = '\0';
    /* Compare the inputbuffer with the expected text */
    if (strcmp(inputbuffer, "foo - bar\n") != 0) {
        fprintf(stderr,"not ok 4 - verify testexe" EXT_EXE " output\n");
        fprintf(stderr,"got %s\n", inputbuffer);
    }
    printf("ok 4 - verify testexe" EXT_EXE " output\n");

    /* test 5: close the child process file handle */
    if ( (status=pclose(childfile)) != 0) {
        perror("01a-cc error 5:");
        exit(5);
    }
    printf("ok 5 - pclose testexe" EXT_EXE "\n");
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
    printf("ok 6 - remove testexe.c\n");
    status = unlink("testexe" EXT_EXE);
    if (status) {
        perror("01a-cc error 7:");
        exit(5);
    }
    printf("ok 7 - remove testexe" EXT_EXE "\n");
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
        #ifdef _WIN32
        "__declspec(dllexport)\n"
        #endif
        "int\n"
        "testfunction(int testnumber, char * description)\n"
        "{\n"
        "    printf(\"ok %%d - %%s\\n\", testnumber, description);\n"
        "    return testnumber+42;\n"
        "}\n"
    );
    fclose(testlib_sourcefile);
    #if defined( _WIN32 )
        #ifdef _MSC_VER
            status = system("cl -LD -WX -nologo testlib.c >nul"); /* Visual C++ */
        #else
            status = system("gcc -mdll -o testlib.dll testlib.c"); /* MinGW */
        #endif
    #else
        status = system("gcc -c -fPIC -o testlib.o testlib.c"); /* Unix */
        if (status==0) {
            status = system("cc -shared -s -o testlib.so testlib.o");
        }
        if (status==0) {
            status = system("rm testlib.o");
        }
    #endif
    if (status) {
        perror("01a-cc error 8:");
        exit(8);
    }
    printf("ok 8 - create testlib" EXT_DYNLIB "\n");
}


/* load_lib */
void
load_lib()
{
    void * testlib;
    char * error;
    int result;
    /* test 9: load the library */
    #if defined( _WIN32 )
        FARPROC pfunction;
        testlib = LoadLibrary("./testlib.dll");
        if (testlib==NULL) {
            fprintf(stderr, "01a-cc error 10a: LoadLibrary returned NULL\n");
            exit(9);
        }
    #else
        testlib = dlopen("./testlib.so", RTLD_LAZY);
        int (* pfunction)(int, char *);
        if (testlib==NULL) {
            fprintf(stderr, "01a-cc error 10: %s\n", dlerror());
            exit(9);
        }
    #endif
    printf("ok 9 - load testlib" EXT_DYNLIB "\n");

    #if defined( _WIN32 )
        pfunction = GetProcAddress(testlib, "testfunction");
    #else
        dlerror(); /* clear any possible error */
        pfunction = dlsym(testlib, "testfunction");
        if( (error = dlerror()) != NULL ) {
            fprintf(stderr, "01a-cc error 10: %s\n", error);
            exit(10);
        }
    #endif
    if (pfunction == NULL) {
        fprintf(stderr, "01a-cc error 10a: GetProcAddress returned NULL\n");
        exit(10);
    }
    printf("ok 10 - dlsym testfunction\n");
    result = (* pfunction)(11, "call testfunction"); /* prints "ok 12" */
    if (result == 42+11)
        printf("ok 12 - testfunction result\n");
    else
        printf("not ok 12 - testfunction result\n");
    #if defined( _WIN32 )
        result = ! FreeLibrary(testlib); /* returns 0 for failure! */
    #else
        result = dlclose(testlib);
    #endif
    if (result) {
        fprintf(stderr, "01a-cc error 13: %s\n", error);
        exit(EXIT_FAILURE);
    }
    printf("ok 13 - unload library\n");
}


/* remove_lib */
void
remove_lib()
{
    int status;
    status = unlink("testlib.c");
    if (status) {
        perror("01a-cc error 14:");
        exit(14);
    }
    printf("ok 14 - remove testlib.c\n");
    #if defined( _MSC_VER )
        status = unlink("testlib" EXT_OBJ);
        if (status) {
            perror("01a-cc error 15:");
            exit(15);
        }
        printf("ok 15 - remove testlib" EXT_OBJ "\n");
    #else
        printf("ok 15 - remove testlib # SKIPPED\n"); /* MinGW */
    #endif
    status = unlink("testlib" EXT_DYNLIB);
    if (status) {
        perror("01a-cc error 16:");
        exit(16);
    }
    printf("ok 16 - remove testlib" EXT_DYNLIB "\n");
}


/* main */
int
main(int argc, char * argv[])
{
    printf("# 01a-cc\n");
    printf("1..16\n"); /* tests */
    create_exe();  /* 1-2 make testexe.c and testexe.exe */
    run_exe();     /* 2-5 run testexe.exe */
    remove_exe();  /* 6-7 remove testexe.c and testexe.exe */
    create_lib();  /* 8-9 make testlib.c and testlib.so */
    load_lib();    /* 10-14 load and call testfunction */
    remove_lib();  /* 15-17 remove testexe.c .o and .so */
    return 0;
}
