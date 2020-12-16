package io.smartcat.migration.migrations.data;

import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import io.smartcat.migration.DataMigration;
import io.smartcat.migration.exceptions.MigrationException;

import java.util.UUID;

public class InsertInitialItemsMigration extends DataMigration {

    private final int count;

    public InsertInitialItemsMigration(final int count, final int version) {
        super(version);
        this.count = count;
    }

    @Override
    public String getDescription() {
        return "Populate items by id table with initial data";
    }

    @Override
    public void execute() throws MigrationException {
        try {
            final PreparedStatement preparedStatement =
                    session.prepare("INSERT INTO items_by_id (id, number, external_id) VALUES (?, ?, ?);");

            for (int i = 0; i < count; i++) {
                session.execute(preparedStatement.bind(UUID.randomUUID(), Integer.toString(i), UUID.randomUUID()));
            }
        } catch (final Exception e) {
            throw new MigrationException("Failed to execute InsertInitialItemsMigration migration", e);
        }

    }
}
