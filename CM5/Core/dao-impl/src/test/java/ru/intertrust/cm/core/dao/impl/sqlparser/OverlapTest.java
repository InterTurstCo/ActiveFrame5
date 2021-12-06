package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

import static org.junit.Assert.*;

public class OverlapTest {

    @Test
    public void testIgnoreValuesLists() {
        String query = "SELECT * FROM test_type WHERE " +
                "(tstzrange(substitution_start_date, substitution_end_date, '[]') " +
                "&& tstzrange(substitution_start_date_2, substitution_end_date_2, '[]'))";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        assertEquals(select.toString(), query);
    }
}
