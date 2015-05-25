package com.smartcat.migration.migrations.data;

import com.datastax.driver.core.PreparedStatement;
import com.smartcat.migration.Migration;
import com.smartcat.migration.MigrationException;
import com.smartcat.migration.MigrationType;

/**
 * Example of data migration used for populating data in table. This shows use case when you need to populate data in
 * table fast. Can be used for test data, logic can be added to add near production like data in huge amounts.
 */
public class InsertBooksMigration extends Migration {

    public InsertBooksMigration(final MigrationType type, final int version) {
        super(type, version);
    }

    @Override
    public String getDescription() {
        return "Insert few books so we can show schema and data migration";
    }

    @Override
    public void execute() throws MigrationException {
        try {
            final PreparedStatement preparedStatement = session
                    .prepare("INSERT INTO books (name, author) VALUES (?, ? );");

            session.execute(preparedStatement.bind("Journey to the Center of the Earth", "Jules Verne"));
            session.execute(preparedStatement.bind("Fifty Shades of Grey", "E. L. James"));
            session.execute(preparedStatement.bind("Clean Code", "Robert C. Martin"));
        } catch (final Exception e) {
            throw new MigrationException("Failed to execute InsertBooksMigration migration", e);
        }
    }

}
