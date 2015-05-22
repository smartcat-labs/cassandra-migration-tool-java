package com.smartcat.migration;

import com.datastax.driver.core.Session;

/**
 * Abstract migration class that implements session DI and exposes required methods for execution. Migrations are sorted
 * ascending by version and executed in the same manner.
 */
public abstract class Migration {

    private int version = -1;
    private MigrationType type = MigrationType.SCHEMA;
    protected Session session;

    public Migration(final MigrationType type, final int version) {
        this.type = type;
        this.version = version;
    }

    /**
     * Enables session injection into migration class
     * 
     * @param session
     */
    public void setSession(final Session session) {
        this.session = session;
    }

    /**
     * Returns migration type (schema or data)
     * 
     * @return Migration type
     */
    public MigrationType getType() {
        return this.type;
    }

    /**
     * Returns resulting database schema version of this migration
     * 
     * @return Resulting db schema version
     */
    public int getVersion() {
        return this.version;
    }

    /**
     * Returns migration description (for history purposes)
     * 
     * @return Migration description
     */
    public abstract String getDescription();

    /**
     * Executes migration implementation
     * 
     * @throws MigrationException
     */
    public abstract void execute() throws MigrationException;

}
