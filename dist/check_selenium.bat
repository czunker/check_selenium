set JAVA_HOME=c:\java\java5
set CLASSPATH=lib\selenium-java-client-driver.jar;lib\junit-4.8.1.jar;lib\commons-cli-1.2.jar;lib\check-selenium.jar;com\example\tests\.;lib\selenium-example-tests.jar

%JAVA_HOME%/bin/java -cp %CLASSPATH% info.devopsabyss.CallSeleniumTest %*
