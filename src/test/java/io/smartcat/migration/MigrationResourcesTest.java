package io.smartcat.migration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.smartcat.migration.Migration;
import io.smartcat.migration.MigrationResources;
import io.smartcat.migration.MigrationType;

public class MigrationResourcesTest {

    @Test
    public void initialization_test() {
        final MigrationResources resources = new MigrationResources();
        resources.addMigration(new SchemaMigrationTestImplementation(1));
        resources.addMigration(new SchemaMigrationTestImplementation(2));
        resources.addMigration(new SchemaMigrationTestImplementation(3));
        resources.addMigration(new SchemaMigrationTestImplementation(4));
        resources.addMigration(new SchemaMigrationTestImplementation(5));

        assertEquals(5, resources.getMigrations().size());
        assertEquals(3, resources.getMigration(2).getVersion());
    }

    @Test
    public void mixed_types_test() {
        final MigrationResources resources = new MigrationResources();
        resources.addMigration(new SchemaMigrationTestImplementation(1));
        resources.addMigration(new SchemaMigrationTestImplementation(2));
        resources.addMigration(new SchemaMigrationTestImplementation(3));
        resources.addMigration(new DataMigrationTestImplementation(1));
        resources.addMigration(new DataMigrationTestImplementation(2));
        resources.addMigration(new DataMigrationTestImplementation(3));

        resources.getMigrations();
        assertEquals(6, resources.getMigrations().size());
        assertEquals(3, resources.getMigration(2).getVersion());
        assertEquals(MigrationType.SCHEMA, resources.getMigration(2).getType());
        assertEquals(3, resources.getMigration(5).getVersion());
        assertEquals(MigrationType.DATA, resources.getMigration(5).getType());
    }

    @Test
    public void test_inserting_same_migration_ignored() {
        final MigrationResources resources = new MigrationResources();
        resources.addMigration(new SchemaMigrationTestImplementation(1));
        resources.addMigration(new SchemaMigrationTestImplementation(2));
        resources.addMigration(new SchemaMigrationTestImplementation(3));
        resources.addMigration(new SchemaMigrationTestImplementation(5));
        resources.addMigration(new SchemaMigrationTestImplementation(3));

        assertEquals(4, resources.getMigrations().size());
        assertEquals(1, resources.getMigration(0).getVersion());
        assertEquals(2, resources.getMigration(1).getVersion());
        assertEquals(3, resources.getMigration(2).getVersion());
        assertEquals(5, resources.getMigration(3).getVersion());
    }

    @Test
    public void mixed_inserts_order() {
        final MigrationResources resources = new MigrationResources();
        resources.addMigration(new SchemaMigrationTestImplementation(1));
        resources.addMigration(new SchemaMigrationTestImplementation(3));
        resources.addMigration(new SchemaMigrationTestImplementation(5));

        assertEquals(3, resources.getMigrations().size());

        resources.addMigration(new SchemaMigrationTestImplementation(2));
        resources.addMigration(new SchemaMigrationTestImplementation(4));

        assertEquals(5, resources.getMigrations().size());
        assertEquals(1, resources.getMigration(0).getVersion());
        assertEquals(3, resources.getMigration(1).getVersion());
        assertEquals(5, resources.getMigration(2).getVersion());
        assertEquals(2, resources.getMigration(3).getVersion());
        assertEquals(4, resources.getMigration(4).getVersion());
    }

    public class SchemaMigrationTestImplementation extends SchemaMigration {

		public SchemaMigrationTestImplementation(final int version) {
			super(version);
		}
    	
		@Override
        public String getDescription() {
            return "Test description";
        }

        @Override
        public void execute() {

        }
    }
    
    public class DataMigrationTestImplementation extends DataMigration {

        protected DataMigrationTestImplementation(final int version) {
            super(version);
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
