package io.smartcat.migration.migrations.schema;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import io.smartcat.migration.SchemaMigration;
import io.smartcat.migration.exceptions.MigrationException;

public class CreateItemByNumberAndExternalIdMigration extends SchemaMigration {

    public CreateItemByNumberAndExternalIdMigration(final int version) {
        super(version);
    }

    @Override
    public String getDescription() {
        return "Creates item table with number and external id as composite key";
    }

    @Override
    public void execute() throws MigrationException {
        try {
            final String statement =
                    "CREATE TABLE IF NOT EXISTS items_by_number_external_id (" +
                            "id uuid," +
                            "number text," +
                            "external_id uuid," +
                            "PRIMARY KEY ((number, external_id))" +
                    ") WITH COMMENT='Items by item number and external id';";
            executeWithSchemaAgreement(SimpleStatement.newInstance(statement));

        } catch (final Exception e) {
            throw new MigrationException("Failed to execute CreateItemsByNumberAndExternalIdMigration migration", e);
        }
    }

}
