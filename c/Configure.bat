:: Configure.bat

:: This script is written for Microsoft Visual C++ Express 2010 or GCC.

:: TODO
:: - Add a --help option and provide for more options in general

@echo off
:: Determine whether the C compiler is MSVC or GCC, by attempting to use
:: them in that order.
echo int main(int argc, char * argv[]) { return 0; } >tools\build\temp.c
cl -nologo -Fotools\build\temp.obj -Fetools\build\temp.exe tools\build\temp.c >nul 2>nul
if not errorlevel 1 goto :msvc
gcc -otools\build\temp.exe tools\build\temp.c
if not errorlevel 1 goto :gcc
cc tools\build\temp.c
if not errorlevel 1 goto :cc
echo Sorry! Cannot compile with eith cl, gcc or cc.  Please fix.
if exist tools\build\temp.exe del tools\build\temp.exe
if exist tools\build\temp.obj del tools\build\temp.obj
if exist tools\build\temp.c   del tools\build\temp.c
goto :end_of_script
:msvc
echo Detected Microsoft Visual C/C++ (cl.exe)
del tools\build\temp.obj tools\build\temp.exe tools\build\temp.c
set COMPILER=MSVC
goto :got_compiler
:gcc
echo Detected GNU Compiler Collection (gcc.exe)
del tools\build\temp.exe tools\build\temp.c
set COMPILER=GCC
goto :got_compiler
:cc

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
set opts_msvc=-nologo -Wall -DCC=MSVC -D_CRT_SECURE_NO_WARNINGS
set opts_gcc=-Wall -DCC=GCC
if "%COMPILER%"=="MSVC" cl %opts_msvc% -Fotools\build\Configure.obj -Fetools\build\Configure.exe tools\build\Configure.c
if "%COMPILER%"=="GCC" gcc %opts_gcc% -otools\build\Configure.exe tools\build\Configure.c
if errorlevel 1 goto :end_of_script
if exist tools\build\Configure.obj del tools\build\Configure.obj
:: echo Run tools\build\Configure.exe to create Makefile
tools\build\Configure.exe tools\build\Makefile.in Makefile

:end_of_script

:: Notes

:: Configure.bat or Configure.cmd?
:: There is almost no difference between the two file types (just something
:: subtle about errorlevel), and in 6model/c they work the same.  So on user
:: friendliness grounds, .bat reassures the reader that the script will do
:: only "simple" things.
:: You could argue it the other way - we use cmd.exe, not command.com, so we
:: should use the extension that command.com cannot handle.  Dunno...