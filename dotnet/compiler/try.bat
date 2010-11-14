@if exist x.exe del /Q x.exe
@if exist x.cs del /Q x.cs
@nmake /nologo all
@parrot compile.pir %1 > x.cs
@csc /nologo x.cs /reference:RakudoRuntime.dll /debug /warn:0
@echo ---
@x

