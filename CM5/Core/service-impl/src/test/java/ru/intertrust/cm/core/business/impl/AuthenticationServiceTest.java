package ru.intertrust.cm.core.business.impl;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.dao.api.AuthenticationDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.MD5Service;


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
    private DomainObjectDao domainObjectDao;

    @Mock
    private AuthenticationDao authenticationDaoMock;

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
    public void testExistsAuthenticationInfo() {
        authenticationService.existsAuthenticationInfo(ADMIN_LOGIN);
        verify(authenticationDaoMock).existsAuthenticationInfo(ADMIN_LOGIN);

    }
}
