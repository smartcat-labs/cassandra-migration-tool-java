package io.smartcat.migration.exceptions;

public class SchemaAgreementException extends MigrationException {

    private static final long serialVersionUID = 4672095868449483293L;

    public SchemaAgreementException(final String message) {
        super(message);
    }

    public SchemaAgreementException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
