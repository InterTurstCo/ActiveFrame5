package ru.intertrust.cm.core.dao.impl.sqlparser;

import static org.junit.Assert.assertEquals;
import net.sf.jsqlparser.statement.select.Select;

import org.junit.Test;

public class WrapAndLowerSelectVisitorTest {

    private WrapAndLowerCaseStatementVisitor visitor = new WrapAndLowerCaseStatementVisitor();

    @Test
    public void testIgnoreValuesLists() {
        String query = "select Column1 from (values (1), (2)) t";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        visitor.visit(select);
        assertEquals("SELECT \"column1\" FROM (VALUES (1), (2)) t", select.toString());
    }

    @Test
    public void testLateralFromProcessing() {
        String query = "select ABC from x, lateral(select M from N where t = x.ABC) y";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        visitor.visit(select);
        assertEquals("SELECT \"abc\" FROM \"x\", LATERAL(SELECT \"m\" FROM \"n\" WHERE \"t\" = x.\"abc\") y", select.toString());
    }
}
