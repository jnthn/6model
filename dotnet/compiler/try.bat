@copy ..\runtime\bin\Debug\RakudoRuntime.dll .
@copy ..\runtime\bin\Debug\RakudoRuntime.pdb .
@del x.exe
@del x.cs
@nmake /NOLOGO
@parrot compile.pir %1 > x.cs
@csc x.cs /reference:RakudoRuntime.dll /debug /warn:0
@echo ---
@x

