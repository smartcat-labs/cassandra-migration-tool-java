package io.smartcat.migration;

import com.datastax.driver.core.Session;

public class Executor {

    /**
     * Execute all migrations in migration resource collection
     * 
     * @param session Datastax driver sesison object
     * @param resources Migration resources collection
     * @return Return success
     */
    public static boolean Migrate(final Session session, final MigrationResources resources) {
        return MigrationEngine.withSession(session).migrate(resources);
    }

}
