package io.smartcat.migration.exceptions;

public class MigrationException extends Exception {

	private static final long serialVersionUID = 939170349798471411L;

	public MigrationException(final String message) {
        super(message);
    }

    public MigrationException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
