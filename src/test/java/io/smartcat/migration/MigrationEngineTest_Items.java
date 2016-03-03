package io.smartcat.migration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationEngineTest_Items {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationEngineTest_Items.class);

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

    @AfterClass
    public static void tearDown() {
        if (cluster != null) {
            cluster.close();
            cluster = null;
        }
    }

    @Test
    public void test_schema_migration() {
        final MigrationResources resources = new MigrationResources();

    }

    @Test
    public void test_data_migration() {

    }

    @Test
    public void test_migrations() {

    }

}
