package com.smartcat.migration.migrations.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.smartcat.migration.Migration;
import com.smartcat.migration.MigrationException;
import com.smartcat.migration.MigrationType;

public class AddGenreMigration extends Migration {
    public AddGenreMigration(final MigrationType type, final int version) {
        super(type, version);
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

        final PreparedStatement updateBookGenreStatement = session
                .prepare("UPDATE books SET genre = ? WHERE name = ? AND author = ?;");

        for (final Row row : results) {
            final String name = row.getString("name");
            final String author = row.getString("author");

            BoundStatement update;

            if (name.equals("Journey to the Center of the Earth")) {
                update = updateBookGenreStatement.bind("fantasy", name, author);
            } else if (name.equals("Journey to the Center of the Earth")) {
                update = updateBookGenreStatement.bind("erotica", name, author);
            } else {
                update = updateBookGenreStatement.bind("programming", name, author);
            }

            session.execute(update);
        }
    }
}
