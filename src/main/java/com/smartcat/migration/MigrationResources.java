package com.smartcat.migration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MigrationResources {

    private final List<Migration> migrations = new ArrayList<>();

    /**
     * Add Migration object to migration list
     * 
     * @param migration Migration object
     */
    public void addMigration(final Migration migration) {
        this.migrations.add(migration);
    }

    /**
     * Add Migration object list to migration list
     * 
     * @param migrations Migration object list
     */
    public void addMigrations(final List<Migration> migrations) {
        this.migrations.addAll(migrations);
    }

    /**
     * Get all Migration objects sorted by migration version
     * 
     * @return Sorted list of Migration objects
     */
    public List<Migration> getMigrations() {
        // Sort migrations by migration version
        Collections.sort(this.migrations, this.comparator);
        return this.migrations;
    }

    // Migration comparator implementation for sorting migrations by version
    private final Comparator<Migration> comparator = new Comparator<Migration>() {

        @Override
        public int compare(final Migration m1, final Migration m2) {
            final int version1 = m1.getVersion();
            final int version2 = m2.getVersion();

            if (version1 == version2) {
                throw new IllegalStateException("Migrations cannot have same version");
            }

            return m1.getVersion() - m2.getVersion();
        }

    };
}
