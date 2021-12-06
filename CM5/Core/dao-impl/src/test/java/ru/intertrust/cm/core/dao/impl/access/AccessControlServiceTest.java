package ru.intertrust.cm.core.dao.impl.access;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.CreateObjectAccessType;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.access.UserSubject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccessControlServiceTest {

    @InjectMocks
    private AccessControlServiceImpl accessControlService;

    @Mock
    private UserGroupGlobalCache userGroupCache;

    @Before
    public void init(){
        Map<String, Id> hash = new HashMap<>();
        hash.put("superuser", new RdbmsId(1, 1));
        hash.put("admin", new RdbmsId(1, 2));
        hash.put("user", new RdbmsId(1, 3));

        doAnswer(invocation -> {
            Id param = (Id)invocation.getArguments()[0];
            return param.equals(hash.get("admin"));
        }).when(userGroupCache).isAdministrator(any(Id.class));

        doAnswer(invocation -> {
            Id param = (Id)invocation.getArguments()[0];
            return param.equals(hash.get("superuser"));
        }).when(userGroupCache).isPersonSuperUser(any(Id.class));

        doAnswer(invocation -> {
            String param = (String)invocation.getArguments()[0];
            return hash.get(param);
        }).when(userGroupCache).getUserIdByLogin(anyString());
    }

    @Test
    public void testSimpleAccessToken(){
        AccessControlServiceImpl accessControlService = new AccessControlServiceImpl();

        AccessControlServiceImpl.SimpleAccessToken token = accessControlService.new SimpleAccessToken(
                new UserSubject(1),
                null,
                new CreateObjectAccessType("test_type", Collections.singletonList("Test_Type")),
                false);

        boolean result = token.allowsAccess(null,
                new CreateObjectAccessType("TEST_TYPE", null));
        assertTrue(result);
    }

    @Test
    public void testTokenDeferred(){
        AccessToken token = accessControlService.createCollectionAccessToken("superuser");
        assertFalse(token.isDeferred());
        token = accessControlService.createCollectionAccessToken("admin");
        assertFalse(token.isDeferred());
        token = accessControlService.createCollectionAccessToken("user");
        assertTrue(token.isDeferred());
    }
}
