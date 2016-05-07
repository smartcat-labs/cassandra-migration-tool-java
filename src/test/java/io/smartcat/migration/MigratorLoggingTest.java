package io.smartcat.migration;

import static uk.org.lidalia.slf4jtest.LoggingEvent.info;
import static uk.org.lidalia.slf4jtest.LoggingEvent.warn;

import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import io.smartcat.migration.MigrationEngine.Migrator;
import io.smartcat.migration.migrations.schema.AddBookGenreFieldMigration;
import io.smartcat.migration.migrations.schema.AddBookISBNFieldMigration;
import io.smartcat.migration.test.logging.LoggerVerifier;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

/**
 * Tests the logging events of {@link Migrator}.
 */
public class MigratorLoggingTest extends BaseTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(MigrationEngineBooksTest.class);

	private static final String CONTACT_POINT = "localhost";
	private static final int PORT = 9142;
	private static final String KEYSPACE = "migration_test_books";
	private static final String CQL = "books.cql";

	private static Session session;
	private static Cluster cluster;

	// allows to assert on slf4j logging events
	private TestLogger testLogger = TestLoggerFactory.getTestLogger(MigrationEngine.class);
	private LoggerVerifier loggerVerifier = new LoggerVerifier(testLogger);

	private CassandraVersioner versioner;
	private Migrator migrator;

	@BeforeClass
	public static void init() throws Exception {
		LOGGER.info("Starting embedded cassandra server");
		EmbeddedCassandraServerHelper.startEmbeddedCassandra("another-cassandra.yaml");

		LOGGER.info("Connect to embedded db");
		cluster = Cluster.builder().addContactPoints(CONTACT_POINT).withPort(PORT).build();
		session = cluster.connect();
	}

	@Before
	public void setUp() throws Exception {
		LOGGER.info("Initialize keyspace");
		final CQLDataLoader cqlDataLoader = new CQLDataLoader(session);
		cqlDataLoader.load(new ClassPathCQLDataSet(CQL, false, true, KEYSPACE));

		versioner = new CassandraVersioner(session);
		migrator = MigrationEngine.withSession(session);

		testLogger.clearAll();
	}

	@AfterClass
	public static void tearDown() {
		if (cluster != null) {
			cluster.close();
			cluster = null;
		}
	}

	@Test
	public void executeSingleMigration() throws Exception {
		Migration migration = new AddBookGenreFieldMigration(1);

		final MigrationResources resources = new MigrationResources();
		resources.addMigration(migration);

		migrator.migrate(resources);

		loggerVerifier.assertLoggedEvent(startedMigrationLoggingEvent(migration));
		assertSuccessMigrationLoggingEvent(migration);
	}

	@Test
	public void executeTwoMigrations() throws Exception {
		final MigrationResources resources = new MigrationResources();
		resources.addMigration(new AddBookGenreFieldMigration(1));
		resources.addMigration(new AddBookISBNFieldMigration(2));

		migrator.migrate(resources);

		for (Migration migration : resources.getMigrations()) {
			assertStartedMigrationLoggingEvent(migration);
			assertSuccessMigrationLoggingEvent(migration);
		}
	}

	@Test
	public void skipOlderMigrationAddedToMigrationPlanAfterNewerMigration() {
		Migration olderMigration = new AddBookGenreFieldMigration(1);
		AddBookGenreFieldMigration newerMigration = new AddBookGenreFieldMigration(olderMigration.getVersion() + 1);

		final MigrationResources resources = new MigrationResources();
		resources.addMigration(newerMigration);
		resources.addMigration(olderMigration);

		migrator.migrate(resources);

		loggerVerifier.assertLoggedEvent(skippedMigrationLoggingEvent(olderMigration));
	}

	private void assertStartedMigrationLoggingEvent(Migration migration) {
		loggerVerifier.assertLoggedEvent(startedMigrationLoggingEvent(migration));
	}

	private LoggingEvent startedMigrationLoggingEvent(Migration migration) {
		return info("Start executing migration to version {}.", migration.getVersion());
	}

	private void assertSuccessMigrationLoggingEvent(Migration migration) {
		loggerVerifier.assertLoggedEventPartialArguments(sucessMigrationLoggingEvent(migration));
	}

	private LoggingEvent sucessMigrationLoggingEvent(Migration migration) {
		String expectedMessage = "Migration [{}] to version {} finished in {} seconds.";
		return info(expectedMessage, migration.getDescription(), migration.getVersion());
	}

	private LoggingEvent skippedMigrationLoggingEvent(Migration migration) {
		String expectedMessage = "Skipping migration [{}] with version {} since db is on higher version {}.";
		int currentVersion = versioner.getCurrentVersion(migration.getType());
		return warn(expectedMessage, migration.getDescription(), migration.getVersion(), currentVersion);
	}

}
