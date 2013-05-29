package ru.intertrust.cm.core.business.impl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.MD5Service;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfo;
import ru.intertrust.cm.core.dao.api.AuthenticationDAO;

/**
 * Тест сервиса аутентификации {@link AuthenticationService}
 * @author atsvetkov
 *
 */

public class AuthenticationServiceTest {

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    @InjectMocks
    private AuthenticationServiceImpl authenticationService = new AuthenticationServiceImpl();

    @Mock
    private MD5Service md5ServiceMock;

    @Mock
    private AuthenticationDAO authenticationDAOMock;

    private AuthenticationInfo testAuthenticationInfo;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testAuthenticationInfo = new AuthenticationInfo();
        testAuthenticationInfo.setId(1);
        testAuthenticationInfo.setUserUid(ADMIN_LOGIN);
        testAuthenticationInfo.setPassword(ADMIN_PASSWORD);
    }

    @Test
    public void testInsertAuthenticationInfo() {

        authenticationService.insertAuthenticationInfo(testAuthenticationInfo);
        when(md5ServiceMock.getMD5(ADMIN_PASSWORD)).thenReturn(ADMIN_PASSWORD);

        verify(md5ServiceMock).getMD5(ADMIN_PASSWORD);
        verify(authenticationDAOMock, times(1)).insertAuthenticationInfo(testAuthenticationInfo);
    }

    @Test
    public void testExistsAuthenticationInfo() {

        authenticationService.existsAuthenticationInfo(ADMIN_LOGIN);
        verify(authenticationDAOMock).existsAuthenticationInfo(ADMIN_LOGIN);

    }
}
