package io.smartcat.migration;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;

import java.util.Set;

public class BaseTest {

    public void truncateTables(final String keyspace, final CqlSession session) {
        for (final CqlIdentifier table : tables(keyspace, session)) {
            session.execute(String.format("TRUNCATE %s.%s;", keyspace, table));
        }
    }

    private Set<CqlIdentifier> tables(final String keyspace, final CqlSession session) {
        final Metadata meta = session.getMetadata();
        final KeyspaceMetadata keyspaceMeta = meta.getKeyspace(keyspace).get();

        return keyspaceMeta.getTables().keySet();
    }

}
