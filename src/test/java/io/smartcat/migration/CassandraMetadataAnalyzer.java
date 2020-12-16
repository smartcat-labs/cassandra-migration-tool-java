package io.smartcat.migration;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.tryFind;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.google.common.base.Optional;

public class CassandraMetadataAnalyzer {

    private CqlSession session;

    public CassandraMetadataAnalyzer(CqlSession session) {
        checkNotNull(session, "Session cannot be null");
        checkNotNull(session.getKeyspace(), "Session must be logged into a keyspace");
        this.session = session;
    }

    public boolean columnExistInTable(String columnName, String tableName) {
        TableMetadata table = getTableMetadata(this.session, tableName);
        Optional<ColumnMetadata> column = tryFind(table.getColumns().values(), new ColumnNameMatcher(columnName));
        return column.isPresent();
    }

    private static TableMetadata getTableMetadata(CqlSession session, String tableName) {
        Metadata metadata = session.getMetadata();
        CqlIdentifier keyspace = session.getKeyspace().get();
        KeyspaceMetadata keyspaceMetadata = metadata.getKeyspace(keyspace).get();
        return keyspaceMetadata.getTable(tableName).get();
    }
}
