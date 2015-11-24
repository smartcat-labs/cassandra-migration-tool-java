package io.smartcat.migration;

import java.util.LinkedHashSet;
import java.util.Set;

public class MigrationResources {

    private final Set<Migration> migrations = new LinkedHashSet<>();

    /**
     * Add data migration to migration collection
     * 
     * @param migration DataMigration implementation
     */
    public <T extends DataMigration> void addMigration(final T migration) {
        this.migrations.add(migration);
    }
    
    /**
     * Add schema migration to migration collection
     * 
     * @param migration SchemaMigration implementation
     */
    public <T extends SchemaMigration> void addMigration(final T migration) {
    	this.migrations.add(migration);
    }

    /**
     * Add Migration object collection to migration collection (set is used as internal collection so no duplicates will
     * be added and order will be preserved meaning that if migration was in collection on position it will stay on that
     * position)
     * 
     * @param migrations Migration object list
     */
    public void addMigrations(final Set<Migration> migrations) {
        this.migrations.addAll(migrations);
    }

    /**
     * Get all Migration objects sorted by order of insert
     * 
     * @return Sorted list of Migration objects
     */
    public Set<Migration> getMigrations() {
        return this.migrations;
    }

    /**
     * Get migration on particular position (position of inserting)
     * 
     * @param position of migration in collection
     * 
     * @return Migration
     */
    public Migration getMigration(final int position) {
        return (Migration) this.migrations.toArray()[position];
    }
}
