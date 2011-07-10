:: Configure.bat

:: Currently this script assumes Microsoft Visual C++ Express 2010.
:: The TODO comments below describe several future alternatives.

:: !!! FUD WARNING !!!
:: Microsoft thinks that C programmers should rewrite every call to,
:: for example, strcpy, replacing it with their more "secure"
:: replacement called strcpy_s, which takes additional paramaters to
:: avoid buffer overruns.  Microsoft's C compilers emit many loud
:: warnings containing weasel words such as "fopen ... may be unsafe".
:: http://msdn.microsoft.com/en-us/library/8ef0s5kh%28v=VS.100%29.aspx
:: Talk about a brazen lock-in attempt!  Since other target platforms
:: are not blessed with strcpy_s and so on, this project opts to
:: continue using the "older, less secure functions" and disable the
:: warnings with -D_CRT_SECURE_NO_WARNINGS

set opts=-Wall -D_CRT_SECURE_NO_WARNINGS

@echo off
:: echo Compiling tools/build/Configure.c to Configure.exe
cl %opts% -Fotools\build\Configure.obj -Fetools\build\Configure.exe tools\build\Configure.c
:: echo Starting tools\build\Configure.exe
del tools\build\Configure.obj
tools\build\Configure.exe tools\build\Makefile.in Makefile

:: Notes

:: Configure.bat or Configure.cmd?
:: There is almost no difference between the two file types (just something
:: subtle about errorlevel), and in 6model/c they work the same.  So on user
:: friendliness grounds, .bat reassures the reader that the script will do
:: only "simple" things.
:: You could argue it the other way - we use cmd.exe, not command.com, so we
:: should use the extension that command.com cannot handle.  Dunno...

:: TODO

:: Evaluate potential Win32 C compilers and development environments
:: http://www.thefreecountry.com/compilers/cpp.shtml
:: Most of them alias cc to their own filenames.

:: msysGit
:: The full install is a 39MB download instead of the 13MB Git install, but it expands to 1.3GB instead of
:: about 200MB, and includes MinGW, which includes GCC.

:: MinGW
:: http://www.mingw.org/
:: Also bundled with the Git full install.  It includes bash, so use Configure.sh rather than Configure.bat
:: The libraries do not include dlopen.

:: lcc-win32
:: http://www.cs.virginia.edu/~lcc-win32/

:: Microsoft Visual C++ Express Edition 1-2GB RAM, 3GB disk
:: Downloads a 3.2MB web installer.
:: http://www.microsoft.com/express/vc/ do not need optional SQL express.
:: Does install Windows Installer 4.5, .NET Framework 4, SQL Server Compact 3.5, Help Viewer 1.0
:: (download 146MB, disk space 2.3GB)

:: Borland
:: Registration required

:: Tiny C Compiler
:: http://bellard.org/tcc/

:: OpenWatCom
:: http://www.openwatcom.org/index.php/Download

:: Digital Mars
:: http://www.digitalmars.com/download/freecompiler.html
