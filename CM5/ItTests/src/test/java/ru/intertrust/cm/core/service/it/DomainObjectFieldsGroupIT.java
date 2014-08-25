package ru.intertrust.cm.core.service.it;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;

@RunWith(Arquillian.class)
public class DomainObjectFieldsGroupIT extends IntegrationTestBase {

    @EJB
    ConfigurationService.Remote configurationService;

    @Before
    public void init() throws IOException, LoginException {
        LoginContext lc = login("admin", "admin");
        lc.login();
    }

    @After
    public void tearDown() throws LoginException {
        LoginContext lc = login("admin", "admin");
        lc.logout();
    }

    @Test
    public void testReadFieldsGroup() {
        DomainObjectTypeConfig config = configurationService.getConfig(DomainObjectTypeConfig.class, "department_test");
        List<FieldConfig> fieldConfigs = config.getFieldConfigs();
        assertEquals(fieldConfigs.size(), 18);
    }
}
