package com.smartcat.migration.migrations.schema;

import com.datastax.driver.core.SimpleStatement;
import com.smartcat.migration.Migration;
import com.smartcat.migration.MigrationException;
import com.smartcat.migration.MigrationType;

/**
 * Example of schema migration which adds new column to existing table.
 */
public class AddBookGenreFieldMigration extends Migration {

    public AddBookGenreFieldMigration(final MigrationType type, final int version) {
        super(type, version);
    }

    @Override
    public String getDescription() {
        return "Alters books tables by adding genre column";
    }

    @Override
    public void execute() throws MigrationException {
        try {
            final String alterBooksAddGenreCQL = "ALTER TABLE books ADD genre text;";

            this.session.execute(new SimpleStatement(alterBooksAddGenreCQL));

        } catch (final Exception e) {
            throw new MigrationException("Failed to execute AddBookGenreField migration", e);
        }
    }

}
