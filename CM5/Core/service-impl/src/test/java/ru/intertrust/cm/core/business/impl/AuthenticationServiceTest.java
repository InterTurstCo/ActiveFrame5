package ru.intertrust.cm.core.business.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.MD5Service;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfo;
import ru.intertrust.cm.core.dao.api.AuthenticationDAO;
import ru.intertrust.cm.core.dao.impl.AuthenticationDAOImpl;

/**
 * Тест сервиса аутентификации {@link AuthenticationService}
 * @author atsvetkov
 *
 */
public class AuthenticationServiceTest {

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    private AuthenticationServiceImpl authenticationService;
    private MD5Service md5ServiceMock;
    private AuthenticationDAO authenticationDAOMock;
    AuthenticationInfo testAuthenticationInfo;
    
    @Before
    public void setUp() {
        md5ServiceMock = mock(MD5ServiceImpl.class);
        authenticationDAOMock = mock(AuthenticationDAOImpl.class);
        authenticationService = new AuthenticationServiceImpl();

        testAuthenticationInfo = new AuthenticationInfo();
        testAuthenticationInfo.setId(1);
        testAuthenticationInfo.setUserUid(ADMIN_LOGIN);
        testAuthenticationInfo.setPassword(ADMIN_PASSWORD);
    }   
    
    @Test
    public void testInsertAuthenticationInfo() {
        authenticationService.setMd5Service(md5ServiceMock);
        authenticationService.setAuthenticationDAO(authenticationDAOMock);

        authenticationService.insertAuthenticationInfo(testAuthenticationInfo);

        verify(md5ServiceMock).getMD5(ADMIN_PASSWORD);
        verify(authenticationDAOMock, times(1)).insertAuthenticationInfo(testAuthenticationInfo);
    }
    
    @Test
    public void testExistsAuthenticationInfo() {

        authenticationService.setMd5Service(md5ServiceMock);
        authenticationService.setAuthenticationDAO(authenticationDAOMock);
        authenticationService.existsAuthenticationInfo(ADMIN_LOGIN);
        verify(authenticationDAOMock).existsAuthenticationInfo(ADMIN_LOGIN);

    }
}
