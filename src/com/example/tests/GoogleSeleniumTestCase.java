package com.example.tests;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;

public class GoogleSeleniumTestCase extends SeleneseTestCase {
	public void setUp() throws Exception {
		setUp("http://www.google.com/", "*chrome");
	}
	public void testGoogleSeleniumTestCase() throws Exception {
		selenium.type("q", "selenium hq");
		verifyTrue(selenium.isTextPresent("Selenium web application testing system"));
		selenium.click("link=Selenium web application testing system");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("Get started with Selenium!"));
	}
}
