package io.smartcat.migration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.smartcat.migration.Migration;
import io.smartcat.migration.MigrationType;

public class MigrationTest {

    @Test
    public void test_equal_migrations() {
        final Migration migration1 = new MigrationTestImplementation(MigrationType.SCHEMA, 1);
        final Migration migration2 = new MigrationTestImplementation(MigrationType.SCHEMA, 1);

        assertTrue(migration1.equals(migration2));
    }

    @Test
    public void test_different_type_non_equal_migrations() {
        final Migration migration1 = new MigrationTestImplementation(MigrationType.DATA, 1);
        final Migration migration2 = new MigrationTestImplementation(MigrationType.SCHEMA, 1);

        assertFalse(migration1.equals(migration2));
    }

    @Test
    public void test_different_version_non_equal_migrations() {
        final Migration migration1 = new MigrationTestImplementation(MigrationType.SCHEMA, 1);
        final Migration migration2 = new MigrationTestImplementation(MigrationType.SCHEMA, 2);

        assertFalse(migration1.equals(migration2));
    }

    public class MigrationTestImplementation extends Migration {

        protected MigrationTestImplementation(final MigrationType type, final int version) {
            super(type, version);
        }

        @Override
        public String getDescription() {
            return "Test description";
        }

        @Override
        public void execute() {

        }

    }

}
