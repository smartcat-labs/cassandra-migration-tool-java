package com.smartcat.migration;

import com.datastax.driver.core.Session;

/**
 * Abstract migration class that implements session DI and exposes required methods for execution
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Migration other = (Migration) obj;
        if (type != other.type)
            return false;
        if (version != other.version)
            return false;
        return true;
    }

}
