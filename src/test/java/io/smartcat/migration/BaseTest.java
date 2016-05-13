package io.smartcat.migration;

import com.datastax.driver.core.*;

import java.util.ArrayList;
import java.util.List;

public class BaseTest {

    public void truncateTables(final String keyspace, final Session session) {
        for (final String table : tables(keyspace, session)) {
            session.execute(String.format("TRUNCATE %s.%s;", keyspace, table));
        }
    }

    private List<String> tables(final String keyspace, final Session session) {
        final List<String> tables = new ArrayList<>();
        final Cluster cluster = session.getCluster();
        final Metadata meta = cluster.getMetadata();
        final KeyspaceMetadata keyspaceMeta = meta.getKeyspace(keyspace);
        for (final TableMetadata tableMeta : keyspaceMeta.getTables()) {
            tables.add(tableMeta.getName());
        }

        return tables;
    }

}
