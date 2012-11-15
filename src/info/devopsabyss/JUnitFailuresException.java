package info.devopsabyss;

import org.junit.runner.Result;


/**
 * @author mfinsterwalder
 * @since 2012-11-15 12:33
 */
public class JUnitFailuresException extends Exception {

	private Result result;

	public JUnitFailuresException(final Result result) {
		this.result = result;
	}

	@Override
	public String getMessage() {
		if (result.wasSuccessful()) {
			return "successful";
		} else {
			return result.getFailures().toString();
		}
	}

	public Result getResult() {
		return result;
	}
}
