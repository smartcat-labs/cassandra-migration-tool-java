package io.smartcat.migration.test.logging;

import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Collections2.filter;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;

import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;

/**
 * Verifies logged events using a {@link TestLogger}.
 */
public class LoggerVerifier {

	private TestLogger testLogger;

	public LoggerVerifier(TestLogger aTestLogger) {
		testLogger = aTestLogger;
	}

	/**
	 * Asserts that the current logged events contains a given {@link LoggingEvent}
	 * @param loggingEvent the expected event
	 */
	public void assertLoggedEvent(LoggingEvent loggingEvent) {
		assertTrue(loggingEvents().contains(loggingEvent));
	}

	/**
	 * Asserts that the current logged events contains a given {@link LoggingEvent} 
	 * @param loggingEvent the expected event
	 */
	public void assertLoggedEventPartialArguments(LoggingEvent loggingEvent) {
		Predicate<LoggingEvent> hasMessage = new LoggingEventMessagePredicate(loggingEvent.getMessage());
		Predicate<LoggingEvent> hasArgumentSublist = new LoggingEventArgumentSublistPredicate(loggingEvent.getArguments());
		Collection<LoggingEvent> filteredEvents = filter(testLogger.getAllLoggingEvents(), and(hasMessage, hasArgumentSublist));
		assertFalse(filteredEvents.isEmpty());
	}

	private List<LoggingEvent> loggingEvents() {
		return testLogger.getLoggingEvents();
	}


}
