package io.smartcat.migration.test.logging;

import java.util.List;

import com.google.common.base.Predicate;

import uk.org.lidalia.slf4jtest.LoggingEvent;

/**
 * Verifies if a given {@link LoggingEvent} argument list has an expected argument sublist, beginning in position 0.   
 */
public class LoggingEventArgumentSublistPredicate implements Predicate<LoggingEvent> {

	private List<Object> expectedArguments;

	public LoggingEventArgumentSublistPredicate(List<Object> arguments) {
		expectedArguments = arguments;
	}
	
	@Override
	public boolean apply(LoggingEvent input) {
		return input.getArguments().subList(0, expectedArguments.size()).equals(expectedArguments);
	}

}
