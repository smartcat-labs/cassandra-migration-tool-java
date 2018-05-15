package io.smartcat.migration.migrations.schema;

import com.datastax.driver.core.SimpleStatement;
import io.smartcat.migration.Migration;
import io.smartcat.migration.MigrationType;
import io.smartcat.migration.exceptions.MigrationException;

public class CreateItemByNumberAndExternalIdMigration<T extends Comparable> extends Migration {

    public CreateItemByNumberAndExternalIdMigration(final T version) {
        super(MigrationType.SCHEMA, version);
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
            executeWithSchemaAgreement(new SimpleStatement(statement));

        } catch (final Exception e) {
            throw new MigrationException("Failed to execute CreateItemsByNumberAndExternalIdMigration migration", e);
        }
    }

}
