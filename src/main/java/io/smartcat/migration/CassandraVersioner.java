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
public class CassandraVersioner
    extends AbstractVersioner<Integer> {

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

    /**
     * Create Cassandra versioner.
     */
    public CassandraVersioner() {
    }


    @Override
    public void bootstrapSchemaVersionTable(Session session) {
        LOGGER.debug("Try to create schema version column family");
        session.execute(CREATE_SCHEMA_VERSION_CQL);
    }

    /**
     * Get current database version for given migration type with ALL consistency. Select one row since
     * migration history is saved ordered descending by timestamp. If there are no rows in the schema_version table,
     * return 0 as default database version. Data version is changed by executing migrations.
     *
     * @param session Active cassandra session
     * @param type Migration type
     * @return Database version for given type
     */
    public int getCurrentVersion(final Session session, final MigrationType type) {
        final Statement select = QueryBuilder.select().all().from(SCHEMA_VERSION_CF)
                .where(QueryBuilder.eq(TYPE, type.name())).limit(1).setConsistencyLevel(ConsistencyLevel.ALL);
        final ResultSet result = session.execute(select);

        final Row row = result.one();
        return row == null ? 0 : row.getInt(VERSION);
    }

    @Override
    public void markMigrationAsApplied(final Session session, final Migration migration) {
        final Statement insert = QueryBuilder.insertInto(SCHEMA_VERSION_CF).value(TYPE, migration.getType().name())
                .value(VERSION, migration.getVersion()).value(TIMESTAMP, System.currentTimeMillis())
                .value(DESCRIPTION, migration.getDescription()).setConsistencyLevel(ConsistencyLevel.ALL);

        session.execute(insert);
    }

}
