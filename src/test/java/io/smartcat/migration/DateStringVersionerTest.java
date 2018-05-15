package io.smartcat.migration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import io.smartcat.migration.migrations.schema.AddBookGenreFieldMigration;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertEquals;

/**
 * @author dalvizu
 */
public class DateStringVersionerTest
    extends BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateStringVersionerTest.class);

    private static final String CONTACT_POINT = "localhost";
    private static final int PORT = 9142;
    private static final String KEYSPACE = "migration_test_items";
    private static final String CQL = "items.cql";

    private static Session session;
    private static Cluster cluster;

    private DateStringVersioner versioner;

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

    @Before
    public void setup() {
        versioner = new DateStringVersioner();
        versioner.bootstrapSchemaVersionTable(session);
    }

    @Test
    public void testDateFormat() {
        LocalDateTime firstDate /* aww */  = LocalDate.of(2018, 05, 01)
                .atTime(06, 13, 14);

        String dateString = DateStringVersioner.getDateString(firstDate);
        assertEquals("20180501061314", dateString);

        LocalDateTime secondDate  = LocalDate.of(2018, 05, 01)
                .atTime(18, 13, 14);

        dateString = DateStringVersioner.getDateString(secondDate);
        assertEquals("20180501181314", dateString);

    }

//    @Test
//    public void testNoUpdatesApplied() {
//        assertThat(versioner.getUpdatesApplied(session, MigrationType.SCHEMA), is(empty()));
//    }

    @Test
    public void testApplyMigration() {
        AddBookGenreFieldMigration migration = new AddBookGenreFieldMigration("20180506010203");
        versioner.markMigrationAsApplied(session, migration);
        assertThat(versioner.getUpdatesApplied(session, migration.getType()), not(empty()));
        assertThat(versioner.getUpdatesApplied(session, migration.getType()), contains(migration.getVersion()));
        assertEquals(migration.getVersion(), versioner.getMostRecentUpdateApplied(session, migration.getType()));
    }

}
