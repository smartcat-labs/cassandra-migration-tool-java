package io.smartcat.migration;

import java.util.LinkedHashSet;
import java.util.Set;

public class MigrationResources {

    private final Set<Migration> migrations = new LinkedHashSet<>();

    /**
     * Add Migration object to migration collection
     * 
     * @param migration Migration object
     */
    public void addMigration(final Migration migration) {
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
