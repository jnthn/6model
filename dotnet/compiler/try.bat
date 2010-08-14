@nmake /NOLOGO
@parrot compile.pir %1 > x.cs
@copy ..\runtime\bin\Debug\RakudoRuntime.dll .
@copy ..\runtime\bin\Debug\RakudoRuntime.pdb .
@csc x.cs /reference:RakudoRuntime.dll
@echo ---
@x

