package io.smartcat.migration;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import io.smartcat.migration.exceptions.MigrationException;
import io.smartcat.migration.exceptions.SchemaAgreementException;

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
     * @param session Session object
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
     */
    public abstract String getDescription();

	/**
     * Executes migration implementation
     *
     * @throws MigrationException exception
     */
    public abstract void execute() throws MigrationException;

    /**
     * Execute provided statement and checks if the schema migration has been propagated
     * to all nodes in the cluster. Use this method when executing schema migrations.
     * @param statement Statement to be executed
     * @throws SchemaAgreementException exception
     */
    protected void executeWithSchemaAgreement(Statement statement)
            throws SchemaAgreementException
    {
        ResultSet result = this.session.execute(statement);
        if (checkSchemaAgreement(result)) {
            return;
        }
        if (checkClusterSchemaAgreement()) {
            return;
        }

        throw new SchemaAgreementException(
                "Failed to propagate schema update to all nodes (schema agreement error)");
    }

    /**
     * Whether the cluster had reached schema agreement after the execution of this query.
     *
     * After a successful schema-altering query (ex: creating a table), the driver
     * will check if the cluster's nodes agree on the new schema version. If not,
     * it will keep retrying for a given delay (configurable via
     * {@link com.datastax.driver.core.Cluster.Builder#withMaxSchemaAgreementWaitSeconds(int)}).
     *
     * If this method returns {@code false}, clients can call {@link com.datastax.driver.core.Metadata#checkSchemaAgreement()}
     * later to perform the check manually.
     *
     * Note that the schema agreement check is only performed for schema-altering queries
     * For other query types, this method will always return {@code true}.
     *
     * @param resultSet Statement execution ResultSet
     * @return whether the cluster reached schema agreement, or {@code true} for a non
     * schema-altering statement.
     */
    protected boolean checkSchemaAgreement(ResultSet resultSet) {
        return resultSet.getExecutionInfo().isSchemaInAgreement();
    }

    /**
     * Checks whether hosts that are currently up agree on the schema definition.
     *
     * This method performs a one-time check only, without any form of retry; therefore
     * {@link com.datastax.driver.core.Cluster.Builder#withMaxSchemaAgreementWaitSeconds(int)}
     * does not apply in this case.
     *
     * @return {@code true} if all hosts agree on the schema; {@code false} if
     * they don't agree, or if the check could not be performed
     * (for example, if the control connection is down).
     */
    protected boolean checkClusterSchemaAgreement() {
        return this.session.getCluster().getMetadata().checkSchemaAgreement();
    }

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
