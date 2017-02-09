package ru.intertrust.cm.core.dao.impl.sqlparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.sf.jsqlparser.statement.select.Select;

import org.junit.Test;

import ru.intertrust.cm.core.dao.impl.sqlparser.FakeConfigurationExplorer.TypeConfigBuilder;

public class CollectDOTypesVisitorTest {

    private FakeConfigurationExplorer configurationExplorer = new FakeConfigurationExplorer();

    private static final String COLLECTION_COUNT_WITH_FILTERS =
            "SELECT count(*), 'employee' AS TEST_CONSTANT FROM employee AS e " +
                    "INNER JOIN department AS d ON e.department = d.id WHERE EXISTS " +
                    "(SELECT r.object_id FROM employee_READ AS r INNER JOIN group_member AS gm ON r.group_id = gm.usergroup " +
                    "WHERE gm.person_id = :user_id " +
                    "AND r.object_id = id) " +
                    "AND 1 = 1 AND d.name = 'dep1' AND e.name = 'employee1'";

    @Test
    public void testFindDOTypes() throws Exception {

        SqlQueryParser parser = new SqlQueryParser(COLLECTION_COUNT_WITH_FILTERS);
        Select select = parser.getSelectStatement();
        CollectDOTypesVisitor visitor = new CollectDOTypesVisitor(configurationExplorer);
        Set<String> types = visitor.getDOTypes(select);
        Set<String> checkTypes = new HashSet<>(Arrays.asList(new String[] {"group_member", "department", "employee" }));
        assertTrue(checkTypes.containsAll(types));
    }

    @Test
    public void testFindDOTypesSelectItemSubQuery() throws Exception {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("x")).addLongField("n"));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("t")).linkedTo("x", "x"));
        SqlQueryParser parser = new SqlQueryParser("select (select array_agg(abc) from t where t.x = x.id) from x where x.n = 0");
        Select select = parser.getSelectStatement();
        CollectDOTypesVisitor visitor = new CollectDOTypesVisitor(configurationExplorer);
        Set<String> types = visitor.getDOTypes(select);
        Set<String> expected = new HashSet<>(Arrays.asList(new String[] {"t", "x" }));
        assertEquals(expected, types);
    }

}
