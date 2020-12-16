package io.smartcat.migration;

/**
 * Schema migration for migrations manipulating schema.
 */
public abstract class SchemaMigration extends Migration {

    /**
     * Create new schema migration with provided version.
     * @param version Version of this schema migration
     */
    protected SchemaMigration(int version) {
        super(MigrationType.SCHEMA, version);
    }
}
