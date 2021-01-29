package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Parser40ProblemTest {
    @Test
    public void testIgnoreValuesLists() {
        String query = "SELECT * FROM " +
                "  ( " +
                "    (SELECT A FROM tbl) " +
                "    UNION " +
                "    (SELECT B FROM tbl2) " +
                "  ) AS union1";
        query = formatToCompare(query);
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        assertEquals(select.toString(), query);
    }

    private String formatToCompare(String sql){
        String result = sql;
        while (result.indexOf("  ") > 0){
            result = result.replace("  ", " ");
        }
        while (result.indexOf("( ") > 0){
            result = result.replace("( ", "(");
        }
        while (result.indexOf(") )") > 0){
            result = result.replace(") )", "))");
        }
        return result;
    }
}
