package io.smartcat.migration.migrations.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import io.smartcat.migration.DataMigration;
import io.smartcat.migration.exceptions.MigrationException;

/**
 * Example of data migration which will go through all entries in DB and for each add genre. Real life example which
 * covers this case is adding of new column to DB and need to populate it with some data for already existing entries
 */
public class AddGenreMigration extends DataMigration {
    public AddGenreMigration(final int version) {
        super(version);
    }

    @Override
    public String getDescription() {
        return "Insert genre for each book in database";
    }

    @Override
    public void execute() throws MigrationException {
        try {
            addGenreToBooks();
        } catch (final Exception e) {
            throw new MigrationException("Failed to execute InsertBooksMigration migration", e);
        }
    }

    private void addGenreToBooks() {
        final Statement select = QueryBuilder.select().all().from("books");
        final ResultSet results = this.session.execute(select);

        final PreparedStatement updateBookGenreStatement =
                session.prepare("UPDATE books SET genre = ? WHERE name = ? AND author = ?;");

        for (final Row row : results) {
            final String name = row.getString("name");
            final String author = row.getString("author");

            BoundStatement update;

            if (name.equals("Journey to the Center of the Earth")) {
                update = updateBookGenreStatement.bind("fantasy", name, author);
            } else if (name.equals("Fifty Shades of Grey")) {
                update = updateBookGenreStatement.bind("erotica", name, author);
            } else {
                update = updateBookGenreStatement.bind("programming", name, author);
            }

            session.execute(update);
        }
    }
}
