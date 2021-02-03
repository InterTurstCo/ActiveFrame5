package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.statement.select.Select;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Parser40ProblemTest {
    @Test
    public void testUnionWithBracket() {
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

    @Test
    @Ignore("Бага в парсере 4.1-RC1")
    public void testUnionWithOrderAndLimit() {
        String query = "SELECT id FROM table1 " +
                "UNION " +
                "SELECT id FROM table2 " +
                "ORDER BY id ASC LIMIT 55";
        query = formatToCompare(query);
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        assertEquals(select.toString(), query);
    }

    private String formatToCompare(String sql){
        String result = sql;
        while (result.indexOf("\t") > 0){
            result = result.replace("\t", " ");
        }
        while (result.indexOf("\n") > 0){
            result = result.replace("\n", " ");
        }
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
