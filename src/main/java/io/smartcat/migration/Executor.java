package io.smartcat.migration;

import com.datastax.oss.driver.api.core.CqlSession;

/**
 * Executor is a class which executes all the migration for given session.
 */
public class Executor {

    private Executor() {
    }

    /**
     * Execute all migrations in migration resource collection.
     *
     * @param session Datastax driver sesison object
     * @param resources Migration resources collection
     * @return Return success
     */
    public static boolean migrate(final CqlSession session, final MigrationResources resources) {
        return MigrationEngine.withSession(session).migrate(resources);
    }

}
