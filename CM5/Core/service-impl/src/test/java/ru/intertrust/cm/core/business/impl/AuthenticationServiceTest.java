package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.MD5Service;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.dao.api.AuthenticationDAO;
import ru.intertrust.cm.core.dao.api.CrudServiceDAO;

import static org.mockito.Mockito.verify;


/**
 * Тест сервиса аутентификации {@link AuthenticationService}
 * @author atsvetkov
 *
 */

public class AuthenticationServiceTest {

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    @InjectMocks
    private final AuthenticationServiceImpl authenticationService = new AuthenticationServiceImpl();

    @Mock
    private MD5Service md5ServiceMock;

    @Mock
    private ConfigurationExplorerImpl configurationExplorer;

    @Mock
    private CrudServiceDAO crudServiceDAO;

    @Mock
    private AuthenticationDAO authenticationDAOMock;

    private AuthenticationInfoAndRole testAuthenticationInfo;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testAuthenticationInfo = new AuthenticationInfoAndRole();
        testAuthenticationInfo.setId(1);
        testAuthenticationInfo.setUserUid(ADMIN_LOGIN);
        testAuthenticationInfo.setPassword(ADMIN_PASSWORD);
    }

    @Test
    public void testInsertAuthenticationInfo() {
        //authenticationService.insertAuthenticationInfoAndRole(testAuthenticationInfo);
//        when(crudServiceDAO.create(anyObject(), anyObject())).thenReturn(Long.valueOf(7)); // ID конфигурации доменного объекта
//        when(md5ServiceMock.getMD5(ADMIN_PASSWORD)).thenReturn(ADMIN_PASSWORD);

//        verify(md5ServiceMock).getMD5(ADMIN_PASSWORD);

    }

    @Test
    public void testExistsAuthenticationInfo() {
        authenticationService.existsAuthenticationInfo(ADMIN_LOGIN);
        verify(authenticationDAOMock).existsAuthenticationInfo(ADMIN_LOGIN);

    }
}
