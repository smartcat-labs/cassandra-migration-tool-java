package io.smartcat.migration;

/**
 * Data migration for migrations manipulating data.
 */
public abstract class DataMigration extends Migration {

    /**
     * Creates new data migration.
     * @param version Version of this data migration
     */
    public DataMigration(int version) {
        super(MigrationType.DATA, version);
    }
}
