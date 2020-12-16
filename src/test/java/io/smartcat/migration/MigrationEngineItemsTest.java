package io.smartcat.migration;

import static org.junit.Assert.assertEquals;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import io.smartcat.migration.migrations.data.InsertInitialItemsMigration;
import io.smartcat.migration.migrations.data.PopulateItemByNumberAndExternalIdMigration;
import io.smartcat.migration.migrations.schema.CreateItemByNumberAndExternalIdMigration;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

public class MigrationEngineItemsTest extends BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationEngineItemsTest.class);

    private static final String LOCAL_DATACENTER = "DC1";
    private static final String CONTACT_POINT = "localhost";
    private static final int PORT = 9142;
    private static final String KEYSPACE = "migration_test_items";
    private static final String CQL = "items.cql";

    private static CqlSession session;

    @BeforeClass
    public static void init() throws Exception {
        LOGGER.info("Starting embedded cassandra server");
        EmbeddedCassandraServerHelper.startEmbeddedCassandra("another-cassandra.yaml");

        LOGGER.info("Connect to embedded db");
        session = new CqlSessionBuilder()
                .withLocalDatacenter(LOCAL_DATACENTER)
                .addContactPoint(new InetSocketAddress(CONTACT_POINT, PORT))
                .build();

        LOGGER.info("Initialize keyspace");
        new CQLDataLoader(session).load(new ClassPathCQLDataSet(CQL, false, true, KEYSPACE));
    }

    @After
    public void cleanUp() {
        truncateTables(KEYSPACE, session);
    }

    @AfterClass
    public static void tearDown() {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    @Test
    public void initial_insert_test() {
        final int count = 100;

        final MigrationResources resources = new MigrationResources();
        resources.addMigration(new InsertInitialItemsMigration(count, 1));
        final boolean result = MigrationEngine.withSession(session).migrate(resources);

        assertEquals(true, result);

        final List<Row> rows = session.execute(QueryBuilder.selectFrom("items_by_id").all().build())
                .all();
        assertEquals(count, rows.size());
    }

    @Test
    public void test_migrations() {
        final int count = 100;

        final MigrationResources resources = new MigrationResources();
        resources.addMigration(new InsertInitialItemsMigration(count, 1));
        resources.addMigration(new CreateItemByNumberAndExternalIdMigration(1));
        resources.addMigration(new PopulateItemByNumberAndExternalIdMigration(2));
        final boolean result = MigrationEngine.withSession(session).migrate(resources);

        assertEquals(true, result);

        final List<Row> rows = session.execute(QueryBuilder.selectFrom("items_by_number_external_id").all().build())
                .all();
        assertEquals(count, rows.size());
    }

}
