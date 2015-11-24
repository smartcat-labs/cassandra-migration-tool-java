package io.smartcat.migration;

public abstract class DataMigration extends Migration {

    public DataMigration(int version) {
        super(MigrationType.DATA, version);
    }
}
