package io.smartcat.migration;

public abstract class SchemaMigration extends Migration {

    public SchemaMigration(int version) {
        super(MigrationType.SCHEMA, version);
    }
}
