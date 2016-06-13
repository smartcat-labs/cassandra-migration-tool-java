package io.smartcat.migration.exceptions;

/**
 * Schema agreement exception which wraps exceptions while schema is propagated on all nodes.
 */
public class SchemaAgreementException extends MigrationException {

    private static final long serialVersionUID = 4672095868449483293L;

    /**
     * Create schema agreement exception with provided message.
     * @param message Message for this exception
     */
    public SchemaAgreementException(final String message) {
        super(message);
    }

    /**
     * Create schema agreement exception with provided message and original cause.
     * @param message Message for this exception
     * @param throwable Throwable wrapping original cause
     */
    public SchemaAgreementException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
