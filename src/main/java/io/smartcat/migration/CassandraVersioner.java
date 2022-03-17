package io.smartcat.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

/**
 * Class responsible for version management.
 */
public class CassandraVersioner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraVersioner.class);

    private static final String SCHEMA_VERSION_CF = "schema_version";
    private static final String TYPE = "type";
    private static final String VERSION = "version";
    private static final String TIMESTAMP = "ts";
    private static final String DESCRIPTION = "description";

    private static final String CREATE_SCHEMA_VERSION_CQL = String.format("CREATE TABLE IF NOT EXISTS %s (",
            SCHEMA_VERSION_CF)
            + String.format("%s text,", TYPE)
            + String.format("%s int,", VERSION)
            + String.format("%s bigint,", TIMESTAMP)
            + String.format("%s text,", DESCRIPTION)
            + String.format("PRIMARY KEY (%s, %s)", TYPE, VERSION)
            + String.format(")  WITH CLUSTERING ORDER BY (%s DESC)", VERSION) + " AND COMMENT='Schema version';";

    private final Session session;

    /**
     * Create Cassandra versioner for active session.
     * @param session Active Cassandra session
     */
    public CassandraVersioner(final Session session) {
        this.session = session;

        createSchemaVersion();
    }

    private void createSchemaVersion() {
        LOGGER.debug("Try to create schema version column family");
        this.session.execute(CREATE_SCHEMA_VERSION_CQL);
    }

    /**
     * Get current database version for given migration type with ALL consistency. Select one row since migration
     * history is saved ordered descending by timestamp. If there are no rows in the schema_version table, return 0 as
     * default database version. Data version is changed by executing migrations.
     *
     * @param type Migration type
     * @param consistencyLevel Desired consistency level
     * @return Database version for given type
     */
    public int getCurrentVersion(final MigrationType type, final ConsistencyLevel consistencyLevel) {
        final Statement select = QueryBuilder.select().all().from(SCHEMA_VERSION_CF)
                .where(QueryBuilder.eq(TYPE, type.name())).limit(1).setConsistencyLevel(consistencyLevel);
        final ResultSet result = session.execute(select);

        final Row row = result.one();
        return row == null ? 0 : row.getInt(VERSION);
    }

    /**
     * Update current database version to the migration version. This is executed after migration success.
     *
     * @param migration Migration that updated the database version
     * @return Success of version update
     */
    public boolean updateVersion(final Migration migration) {
        final Statement insert = QueryBuilder.insertInto(SCHEMA_VERSION_CF).value(TYPE, migration.getType().name())
                .value(VERSION, migration.getVersion()).value(TIMESTAMP, System.currentTimeMillis())
                .value(DESCRIPTION, migration.getDescription()).setConsistencyLevel(ConsistencyLevel.ALL);

        try {
            session.execute(insert);
            return true;
        } catch (final Exception e) {
            LOGGER.error("Failed to execute update version statement", e);
            return false;
        }
    }
}
