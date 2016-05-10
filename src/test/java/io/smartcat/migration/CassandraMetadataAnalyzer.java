package io.smartcat.migration;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.tryFind;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.google.common.base.Optional;

public class CassandraMetadataAnalyzer {

    private Session session;

    public CassandraMetadataAnalyzer(Session session) {
        checkNotNull(session, "Session cannot be null");
        checkNotNull(session.getLoggedKeyspace(), "Session must be logged into a keyspace");
        this.session = session;
    }

    public boolean columnExistInTable(String columnName, String tableName) {
        TableMetadata table = getTableMetadata(this.session, tableName);
        Optional<ColumnMetadata> column = tryFind(table.getColumns(), new ColumnNameMatcher(columnName));
        return column.isPresent();
    }

    private static TableMetadata getTableMetadata(Session session, String tableName) {
        Metadata metadata = session.getCluster().getMetadata();
        KeyspaceMetadata keyspaceMetadata = metadata.getKeyspace(session.getLoggedKeyspace());
        return keyspaceMetadata.getTable(tableName);
    }
}
