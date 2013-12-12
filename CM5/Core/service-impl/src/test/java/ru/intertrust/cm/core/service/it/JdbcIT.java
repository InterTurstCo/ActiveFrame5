package ru.intertrust.cm.core.service.it;

import java.util.List;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;

@RunWith(Arquillian.class)
public class JdbcIT {

    @Deployment
    public static Archive createDeployment() {
        EnterpriseArchive archive = DeployArchives.SOCHI_PLATFORM;
        JavaArchive testArchive = ShrinkWrap.create(JavaArchive.class)
                .addClasses(JdbcIT.class, LoginPasswordCallbackHandler.class);
        testArchive.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        WebArchive webApp = archive.getAsType(WebArchive.class, "web-app.war");
        webApp.addAsLibraries(testArchive);

        return archive;
    }

    @EJB
    private ScheduleService.Remote scheduleService;

    @EJB
    private CollectionsService.Remote collectionService;

    @Test
    public void testArquillian() {
        Assert.assertNotNull(scheduleService);
        List<String> taskClasses = scheduleService.getTaskClasses();
        for (String taskClass : taskClasses) {
            System.out.println(taskClass);
        }
        Assert.assertTrue(taskClasses.size() > 0);
    }

    @Test
    public void testCollection() throws LoginException {
        LoginContext lc = login("admin", "admin");
        lc.login();
        try {

            Assert.assertNotNull(collectionService);
            String query = "select p.id from person p";
            IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query);
            Assert.assertNotNull(collection);
            Assert.assertTrue(collection.size() > 0);
        } finally {
            lc.logout();
        }
    }

    private LoginContext login(String login, String password)
            throws LoginException {
        LoginContext lc = null;
        lc = new LoginContext("CM5", new LoginPasswordCallbackHandler(login, password));
        lc.login();
        return lc;
    }

}
