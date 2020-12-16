package io.smartcat.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;
import static com.datastax.oss.driver.api.querybuilder.relation.Relation.column;

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

    private final CqlSession session;

    /**
     * Create Cassandra versioner for active session.
     * @param session Active Cassandra session
     */
    public CassandraVersioner(final CqlSession session) {
        this.session = session;

        createSchemaVersion();
    }

    private void createSchemaVersion() {
        LOGGER.debug("Try to create schema version column family");
        this.session.execute(CREATE_SCHEMA_VERSION_CQL);
    }

    /**
     * Get current database version for given migration type with ALL consistency. Select one row since
     * migration history is saved ordered descending by timestamp. If there are no rows in the schema_version table,
     * return 0 as default database version. Data version is changed by executing migrations.
     *
     * @param type Migration type
     * @return Database version for given type
     */
    public int getCurrentVersion(final MigrationType type) {
        final SimpleStatement select  = QueryBuilder.selectFrom(SCHEMA_VERSION_CF).all()
                .where(column(TYPE).isEqualTo(literal(type.name()))).limit(1)
                .builder().setConsistencyLevel(DefaultConsistencyLevel.ALL).build();
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
        final SimpleStatement insert = QueryBuilder.insertInto(SCHEMA_VERSION_CF)
                .value(TYPE, literal(migration.getType().name()))
                .value(VERSION, literal(migration.getVersion()))
                .value(TIMESTAMP, literal(System.currentTimeMillis()))
                .value(DESCRIPTION, literal(migration.getDescription()))
                .builder().setConsistencyLevel(DefaultConsistencyLevel.ALL)
                .build();
        try {
            session.execute(insert);
            return true;
        } catch (final Exception e) {
            LOGGER.error("Failed to execute update version statement", e);
            return false;
        }
    }
}
