package io.smartcat.migration.migrations.data;

import java.util.List;

import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;

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

            final SimpleStatement select = QueryBuilder.selectFrom("items_by_id").all().limit(1000).build();
            final List<Row> rows = session.execute(select).all();
            for (Row row : rows) {
                session.execute(
                        preparedStatement.bind(row.getUuid("id"), row.getString("number"), row.getUuid("external_id")));
            }
        } catch (final Exception e) {
            throw new MigrationException("Failed to execute PopulateItemByNumberAndExternalId migration", e);
        }
    }
}
