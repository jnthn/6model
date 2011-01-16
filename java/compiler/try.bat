parrot compile.pir %1 > RakudoOutput.java
javac -classpath ../runtime/RakudoRuntime.jar RakudoOutput.java
echo ---
java -classpath ../runtime/RakudoRuntime.jar;NQPSetting.jar;. RakudoOutput
