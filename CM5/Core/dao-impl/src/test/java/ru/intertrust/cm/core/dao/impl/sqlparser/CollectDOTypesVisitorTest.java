package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.config.ConfigurationExplorer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CollectDOTypesVisitorTest {

    @Mock
    private ConfigurationExplorer configurationExplorer;

    private static final String COLLECTION_COUNT_WITH_FILTERS =
            "SELECT count(*), 'employee' AS TEST_CONSTANT FROM employee AS e " +
                    "INNER JOIN department AS d ON e.department = d.id WHERE EXISTS " +
                    "(SELECT r.object_id FROM employee_READ AS r INNER JOIN group_member AS gm ON r.group_id = gm.usergroup " +
                    "WHERE gm.person_id = :user_id " +
                    "AND r.object_id = id) " +
                    "AND 1 = 1 AND d.name = 'dep1' AND e.name = 'employee1'";

    @Test
    public void testFindDOTypes() throws Exception {
        when(configurationExplorer.findChildDomainObjectTypes(anyString(), eq(true))).thenReturn(null);
        SqlQueryParser parser = new SqlQueryParser(COLLECTION_COUNT_WITH_FILTERS);
        Select select = parser.getSelectStatement();
        CollectDOTypesVisitor visitor = new CollectDOTypesVisitor(configurationExplorer);
        Set<String> types = visitor.getDOTypes(select);
        Set<String> checkTypes = new HashSet<>(Arrays.asList(new String[] {"group_member", "department", "employee" }));
        assertTrue(checkTypes.containsAll(types));

        System.out.println(types);
    }

}
