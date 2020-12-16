package io.smartcat.migration.migrations.schema;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;

import io.smartcat.migration.SchemaMigration;
import io.smartcat.migration.exceptions.MigrationException;

/**
 * Example of schema migration which adds new column to existing table.
 */
public class AddBookISBNFieldMigration extends SchemaMigration {

    public AddBookISBNFieldMigration(final int version) {
        super(version);
    }

    @Override
    public String getDescription() {
        return "Alters books tables by adding ISBN column";
    }

    @Override
    public void execute() throws MigrationException {
        try {
            final String alterBooksAddISBNCQL = "ALTER TABLE books ADD isbn text;";

            executeWithSchemaAgreement(SimpleStatement.newInstance(alterBooksAddISBNCQL));

        } catch (final Exception e) {
            throw new MigrationException("Failed to execute AddBookISBNField migration", e);
        }
    }

}
