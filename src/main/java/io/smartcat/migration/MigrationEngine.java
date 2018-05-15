package io.smartcat.migration;

import io.smartcat.migration.exceptions.MigrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;

/**
 * Responsible for applying migrations to production - accepts as arguments an active
 * cassandra session and a VersionStrategy to determine what migrations to apply. Migrations
 * to apply are passed as arguments to the migration method.
 *
 */
public class MigrationEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationEngine.class);

    private final Session session;
    private final AbstractVersioner versioner;

    /**
     * Private constructor - use use MigrationEngine.Builder() instead.
     * @param session active cassandra session
     * @param versioner the versioner to use to interact with schema_verion table
     */
    private MigrationEngine(final Session session, final AbstractVersioner versioner) {
        this.session = session;
        this.versioner = versioner;
        versioner.bootstrapSchemaVersionTable(session);
    }

    /**
     * Builder for MigrationEngine, used to construct a MigrationEngine object.
     */
    public static class Builder {
        // See Josh Bloch's 'Effective Java' for this pattern

        private AbstractVersioner versioner = new CassandraVersioner();

        /**
         * Set the Versioner to use, defaults to CassandraVersioner.
         * @param versioner versioner to use
         * @return this
         */
        public Builder withVersioner(AbstractVersioner versioner) {
            this.versioner = versioner;
            return this;
        }

        /**
         * Construct a MigrationEngine object.
         * @param session active Cassandra migration
         * @return a MigrationEngine object
         */
        public MigrationEngine build(Session session) {

            return new MigrationEngine(session, versioner);
        }
    }

    /**
     * Method that executes all migration from migration resources that are higher version than db version. If
     * migration fails, method will exit.
     *
     * @param resources Collection of migrations to be executed
     * @return Success of migration
     */
    public boolean migrate(final MigrationResources resources) {
        LOGGER.debug("Start migration");

        for (final Migration migration : resources.getMigrations()) {
            final MigrationType type = migration.getType();
            final Comparable migrationVersion = migration.getVersion();
            final Object version = versioner.getMostRecentUpdateApplied(session, migration.getType());

            LOGGER.info("Db is version {} for type {}.", version, type.name());
            LOGGER.info("Compare {} migration version {} with description {}", type.name(), migrationVersion,
                    migration.getDescription());

            if (versioner.getUpdatesApplied(session, migration.getType()).contains(migration.getVersion())) {
                LOGGER.warn("Skipping migration [{}] with version {} since db already has it applied {}.",
                        migration.getDescription(), migrationVersion, version);
                continue;
            }

            migration.setSession(session);

            final long start = System.currentTimeMillis();
            LOGGER.info("Start executing migration to version {}.", migrationVersion);

            try {
                migration.execute();
            } catch (final MigrationException e) {
                LOGGER.error("Failed to execute migration version {}, exception {}!", migrationVersion,
                        e);
                LOGGER.debug("Exception stack trace: {}", e);
                return false;
            }

            final long end = System.currentTimeMillis();
            final long seconds = (end - start) / 1000;
            LOGGER.info("Migration [{}] to version {} finished in {} seconds.", migration.getDescription(),
                    migrationVersion, seconds);

            try {
                versioner.markMigrationAsApplied(session, migration);
            } catch (final Exception e) {
                LOGGER.error("Db schema update failed for migration version {}!", migrationVersion);
                return false;
            }
        }

        return true;
    }

    /**
     * Create migrator out of session fully prepared for doing migration of resources.
     * @deprecated preseved for compatability - do not use
     * @param session Datastax driver session object
     * @return migrator instance with versioner and session which can migrate resources
     */
    public static Migrator withSession(final Session session) {
        return new Migrator(session);
    }

    /**
     * Migrator handles migrations and errors.
     * @deprecated do not use - kept for compatability reasons. Use MigrationEngine directly
     */
    public static class Migrator
        extends MigrationEngine {

        /**
         * @deprecated do not use - use MigrationEngine.Builder() instead
         * @param session active cassandra session
         */
        public Migrator(Session session) {
            super(session, new CassandraVersioner());
        }
    }

}
