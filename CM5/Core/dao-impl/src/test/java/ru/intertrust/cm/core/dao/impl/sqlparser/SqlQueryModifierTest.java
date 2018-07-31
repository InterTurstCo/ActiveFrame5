package ru.intertrust.cm.core.dao.impl.sqlparser;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.impl.DomainObjectQueryHelper;
import ru.intertrust.cm.core.dao.impl.sqlparser.FakeConfigurationExplorer.TypeConfigBuilder;

@RunWith(MockitoJUnitRunner.class)
public class SqlQueryModifierTest {
    @Mock
    private CurrentUserAccessor currentUserAccessor;
    @Mock
    private UserGroupGlobalCache userGroupCache;
    
    @Test
    public void testIgnoreNotDoTables() throws Exception {
        FakeConfigurationExplorer configurationExplorer = new FakeConfigurationExplorer();
        TypeConfigBuilder typeConfigBuilder = new TypeConfigBuilder("person");
        typeConfigBuilder.addStringField("login");
        typeConfigBuilder.addReferenceField("profile", "profile");
        configurationExplorer.createTypeConfig(typeConfigBuilder);

        SqlQueryModifier modifier = new SqlQueryModifier(configurationExplorer, 
                userGroupCache, currentUserAccessor, new DomainObjectQueryHelper());
        
        SqlQueryParser sqlParser = new SqlQueryParser("select profile from (select login as profile from person) t");
        Select select = sqlParser.getSelectStatement();        
        modifier.addServiceColumns(select);
        
        assertTrue(((PlainSelect)select.getSelectBody()).getSelectItems().size() == 1);
    }
}
