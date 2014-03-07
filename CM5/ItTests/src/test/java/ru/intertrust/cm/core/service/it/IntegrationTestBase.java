package ru.intertrust.cm.core.service.it;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;

/**
 * Базовый класс для интеграционных тестов
 * @author larin
 * 
 */
public class IntegrationTestBase extends IntegrationTestSuit {
    private ApplicationContext applicationContext;

    @EJB
    protected ImportDataService.Remote importService;

    @Inject
    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

    private static boolean isInit;

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

    /**
     * Инициализация всех тестов. Импорт данных из CSV файлов, Подготовка spring контекста. Необходимо вызвать в методе @Before
     * дочерних классов
     * @throws IOException
     */
    protected void initBase() throws IOException {
        if (!isInit) {
            applicationContext = ApplicationContextProvider.getApplicationContext();

            importTestData("test-data/status.csv");
            importTestData("test-data/import-organization.csv");
            importTestData("test-data/import-department.csv");
            importTestData("test-data/import-employee.csv");
            importTestData("test-data/set-organization-boss.csv");
            importTestData("test-data/set-department-boss.csv");
        }
    }

    protected <T> T getBean(Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(requiredType);
    }
}
