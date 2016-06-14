package io.smartcat.migration.migrations.data;

import java.util.List;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import io.smartcat.migration.DataMigration;
import io.smartcat.migration.exceptions.MigrationException;

public class PopulateItemByNumberAndExternalIdMigration extends DataMigration {

    public PopulateItemByNumberAndExternalIdMigration(final int version) {
        super(version);
    }

    @Override
    public String getDescription() {
        return "Create entry for each item in items by id table into new items by number and external id table";
    }

    @Override
    public void execute() throws MigrationException {
        try {
            final PreparedStatement preparedStatement =
                    session.prepare(
                            "INSERT INTO items_by_number_external_id (id, number, external_id) VALUES (?, ?, ?);");

            final List<Row> rows = session.execute(QueryBuilder.select().from("items_by_id").setFetchSize(1000)).all();
            for (Row row : rows) {
                session.execute(
                        preparedStatement.bind(row.getUUID("id"), row.getString("number"), row.getUUID("external_id")));
            }
        } catch (final Exception e) {
            throw new MigrationException("Failed to execute PopulateItemByNumberAndExternalId migration", e);
        }
    }
}
