@echo off

echo Installing libraries...

call mvn install:install-file -Dpackaging=jar -Dversion=1.0.0     -DgroupId=com.x256n.core   -DartifactId=msm-guide-core               -Dfile=msm-guide-core-1.0.0.jar

 pause