package io.smartcat.migration;

import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.google.common.base.Predicate;

public class ColumnNameMatcher implements Predicate<ColumnMetadata> {
    private String columnName;

    public ColumnNameMatcher(String columnName) {
        this.columnName = columnName;
    }

    public boolean apply(ColumnMetadata column) {
        return column.getName().asInternal().equals(columnName);
    }
}
