package io.smartcat.migration;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractVersioner.class);

    /**
     * Name of the schema_version table.
     */
    protected static final String SCHEMA_VERSION_CF = "schema_version";

    /**
     * Name of the schema_version table.
     */
    protected static final String TYPE = "type";

    /**
     * Name of the version column of the schema_version table.
     */
    protected static final String VERSION = "version";

    /**
     * Name of the version column of the schema_version table.
     */
    protected static final String TIMESTAMP = "ts";

    /**
     * Name of the description table.
     */
    protected static final String DESCRIPTION = "description";

    /**
     * @return the CQL version type in the schema_version table, used to create the table
     */
    public abstract String getVersionType();

    /**
     * @param row - cassandra row containing a version column
     * @return the java type, or the default type if no row found
     */
    protected abstract T getVersion(Optional<Row> row);

    /**
     *
     * @return CQL necessary to create the schema_version table
     */
    protected String getCreateSchemaVersionTableStatement() {
        return String.format("CREATE TABLE IF NOT EXISTS %s (",
                SCHEMA_VERSION_CF)
                + String.format("%s text,", TYPE)
                + String.format("%s %s,", VERSION, getVersionType())
                + String.format("%s bigint,", TIMESTAMP)
                + String.format("%s text,", DESCRIPTION)
                + String.format("PRIMARY KEY (%s, %s)", TYPE, VERSION)
                + String.format(")  WITH CLUSTERING ORDER BY (%s DESC)", VERSION) + " AND COMMENT='Schema version';";
    }


    /**
     * Create the table responsible for storing information about migrations already applied.
     * @param session active cassandra session
     */
    public void bootstrapSchemaVersionTable(Session session) {
        LOGGER.debug("Try to create schema version column family");
        session.execute(getCreateSchemaVersionTableStatement());
    }

    /**
     * Update current database version to the migration version. This is executed after migration success.
     *
     * @param session active cassandra session
     * @param migration Migration that updated the database version
     */
    public void markMigrationAsApplied(final Session session, final Migration migration) {
        final Statement insert = QueryBuilder.insertInto(SCHEMA_VERSION_CF).value(TYPE, migration.getType().name())
                .value(VERSION, migration.getVersion()).value(TIMESTAMP, System.currentTimeMillis())
                .value(DESCRIPTION, migration.getDescription()).setConsistencyLevel(ConsistencyLevel.ALL);

        session.execute(insert);
    }

    /**
     * @param session active cassandra session
     * @param migrationType type of migration
     * @return a set of migration versions which have not yet been applied
     */
    public Set<T> getUpdatesApplied(Session session, MigrationType migrationType) {
        final Statement statement = QueryBuilder.select().all().from(SCHEMA_VERSION_CF)
                .where(QueryBuilder.eq(TYPE, migrationType.name()))
                .setConsistencyLevel(ConsistencyLevel.ALL);
        final ResultSet resultSet = session.execute(statement);
        return resultSet.all().stream().map(row -> getVersion(Optional.of(row))).collect(Collectors.toSet());
    }

    /**
     * @param session active cassandra session
     * @param migrationType type of migration
     * @return the version of the most recently applied update
     */
    public T getMostRecentUpdateApplied(Session session, MigrationType migrationType) {
        final Statement select = QueryBuilder.select().all().from(SCHEMA_VERSION_CF)
                .where(QueryBuilder.eq(TYPE, migrationType.name())).limit(1).setConsistencyLevel(ConsistencyLevel.ALL);
        final ResultSet result = session.execute(select);

        final Row row = result.one();
        return getVersion(Optional.ofNullable(row));
    }

}
