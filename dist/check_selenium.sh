#!/bin/bash

JAVA_HOME=/opt/java/java5
DIR=$(/usr/bin/dirname ${0})
CLASSPATH=${DIR}/lib/selenium-java-client-driver.jar:${DIR}/lib/commons-cli-1.2.jar:${DIR}/lib/junit-4.8.1.jar:${DIR}/com/example/tests/.:${DIR}/lib/check-selenium.jar:${DIR}/lib/selenium-example-tests.jar

${JAVA_HOME}/bin/java -cp ${CLASSPATH} info.devopsabyss.CallSeleniumTest $@
