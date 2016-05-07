package io.smartcat.migration.test.logging;

import com.google.common.base.Predicate;

import uk.org.lidalia.slf4jtest.LoggingEvent;

/**
 * Verifies if a given {@link LoggingEvent} message has an expected value.
 */
public class LoggingEventMessagePredicate implements Predicate<LoggingEvent> {
	private String expectedMessage;

	public LoggingEventMessagePredicate(String aMessage) {
		expectedMessage = aMessage;
	}

	public boolean apply(LoggingEvent event) {
		return event.getMessage().equals(expectedMessage);
	}
}
