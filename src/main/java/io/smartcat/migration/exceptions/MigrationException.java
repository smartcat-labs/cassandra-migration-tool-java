package io.smartcat.migration.exceptions;

/**
 * Domain exception for things that happen while migrating.
 */
public class MigrationException extends Exception {

    private static final long serialVersionUID = 939170349798471411L;

    /**
     * Create migration exception with provided message.
     * @param message Message for this exception
     */
    public MigrationException(final String message) {
        super(message);
    }

    /**
     * Create migration exception with provided message and original cause.
     * @param message Message for this exception
     * @param throwable Throwable wrapping original cause.
     */
    public MigrationException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
