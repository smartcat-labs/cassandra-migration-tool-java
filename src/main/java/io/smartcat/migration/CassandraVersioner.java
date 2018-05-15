package io.smartcat.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.util.Optional;


/**
 * Saves migrations with an Integer value.
 */
public class CassandraVersioner
    extends AbstractVersioner<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraVersioner.class);


    /**
     * Create Cassandra versioner.
     */
    public CassandraVersioner() {
    }


    @Override
    public String getVersionType() {
        return "int";
    }

    @Override
    protected Integer getVersion(Optional<Row> row) {
        return row.isPresent() ? row.get().getInt(VERSION) : 0;
    }

    /**
     * Get current database version for given migration type with ALL consistency. Select one row since
     * migration history is saved ordered descending by timestamp. If there are no rows in the schema_version table,
     * return 0 as default database version. Data version is changed by executing migrations.
     *
     * @deprecated use #getMostRecentUpdateApplied() - kept in place for API compatability
     * @param session Active cassandra session
     * @param type Migration type
     * @return Database version for given type
     */
    public int getCurrentVersion(final Session session, final MigrationType type) {
        return getMostRecentUpdateApplied(session, type);
    }

}
