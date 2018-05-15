package io.smartcat.migration;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Version Migrations with a String value assumed to be a timestamp a la
 * ruby-on-rails active record migrations, e.g: '20180514010203'.
 *
 * @author dalvizu
 */
public class DateStringVersioner
    extends AbstractVersioner<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateStringVersioner.class);

    private static final String SCHEMA_VERSION_CF = "schema_version";
    private static final String TYPE = "type";
    private static final String VERSION = "version";
    private static final String TIMESTAMP = "ts";
    private static final String DESCRIPTION = "description";

    private static final String CREATE_SCHEMA_VERSION_CQL = String.format("CREATE TABLE IF NOT EXISTS %s (",
            SCHEMA_VERSION_CF)
            + String.format("%s text,", TYPE)
            + String.format("%s text,", VERSION)
            + String.format("%s bigint,", TIMESTAMP)
            + String.format("%s text,", DESCRIPTION)
            + String.format("PRIMARY KEY (%s, %s)", TYPE, VERSION)
            + String.format(")  WITH CLUSTERING ORDER BY (%s DESC)", VERSION) + " AND COMMENT='Schema version';";

    /**
     * Constructor.
     */
    public  DateStringVersioner() {

    }

    /**
     * @param localDateTime - the date to format
     * @return a String of the given local date time suitable for saving to the database as a version
     */
    public static String getDateString(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    @Override
    public void bootstrapSchemaVersionTable(Session session) {
        LOGGER.debug("Try to create schema version column family");
        session.execute(CREATE_SCHEMA_VERSION_CQL);
    }

    @Override
    public void markMigrationAsApplied(Session session, Migration migration) {
        final Statement insert = QueryBuilder.insertInto(SCHEMA_VERSION_CF).value(TYPE, migration.getType().name())
                .value(VERSION, migration.getVersion()).value(TIMESTAMP, System.currentTimeMillis())
                .value(DESCRIPTION, migration.getDescription()).setConsistencyLevel(ConsistencyLevel.ALL);

        session.execute(insert);
    }

    @Override
    public Set<String> getUpdatesApplied(Session session, MigrationType migrationType) {
        final Statement statement = QueryBuilder.select().all().from(SCHEMA_VERSION_CF)
                .where(QueryBuilder.eq(TYPE, migrationType.name()))
                .setConsistencyLevel(ConsistencyLevel.ALL);
        final ResultSet resultSet = session.execute(statement);
        return resultSet.all().stream().map(row -> row.getString(VERSION)).collect(Collectors.toSet());
    }

    @Override
    public String getMostRecentUpdateApplied(Session session, MigrationType migrationType) {
        final Statement select = QueryBuilder.select().all().from(SCHEMA_VERSION_CF)
                .where(QueryBuilder.eq(TYPE, migrationType.name())).limit(1).setConsistencyLevel(ConsistencyLevel.ALL);
        final ResultSet result = session.execute(select);

        final Row row = result.one();
        return row == null ? "" : row.getString(VERSION);
    }
}
