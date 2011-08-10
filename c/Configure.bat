:: Configure.bat

:: This script should work on a Microsoft Windows operating system with
:: either Microsoft Visual C++ or GNU Compiler Collection gcc.

:: TODO
:: - Add a --help option and provide for more options in general

@echo off
:: Determine whether the C compiler is cl (Microsoft C) or mingw32-gcc,
:: by attempting to use each one on a very small C program.
echo int main(int argc, char * argv[]) { return 0; } >tools\build\temp.c
cl -nologo -Fotools\build\temp.obj -Fetools\build\temp.exe tools\build\temp.c >nul 2>nul
if not errorlevel 1 goto cl
mingw32-gcc -otools\build\temp.exe tools\build\temp.c
if not errorlevel 1 goto mingw32-gcc
echo Sorry! Cannot compile with either cl or mingw32-gcc.  Please fix.
if exist tools\build\temp.exe del tools\build\temp.exe
if exist tools\build\temp.obj del tools\build\temp.obj
if exist tools\build\temp.c   del tools\build\temp.c
goto :end_of_script

:cl
echo Detected Microsoft Visual C/C++ (cl.exe)
del tools\build\temp.obj tools\build\temp.exe tools\build\temp.c
set COMPILER=cl
goto :got_compiler

:mingw32-gcc
echo Detected MinGW GCC (mingw32-gcc.exe)
del tools\build\temp.exe tools\build\temp.c
set COMPILER=mingw32-gcc
goto :got_compiler

:got_compiler
echo Compiling tools\build\Configure.c to tools\build\Configure.exe

:: !!! FUD WARNING !!!
:: Microsoft thinks that C programmers should rewrite every call to,
:: for example, strcpy, replacing it with their more "secure"
:: replacement called strcpy_s, which takes additional parameters to
:: avoid buffer overruns.  Microsoft's C compilers emit many loud
:: warnings containing weasel words such as "fopen ... may be unsafe".
:: http://msdn.microsoft.com/en-us/library/8ef0s5kh%28v=VS.100%29.aspx
:: Talk about a brazen lock-in attempt!  Since other target platforms
:: are not blessed with strcpy_s and so on, this project opts to
:: continue using the "older, less secure functions" and disable the
:: warnings with -D_CRT_SECURE_NO_WARNINGS
set opts_msc=-nologo -Wall -openmp -D_CRT_SECURE_NO_WARNINGS -wd4820 -wd4668 -wd4255
set opts_gcc=-Wall -fopenmp
if "%COMPILER%"=="cl" cl %opts_msvc% -Fotools\build\Configure.obj -Fetools\build\Configure.exe tools\build\Configure.c
if "%COMPILER%"=="mingw32-gcc" mingw32-gcc %opts_gcc% -otools\build\Configure.exe tools\build\Configure.c
if errorlevel 1 goto :end_of_script
if exist tools\build\Configure.obj del tools\build\Configure.obj
:: echo Run tools\build\Configure.exe to create Makefile
tools\build\Configure.exe tools\build\Makefile.in Makefile

:end_of_script

:: Notes

:: Configure.bat or Configure.cmd?
:: Windows XP and later handle the two file types in almost exactly the
:: same way (there is a minor detail about errorlevel).  In 6model/c
:: either file type would work the same.  So for user friendliness, .bat
:: reassures the user that the script does only simple things.  There is
:: the opposite rationale, that it needs cmd.exe not command.com, and
:: should use the extension that command.com cannot handle.  Dunno...
