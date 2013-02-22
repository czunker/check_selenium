#!/bin/bash

JAVA_HOME=/opt/java/java5
DIR=$(/usr/bin/dirname ${0})
CLASSPATH=${DIR}/lib/commons-logging-1.1.1.jar:${DIR}/lib/httpclient-4.1.2.jar:${DIR}/lib/httpcore-4.1.3.jar:${DIR}/lib/commons-exec-1.1.jar:${DIR}/lib/json-20080701.jar:${DIR}/lib/guava-11.0.1.jar:${DIR}/lib/selenium-java-2.20.0.jar:${DIR}/lib/commons-cli-1.2.jar:${DIR}/lib/junit-4.8.1.jar:${DIR}/com/example/tests/.:${DIR}/lib/check-selenium.jar:${DIR}/lib/selenium-example-tests.jar

#echo $CLASSPATH

${JAVA_HOME}/bin/java -cp ${CLASSPATH} info.devopsabyss.CallSeleniumTest $@
