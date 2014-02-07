package ru.intertrust.cm.core.service.it;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.core.api.annotation.Inject;

import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

/**
 * Базовый класс для интеграционных тестов
 * @author larin
 * 
 */
public class IntegrationTestBase extends IntegrationTestSuit {

    @EJB
    protected ImportDataService.Remote importService;

    @Inject
    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

    /**
     * Подключение под именем пользователя и пароля
     * @param login
     * @param password
     * @return
     * @throws LoginException
     */
    protected LoginContext login(String login, String password)
            throws LoginException {
        LoginContext lc = null;
        lc = new LoginContext("CM5", new LoginPasswordCallbackHandler(login, password));
        lc.login();
        return lc;
    }

    /**
     * Импорт тестовых данных
     * @param filePath
     * @throws IOException
     */
    protected void importTestData(String filePath) throws IOException {
        URL fileUrl = Thread.currentThread().getContextClassLoader().getResource(filePath);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream input = null;
        try {
            input = fileUrl.openStream();
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }

            importService.importData(out.toByteArray());
        } finally {
            try {
                input.close();
            } catch (Exception ignoreEx) {

            }
        }
    }

}
