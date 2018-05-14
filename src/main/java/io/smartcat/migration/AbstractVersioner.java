package io.smartcat.migration;

import com.datastax.driver.core.Session;

import java.util.Set;

/**
 * Class describing the responsibilities for versioning schema upgrades: mark them
 * as applied in a table, create the table if it doesn't exist, determine which
 * updates have already been applied, and determine which updates must be applied.
 *
 * T - the key of the database schema upgrade, i.e an integer
 *
 * @param <T> the type of the column to use as version in the database schema upgrade
 * @author dalvizu
 */
public abstract class AbstractVersioner<T> {

    /**
     * Create the table responsible for storing information about migrations already applied.
     * @param session active cassandra session
     */
    public abstract void bootstrapSchemaVersionTable(Session session);

    /**
     * Update current database version to the migration version. This is executed after migration success.
     *
     * @param session active cassandra session
     * @param migration Migration that updated the database version
     */
    public abstract void markMigrationAsApplied(Session session, Migration migration);

    /**
     * @param session active cassandra session
     * @param migrationType type of migration
     * @return a set of migration versions which have not yet been applied
     */
    public abstract Set<T> getUpdatesApplied(Session session, MigrationType migrationType);

    /**
     * @param session active cassandra session
     * @param migrationType type of migration
     * @return the version of the most recently applied update
     */
    public abstract T getMostRecentUpdateApplied(Session session, MigrationType migrationType);


}
