package io.smartcat.migration.migrations.schema;

import com.datastax.driver.core.SimpleStatement;

import io.smartcat.migration.exceptions.MigrationException;
import io.smartcat.migration.SchemaMigration;

/**
 * Example of schema migration which adds new column to existing table.
 */
public class AddBookGenreFieldMigration extends SchemaMigration {

    public AddBookGenreFieldMigration(final int version) {
        super(version);
    }

    @Override
    public String getDescription() {
        return "Alters books tables by adding genre column";
    }

    @Override
    public void execute() throws MigrationException {
        try {
            final String alterBooksAddGenreCQL = "ALTER TABLE books ADD genre text;";

            executeWithSchemaAgreement(new SimpleStatement(alterBooksAddGenreCQL));

        } catch (final Exception e) {
            throw new MigrationException("Failed to execute AddBookGenreField migration", e);
        }
    }

}
