package io.smartcat.migration;

import static junit.framework.Assert.assertEquals;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
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

import java.util.List;

public class MigrationEngineItemsTest extends BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationEngineItemsTest.class);

    private static final String CONTACT_POINT = "localhost";
    private static final int PORT = 9142;
    private static final String KEYSPACE = "migration_test_items";
    private static final String CQL = "items.cql";

    private static Session session;
    private static Cluster cluster;

    @BeforeClass
    public static void init() throws Exception {
        LOGGER.info("Starting embedded cassandra server");
        EmbeddedCassandraServerHelper.startEmbeddedCassandra("another-cassandra.yaml");

        LOGGER.info("Connect to embedded db");
        cluster = Cluster.builder().addContactPoints(CONTACT_POINT).withPort(PORT).build();
        session = cluster.connect();

        LOGGER.info("Initialize keyspace");
        final CQLDataLoader cqlDataLoader = new CQLDataLoader(session);
        cqlDataLoader.load(new ClassPathCQLDataSet(CQL, false, true, KEYSPACE));
    }

    @After
    public void cleanUp() {
        truncateTables(KEYSPACE, session);
    }

    @AfterClass
    public static void tearDown() {
        if (cluster != null) {
            cluster.close();
            cluster = null;
        }
    }

    @Test
    public void initial_insert_test() {
        final int count = 100;

        final MigrationResources resources = new MigrationResources();
        resources.addMigration(new InsertInitialItemsMigration(count, 1));
        final boolean result = MigrationEngine.withSession(session).migrate(resources);

        assertEquals(true, result);

        final List<Row> rows = session.execute(QueryBuilder.select().from("items_by_id")).all();
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

        final List<Row> rows = session.execute(QueryBuilder.select().from("items_by_number_external_id")).all();
        assertEquals(count, rows.size());
    }

}
