package io.smartcat.migration.migrations.schema;

import com.datastax.driver.core.SimpleStatement;

import io.smartcat.migration.Migration;
import io.smartcat.migration.MigrationType;
import io.smartcat.migration.exceptions.MigrationException;

/**
 * Example of schema migration which adds new column to existing table.
 */
public class AddBookISBNFieldMigration<T extends Comparable> extends Migration {

    public AddBookISBNFieldMigration(final T version) {
        super(MigrationType.SCHEMA, version);
    }

    @Override
    public String getDescription() {
        return "Alters books tables by adding ISBN column";
    }

    @Override
    public void execute() throws MigrationException {
        try {
            final String alterBooksAddISBNCQL = "ALTER TABLE books ADD isbn text;";

            executeWithSchemaAgreement(new SimpleStatement(alterBooksAddISBNCQL));

        } catch (final Exception e) {
            throw new MigrationException("Failed to execute AddBookISBNField migration", e);
        }
    }

}
