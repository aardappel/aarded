cls
cd src
del *.class r\*.class jas\*.class
javac -Xmaxerrs 20 -cp . -sourcepath . *.java r\*.java jas\*.java
java AardEd
cd ..
