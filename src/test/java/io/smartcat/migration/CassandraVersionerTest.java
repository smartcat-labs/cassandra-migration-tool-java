package io.smartcat.migration;

import static io.smartcat.migration.MigrationType.SCHEMA;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;



import io.smartcat.migration.migrations.schema.AddBookGenreFieldMigration;

public class CassandraVersionerTest {
    private CassandraVersioner versioner;
    private CqlSession session;
    private ResultSet versionResultSet;

    @Before
    public void setUp() {
        session = mock(CqlSession.class);
        versioner = new CassandraVersioner(session);
        versionResultSet = mock(ResultSet.class);
    }

    @Test
    public void whenSchemaVersionTableIsEmptyThenCurrentVersionShouldBe0() {
        expectRetrieveEmptyCurrentVersion();

        int currentVersion = versioner.getCurrentVersion(SCHEMA);

        assertThat(currentVersion, is(0));
    }

    @Test
    public void whenSchemaVersionTableIsNotEmptyThenCurrentVersionShouldBeRetrievedFromTheTable() {
        int expectedVersion = 1;

        expectRetrieveCurrentVersion(expectedVersion);

        int currentVersion = versioner.getCurrentVersion(SCHEMA);

        assertThat(currentVersion, is(expectedVersion));
    }

    @Test
    public void updateVersionSuccess() {
        versioner.updateVersion(new AddBookGenreFieldMigration(1));
    }

    private void expectRetrieveEmptyCurrentVersion() {
        expectRetrieveVersionResultSetWithRow(null);
    }

    private void expectRetrieveCurrentVersion(int expectedVersion) {
        Row row = expectRowWithVersion(expectedVersion);
        expectRetrieveVersionResultSetWithRow(row);
    }

    private void expectRetrieveVersionResultSetWithRow(Row row) {
        whenSessionExecuteQuery().thenReturn(versionResultSet);
        whenRetrieveRowFromVersionResultSet().thenReturn(row);
    }

    private Row expectRowWithVersion(int version) {
        Row row = mock(Row.class);
        when(row.getInt("version")).thenReturn(version);
        return row;
    }

    private OngoingStubbing<ResultSet> whenSessionExecuteQuery() {
        return when(session.execute(Mockito.any(Statement.class)));
    }

    private OngoingStubbing<Row> whenRetrieveRowFromVersionResultSet() {
        return when(versionResultSet.one());
    }
}
