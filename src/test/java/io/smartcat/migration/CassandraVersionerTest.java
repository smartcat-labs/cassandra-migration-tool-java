package io.smartcat.migration;

import static io.smartcat.migration.MigrationType.SCHEMA;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

import io.smartcat.migration.migrations.schema.AddBookGenreFieldMigration;

public class CassandraVersionerTest {
    private CassandraVersioner versioner;
    private Session session;
    private ResultSet versionResultSet;

    @Before
    public void setUp() throws Exception {
        session = mock(Session.class);
        versioner = new CassandraVersioner();
        versionResultSet = mock(ResultSet.class);
    }

    @Test
    public void whenSchemaVersionTableIsEmptyThenCurrentVersionShouldBe0() throws Exception {
        expectRetrieveEmptyCurrentVersion();

        int currentVersion = versioner.getCurrentVersion(session, SCHEMA);

        assertThat(currentVersion, is(0));
    }

    @Test
    public void whenSchemaVersionTableIsNotEmptyThenCurrentVersionShouldBeRetrievedFromTheTable() throws Exception {
        int expectedVersion = 1;

        expectRetrieveCurrentVersion(expectedVersion);

        int currentVersion = versioner.getCurrentVersion(session, SCHEMA);

        assertThat(currentVersion, is(expectedVersion));
    }

    @Test
    public void updateVersionSucess() throws Exception {
        versioner.markMigrationAsApplied(session, new AddBookGenreFieldMigration<>(1));
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
