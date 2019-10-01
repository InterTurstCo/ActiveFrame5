package ru.intertrust.cm.core.dao.impl.access;

import org.junit.Test;
import ru.intertrust.cm.core.dao.access.CreateObjectAccessType;
import ru.intertrust.cm.core.dao.access.UserSubject;

import java.util.Collections;

import static junit.framework.TestCase.assertTrue;

public class AccessControlServiceTest {

    @Test
    public void testSimpleAccessToken(){
        AccessControlServiceImpl accessControlService = new AccessControlServiceImpl();

        AccessControlServiceImpl.SimpleAccessToken tocken = accessControlService.new SimpleAccessToken(
                new UserSubject(1),
                null,
                new CreateObjectAccessType("test_type", Collections.singletonList("Test_Type")),
                false);

        boolean result = tocken.allowsAccess(null,
                new CreateObjectAccessType("TEST_TYPE", null));
        assertTrue(result);
    }
}
