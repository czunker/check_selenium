package info.devopsabyss;

/*

 * This is a nagios plugin to integrate Selenium Test Cases into Nagios.
 * Copyright (C) 2010 Christian Zunker (devops.abyss@googlemail.com)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.SeleniumException;

public class CallSeleniumTest {

	private final int NAGIOS_RC_OK = 0;
	private final int NAGIOS_RC_WARNING = 1;
	private final int NAGIOS_RC_CRITICAL = 2;
	private final int NAGIOS_RC_UNKNOWN = 3;
		
	private final String NAGIOS_TEXT_OK = "OK";
	private final String NAGIOS_TEXT_WARNING = "WARNING";
	private final String NAGIOS_TEXT_CRITICAL = "CRITICAL";
	private final String NAGIOS_TEXT_UNKNOWN = "UNKNOWN";
	
	private Options options = null;

	//TODO: compile java files only when no class file found. this way the user does not have to compile the sources.
	//		what is when i get compile errors? => return NAGIOS_UNKNOWN?
	
	private long startTest(String className) throws NoClassDefFoundError, InvocationTargetException, NoSuchMethodException, Exception {

		long t1 = 0;
		long t2 = 0;
		Class seleniumTestClass = null;
		Object seleniumTest = null;
		
		// get class of TestCase and create an instance
		// must be outside try block, because tearDown should not be called
		//System.out.println(className);
		seleniumTestClass = Class.forName(className);
		
		
		try {
			Constructor seTestCon = seleniumTestClass.getConstructor(null);
			seleniumTest = seTestCon.newInstance(null);
			
			Method setUp = seleniumTestClass.getMethod("setUp");
			setUp.invoke(seleniumTest);
		
			
			// need classname without package name
			// because test method has this pattern: test<ClassName>
			//System.out.println(seleniumTestClass.getSimpleName());		
			// in case simplename does not work, this will help
			//System.out.println(seleniumTestClass.getName().replaceAll(seleniumTestClass.getPackage().getName() + ".", ""));
			Method callTest = seleniumTestClass.getMethod("test" + seleniumTestClass.getSimpleName());
			
			// messure time of test execution
			// this is also includes the startup time of the browser
			t1 = System.currentTimeMillis();
			callTest.invoke(seleniumTest);
			t2 = System.currentTimeMillis();
			
			// TODO: how to get the result of the test?
			// is AssertionFailedError the only possibility?
			// perhaps this is better: checkForVerificationErrors() 
			
			// TODO: parse log and try to find this:
			/*
			 09:22:33.394 INFO - Got result: OK on session 3920d36bff004d51a2c46a72914ee58f
			 09:22:33.397 INFO - Command request: isTextPresent[Masmann, ] on session 3920d36bff004d51a2c46a72914ee58f
			 09:22:34.354 INFO - Got result: OK,false on session 3920d36bff004d51a2c46a72914ee58f
			 09:22:34.358 INFO - Command request: testComplete[, ] on session 3920d36bff004d51a2c46a72914ee58f
			 */
			/*
			 * has to work somehow like this:
			 */
			
			/*
			Class seleneseTestCase2 = seleniumTestClass.getSuperclass();
			Field fields[] = seleneseTestCase2.getFields();
		    System.out.println("Access all the fields");
		    for (int i = 0; i < fields.length; i++){ 
		       System.out.println("Field Name: " + fields[i].getName()); 
		       System.out.println("accessible: " + fields[i].isAccessible()); 
		    }
		    */

			/*
			Field se = seleniumTestClass.getField("selenium");
			se.setAccessible(true);
			Object seleniumInstance = se.get(null);
			Class seleniumClass = se.getType();
			Method retrieveLog = seleniumClass.getMethod("retrieveLastRemoteControlLogs");
			String log = (String)retrieveLog.invoke(seleniumInstance);
			System.out.println(log);
			*/
		}
		catch (NullPointerException ex) {
			ex.printStackTrace();
			System.err.println("Couldn't find method setUp or test" + seleniumTestClass.getSimpleName() + " in your testcase!");
		}
		finally {
			// close browser windows
			Method tearDown = seleniumTestClass.getMethod("tearDown");
			tearDown.invoke(seleniumTest);
		}
		return (t2 - t1);
	}

	public static void main(String[] args) throws Exception {
		// TODO: Selenium Server host and port as parameters
		
		CallSeleniumTest seTest = new CallSeleniumTest();
		
		Option optionclass = new Option( "c", "class", true, "full classname of testcase (required)    " +				
													  " e.g. \"com.example.tests.GoogleSeleniumTestCase\"");
		//optiontype.setRequired(true);
		Option optionverbose = new Option( "v", "verbose", false, "show a lot of information (useful in case of problems)");
		Option optionhelp = new Option( "h", "help", false, "show this help screen");
		
		seTest.options = new Options();
		seTest.options.addOption(optionclass);
		seTest.options.addOption(optionverbose);
		seTest.options.addOption(optionhelp);
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;

		
		// TODO: verify baseURL
		// TODO: is there a possibility to verify classname?
		
		String output = seTest.NAGIOS_TEXT_UNKNOWN + " - Upps |;;;;";
		int nagios_rc = seTest.NAGIOS_RC_UNKNOWN;
		long time = 0;
		
		try {
			cmd = parser.parse(seTest.options, args);
			// has to be checked manually, otherwise you can't access the help message without specifying correct parameters
			if (cmd.hasOption("h") || cmd.getOptionValue("c") == null) {
				usage(seTest.options);
				System.exit(nagios_rc);
			}

			time = seTest.startTest(cmd.getOptionValue("c"));
			output = seTest.NAGIOS_TEXT_OK + " - " + cmd.getOptionValue("c") + " Test passed | ExecTime=" + time + "ms;;;;";
			nagios_rc = seTest.NAGIOS_RC_OK;
		}
		
		// there can't be a SeleniumException, because it will be wrapped by an InvocationTargetException because of reflection
		// not anymore with selenium server 2.20.0
		catch (SeleniumException ex) {
			if (cmd.hasOption("v")) {
				ex.printStackTrace();
			}
			output = seTest.NAGIOS_TEXT_CRITICAL + " - " + ex.getMessage() + "|";
			nagios_rc = seTest.NAGIOS_RC_CRITICAL;
		}
		catch (UnrecognizedOptionException ex) {
			output = seTest.NAGIOS_TEXT_UNKNOWN + " - " + "Parameter problems: " + ex.getMessage() + " |;;;;";
			nagios_rc = seTest.NAGIOS_RC_UNKNOWN;
			usage(seTest.options);
		}
		catch (ParseException ex) {
			output = seTest.NAGIOS_TEXT_UNKNOWN + " - " + "Parameter problems: " + ex.getMessage() + " |;;;;";
			nagios_rc = seTest.NAGIOS_RC_UNKNOWN;
			usage(seTest.options);
		}
		catch (NoClassDefFoundError ex) {
			if (cmd.hasOption("v")) {
				ex.printStackTrace();
			}
			output = seTest.NAGIOS_TEXT_UNKNOWN + " - " + cmd.getOptionValue("c") + ": " + ex.getMessage() + " |;;;;";
			nagios_rc = seTest.NAGIOS_RC_UNKNOWN;
		}
		catch (ClassNotFoundException ex) {
			output = seTest.NAGIOS_TEXT_UNKNOWN + " - " + cmd.getOptionValue("c") + ": Testcase class " + ex.getMessage() + " not found! |;;;;";
			nagios_rc = seTest.NAGIOS_RC_UNKNOWN;
		}
		catch (NoSuchMethodException ex) {
			output = seTest.NAGIOS_TEXT_UNKNOWN + " - " + cmd.getOptionValue("c") + ": Testcase method " + ex.getMessage() + " not found! |;;;;";
			nagios_rc = seTest.NAGIOS_RC_UNKNOWN;
		}
		catch (InvocationTargetException ex) {
			if (cmd.hasOption("v")) {
				ex.printStackTrace();
			}
			Throwable causeEx = ex.getCause();
			if (causeEx instanceof SeleniumException) {
				output = seTest.NAGIOS_TEXT_CRITICAL + " - " + cmd.getOptionValue("c") + ex.getCause() + " |;;;;";
				nagios_rc = seTest.NAGIOS_RC_CRITICAL;
			}
			else {
				output = seTest.NAGIOS_TEXT_UNKNOWN + " - " + cmd.getOptionValue("c") + ": Got some problems: " + ex.getCause() + " |;;;;";
				nagios_rc = seTest.NAGIOS_RC_UNKNOWN;
			}
		}
		catch (Exception ex) {
			if (cmd.hasOption("v")) {
				ex.printStackTrace();
			}
			output = seTest.NAGIOS_TEXT_CRITICAL + " - " + cmd.getOptionValue("c") + ": " + ex.getMessage() + " |;;;;";
			nagios_rc = seTest.NAGIOS_RC_CRITICAL;
		}
		finally {
			System.out.println(output);
			System.exit(nagios_rc);
		}
	}

	private static void usage(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("check_selenium", options);
		System.out.println("");
		System.out.println("This version of check_selenium was tested with:");
		System.out.println("  - selenium server 2.20.0");
		System.out.println("  - selenium ide 1.7.2");
		System.out.println("  - test case exported as JUnit 4 (Webdriver)");
		System.out.println("");
		System.out.println("Some example calls:");
		System.out.println(" ./check_selenium -c \"com.example.tests.GoogleSeleniumWebdriverTestCase\"");
		System.out.println(" ./check_selenium --class \"com.example.tests.GoogleSeleniumWebdriverTestCase\"");
	}
}
