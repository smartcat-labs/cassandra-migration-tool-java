package io.smartcat.migration;

import com.datastax.driver.core.Row;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Version Migrations with a String value assumed to be a timestamp a la
 * ruby-on-rails active record migrations, e.g: '20180514010203'.
 *
 * @author dalvizu
 */
public class DateStringVersioner
    extends AbstractVersioner<String> {


    @Override
    public String getVersionType() {
        return "text";
    }

    /**
     * Constructor.
     */
    public  DateStringVersioner() {

    }

    /**
     * @param localDateTime - the date to format
     * @return a String of the given local date time suitable for saving to the database as a version
     */
    public static String getDateString(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    @Override
    protected String getVersion(Optional<Row> row) {
        return row.isPresent() ? row.get().getString(VERSION) : "";
    }
}
