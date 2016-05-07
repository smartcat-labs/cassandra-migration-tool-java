package io.smartcat.migration.test.logging;

import com.google.common.base.Predicate;

import uk.org.lidalia.slf4jtest.LoggingEvent;

/**
 * Verifies if a given {@link LoggingEvent} argument has an expected value.
 */
public class LoggingEventArgumentPredicate implements Predicate<LoggingEvent> {

	private int argumentIndex;
	private Object expectedValue;

	/**
	 * @param anIndex the index of the {@link LoggingEvent} argument checked. 
	 * @param aValue the expected value of the argument
	 */
	public LoggingEventArgumentPredicate(int anIndex, Object aValue) {
		argumentIndex = anIndex;
		expectedValue = aValue;
	}

	@Override
	public boolean apply(LoggingEvent input) {
		return input.getArguments().get(argumentIndex).equals(expectedValue);
	}

}
