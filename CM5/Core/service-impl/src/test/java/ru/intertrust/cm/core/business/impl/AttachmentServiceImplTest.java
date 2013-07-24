package ru.intertrust.cm.core.business.impl;

import com.google.common.io.ByteStreams;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.model.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.model.AttachmentTypesConfig;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.impl.FileSystemAttachmentContentDaoImpl;

import java.io.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Vlad
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class AttachmentServiceImplTest {

    static private  final boolean IS_WIN_OS = System.getProperty("os.name").toLowerCase().indexOf("win") > 0;
    static private final String TEST_OUT_DIR = System.getProperty("test.cnf.testOutDir");
    static private final int PORT_RMI = Integer.parseInt(System.getProperty("test.cnf.portRmi"));
    static private final String PATH_XML = System.getProperty("test.cnf.pathXml");

    private static final String CONFIGURATION_SCHEMA_PATH = convertPathByOS(PATH_XML, "/config/configuration-test.xsd");
    private static final String SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH = convertPathByOS(PATH_XML, "/config/system-domain-objects-test.xml");
    private static final String DOMAIN_OBJECTS_CONFIG_PATH = convertPathByOS(PATH_XML, "/config/domain-objects-test.xml");
    private static final String COLLECTIONS_CONFIG_PATH = convertPathByOS(PATH_XML, "/config/collections-test.xml");

    private static final Set<String> CONFIG_PATHS =
        new HashSet<>(Arrays.asList(SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH, DOMAIN_OBJECTS_CONFIG_PATH, COLLECTIONS_CONFIG_PATH));

    static private AttachmentServiceRmi stubAttachmentService;

    static private String convertPathByOS(String ... paths) {
        String absPath = "";
        for (String path : paths) {
            absPath += path;
        }
        return IS_WIN_OS ? absPath.replace("/", "\\") : absPath.replace("\\", "/");
    }

    static class GenericDomainObjectWrapper extends GenericDomainObject implements Serializable {
        Map<String, Value> values = new HashMap<>();
        String typeName;
        Id id;

        public void setValue(String field, Value value) {
            values.put(field, value);
        }

        public Value getValue(String field) {
            return values.get(field);
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public Id getId() {
            return id;
        }

        public void setId(Id id) {
            this.id = id;
        }
    }

    @Configuration
    @RunWith(MockitoJUnitRunner.class)
    static class ContextConfiguration {
        {
            MockitoAnnotations.initMocks(this);
        }

        @Mock
        private ConfigurationExplorer configurationExplorer;

        @Mock
        private DomainObjectDao domainObjectDao;

        @Bean
        public ConfigurationExplorer configurationExplorer() {
            try {
                return getConfigurationExplorer();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Bean
        public AttachmentService attachmentService() {
            return new AttachmentServiceImpl();
        }

        @Bean
        public AttachmentContentDao attachmentContentDao() {
            FileSystemAttachmentContentDaoImpl contentDao = new FileSystemAttachmentContentDaoImpl();
            contentDao.setAttachmentSaveLocation(TEST_OUT_DIR);
            return contentDao;
        }

        @Bean
        public DomainObjectDao domainObjectDao() {
            return fillDomainObjectDao(domainObjectDao);
        }

        @Bean
        public CrudService crudService() {
            return new CrudServiceImpl();
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        if (stubAttachmentService == null) {
            Registry registry = LocateRegistry.createRegistry(PORT_RMI);
            AttachmentServiceRmiImpl serviceRmi = new AttachmentServiceRmiImpl();
            serviceRmi.setAttachmentService(attachmentService);
            registry.bind("AttachmentServiceRmi", UnicastRemoteObject.exportObject(serviceRmi, 0));
            stubAttachmentService = (AttachmentServiceRmi) registry.lookup("AttachmentServiceRmi");
        }
    }

    @Autowired
    private AttachmentService attachmentService;


    public interface AttachmentServiceRmi extends Remote {
        String saveAttachment(RemoteInputStream inputStream, DomainObject domainObject) throws RemoteException;

        RemoteInputStream loadAttachment(DomainObject attachmentDomainObject) throws RemoteException;

        void deleteAttachment(DomainObject attachmentDomainObject) throws RemoteException;

        List<DomainObject> getAttachmentDomainObjectsFor(DomainObject domainObject) throws RemoteException;
    }

    static public class AttachmentServiceRmiImpl implements AttachmentServiceRmi {
        AttachmentService attachmentService;

        public void setAttachmentService(AttachmentService attachmentService) {
            this.attachmentService = attachmentService;
        }

        public String saveAttachment(RemoteInputStream inputStream, DomainObject domainObject) throws RemoteException {
            attachmentService.saveAttachment(inputStream, domainObject);
            return ((StringValue) domainObject.getValue("path")).get();
        }

        public RemoteInputStream loadAttachment(DomainObject attachmentDomainObject) throws RemoteException {
            return attachmentService.loadAttachment(attachmentDomainObject);
        }

        public void deleteAttachment(DomainObject attachmentDomainObject) throws RemoteException {
            attachmentService.deleteAttachment(attachmentDomainObject);
        }

        public List<DomainObject> getAttachmentDomainObjectsFor(DomainObject domainObject) throws RemoteException {
            return attachmentService.getAttachmentDomainObjectsFor(domainObject);
        }
    }

    @Test
    public void testSaveAttachmentForInsert() throws Exception {
        InputStream istream = null;
        FileInputStream fis = null;
        try {
            byte[] expBytes = {1, 2, 3, 4, 5, 6, 7, 8, 9};
            ByteArrayInputStream bis = new ByteArrayInputStream(expBytes);
            SimpleRemoteInputStream stream = new SimpleRemoteInputStream(bis);
            DomainObject domainObject = new GenericDomainObjectWrapper();
            String path = stubAttachmentService.saveAttachment(stream.export(), domainObject);
            Assert.assertTrue(path != null);
            fis = new FileInputStream(path);
            ByteArrayOutputStream actBytes = new ByteArrayOutputStream();
            ByteStreams.copy(fis, actBytes);
            Assert.assertArrayEquals(expBytes, actBytes.toByteArray());
        } finally {
            //registry.unbind("AttachmentServiceRmi");
            if (istream != null) {
                istream.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
    }

    @Test
    public void testSaveAttachmentForUpdate() throws Exception {
        InputStream istream = null;
        FileInputStream fis1 = null;
        FileInputStream fis2 = null;
        try {
            byte[] expBytes = {1, 2, 3, 4, 5, 6, 7, 8, 9};
            ByteArrayInputStream bis = new ByteArrayInputStream(expBytes);
            SimpleRemoteInputStream stream = new SimpleRemoteInputStream(bis);
            DomainObject domainObject = new GenericDomainObjectWrapper();
            String path1 = stubAttachmentService.saveAttachment(stream.export(), domainObject);
            fis1 = new FileInputStream(path1);
            ByteArrayOutputStream actBytes = new ByteArrayOutputStream();
            ByteStreams.copy(fis1, actBytes);
            Assert.assertArrayEquals(expBytes, actBytes.toByteArray());

            expBytes = new byte[]{9, 8, 7, 6, 5, 4, 3, 2, 1};
            bis = new ByteArrayInputStream(expBytes);
            stream = new SimpleRemoteInputStream(bis);
            domainObject.setValue("path", new StringValue(path1));
            String path2 = stubAttachmentService.saveAttachment(stream.export(), domainObject);
            fis2 = new FileInputStream(path2);
            actBytes = new ByteArrayOutputStream();
            ByteStreams.copy(fis2, actBytes);
            Assert.assertArrayEquals(expBytes, actBytes.toByteArray());
            Assert.assertTrue(!path1.equalsIgnoreCase(path2));
            Assert.assertFalse(new File(path1).exists());
        } finally {
            if (istream != null) {
                istream.close();
            }
            if (fis1 != null) {
                fis1.close();
            }
            if (fis2 != null) {
                fis2.close();
            }
        }

    }

    @Test
    public void testGetAttachmentDomainObjectsFor() throws Exception {
        DomainObject domainObject = new GenericDomainObjectWrapper();
        domainObject.setTypeName("Person");
        List<DomainObject> l = stubAttachmentService.getAttachmentDomainObjectsFor(domainObject);
        Assert.assertEquals(1, ((RdbmsId) l.get(0).getId()).getId());
        Assert.assertEquals(2, ((RdbmsId) l.get(1).getId()).getId());
    }

    @Test
    public void testDeleteAttachment() throws Exception {
        try {
            byte[] expBytes = {1, 2, 3, 4, 5, 6, 7, 8, 9};
            ByteArrayInputStream bis = new ByteArrayInputStream(expBytes);
            SimpleRemoteInputStream stream = new SimpleRemoteInputStream(bis);
            DomainObject domainObject = new GenericDomainObjectWrapper();
            String path = stubAttachmentService.saveAttachment(stream.export(), domainObject);
            GenericDomainObjectWrapper delDO = new GenericDomainObjectWrapper();
            delDO.setValue("path", new StringValue(path));
            Assert.assertTrue(new File(path).exists());
            stubAttachmentService.deleteAttachment(delDO);
            Assert.assertFalse(new File(path).exists());
        } finally {
            //registry.unbind("AttachmentServiceRmi");
        }
    }

    @Test
    public void testLoadAttachment() throws Exception {
        InputStream contentStream = null;
        try {
            byte[] expBytes = {1, 2, 3, 4, 5, 6, 7, 8, 9};
            ByteArrayInputStream bis = new ByteArrayInputStream(expBytes);
            SimpleRemoteInputStream stream = new SimpleRemoteInputStream(bis);
            DomainObject domainObject = new GenericDomainObjectWrapper();
            String path = stubAttachmentService.saveAttachment(stream.export(), domainObject);
            Assert.assertTrue(new File(path).exists());
            GenericDomainObjectWrapper loadDO = new GenericDomainObjectWrapper();
            loadDO.setValue("path", new StringValue(path));
            Assert.assertTrue(new File(path).exists());
            RemoteInputStream inputStream = stubAttachmentService.loadAttachment(loadDO);
            contentStream = RemoteInputStreamClient.wrap(inputStream);
            ByteArrayOutputStream actBytes = new ByteArrayOutputStream();
            ByteStreams.copy(contentStream, actBytes);
            Assert.assertArrayEquals(expBytes, actBytes.toByteArray());
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        } finally {
            contentStream.close();
            //registry.unbind("AttachmentServiceRmi");
        }
    }

    private DomainObjectTypeConfig createEmployee() {
        DomainObjectTypeConfig result = new DomainObjectTypeConfig();
        result.setName("Employee");

        return result;
    }

    static private ConfigurationExplorer getConfigurationExplorer() throws Exception {
        ru.intertrust.cm.core.config.model.Configuration configuration = new ru.intertrust.cm.core.config.model.Configuration();
        DomainObjectTypeConfig dot = new DomainObjectTypeConfig();
        dot.setName("Person");
        AttachmentTypesConfig attachmentTypesConfig = new AttachmentTypesConfig();
        List<AttachmentTypeConfig> attachmentTypeConfigs = new ArrayList<>();
        AttachmentTypeConfig typeConfig = new AttachmentTypeConfig();
        typeConfig.setName("Person_Attachment");
        attachmentTypeConfigs.add(typeConfig);
        attachmentTypesConfig.setAttachmentTypeConfigs(attachmentTypeConfigs);
        dot.setAttachmentTypesConfig(attachmentTypesConfig);
        configuration.getConfigurationList().add(dot);
        dot = new DomainObjectTypeConfig();
        dot.setName("Attachment");
        dot.setTemplate(true);
        configuration.getConfigurationList().add(dot);

        ConfigurationExplorer configurationExplorer = new ConfigurationExplorerImpl(configuration);
        configurationExplorer.build();

        dot = configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Person");
        Assert.assertNotNull(dot);
        Assert.assertFalse(dot.isTemplate());
        dot = configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Attachment");
        Assert.assertNotNull(dot);
        Assert.assertTrue(dot.isTemplate());
        dot = configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Person_Attachment");
        Assert.assertNotNull(dot);
        Assert.assertFalse(dot.isTemplate());

        return configurationExplorer;
    }

    static private DomainObjectDao fillDomainObjectDao(DomainObjectDao domainObjectDao) {
        new RdbmsId("PERSON|1");
        any(Id.class);
        GenericDomainObject domainObject1 = new GenericDomainObjectWrapper();
        domainObject1.setTypeName("Person_Attachment");
        domainObject1.setId(new RdbmsId("Person_Attachment|1"));
        GenericDomainObject domainObject2 = new GenericDomainObjectWrapper();
        domainObject2.setTypeName("Person_Attachment");
        domainObject2.setId(new RdbmsId("Person_Attachment|2"));
        when(domainObjectDao.findChildren(any(Id.class), "Person_Attachment")).
            thenReturn(Arrays.asList(new DomainObject[]{domainObject1, domainObject2}));
        return domainObjectDao;
    }
}
