package ru.intertrust.cm.core.business.impl;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.impl.RdbmsIdServiceImpl;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.FormLogicalValidator;
import ru.intertrust.cm.core.config.NavigationPanelLogicalValidator;
import ru.intertrust.cm.core.config.model.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.model.AttachmentTypesConfig;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
//import static org.powermock.api.support.membermodification.MemberMatcher.method;
//import static org.powermock.api.support.membermodification.MemberModifier.replace;

/**
 * @author Vlad
 */
//@RunWith(PowerMockRunner.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
//@PrepareForTest({ConfigurationExplorerImpl.class, AttachmentContentDao.class})
public class AttachmentServiceImplTest {

    static private final String TEST_OUT_DIR = System.getProperty("test.cnf.testOutDir");
    static private final int PORT_RMI = Integer.parseInt(System.getProperty("test.cnf.portRmi"));
    private static final int BUF_SIZE = 0x1000;
    private static String absDirPath;
    private static Long suffix;

    private static IdService idService = new RdbmsIdServiceImpl();

    static private AttachmentServiceRmi stubAttachmentService;
    static private AttachmentServiceRmiImpl serviceRmi;

    @Autowired
    ApplicationContext context;

    static class GenericDomainObjectWrapper extends GenericDomainObject {
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
    static class ContextConfiguration {
        {
            MockitoAnnotations.initMocks(this);
        }

        @Mock
        private ConfigurationExplorer configurationExplorer;

        @Mock
        private DomainObjectDao domainObjectDao;

        @Mock
        private AttachmentContentDao attachmentContentDao;

        @Mock
        private AccessControlService accessControlService;

        @Mock
        AccessToken accessToken;

        @Mock
        private DomainObjectTypeIdCache domainObjectTypeIdCache;

        @Mock
        private FormLogicalValidator formLogicalValidator;

        @Mock
        private NavigationPanelLogicalValidator navigationPanelLogicalValidator;

        @Mock
        private ApplicationContext context;

        @Bean
        public ApplicationContext context() {
            return context;
        }

        @Bean
        public AccessControlService accessControlService() {
            return accessControlService;
        }

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
        public DomainObjectTypeIdCache getDomainObjectTypeIdCache() {
            return domainObjectTypeIdCache;
        }

        @Bean
        public AttachmentContentDao attachmentContentDao() {
            doAnswer(new Answer() {
                public Object answer(InvocationOnMock invocation) {
                    InputStream inputStream = (InputStream) invocation.getArguments()[0];
                    return saveContent(inputStream);
                }
            }).when(attachmentContentDao).saveContent(any(InputStream.class));
            doAnswer(new Answer() {
                public Object answer(InvocationOnMock invocation) {
                    DomainObject domainObject = (DomainObject) invocation.getArguments()[0];
                    deleteContent(domainObject);
                    return null;
                }
            }).when(attachmentContentDao).deleteContent(any(DomainObject.class));
            doAnswer(new Answer() {
                public Object answer(InvocationOnMock invocation) {
                    DomainObject domainObject = (DomainObject) invocation.getArguments()[0];
                    return loadContent(domainObject);
                }
            }).when(attachmentContentDao).loadContent(any(DomainObject.class));
            return attachmentContentDao;
        }

        @Bean
        public DomainObjectDao domainObjectDao() {
            when(accessToken.isDeferred()).thenReturn(true);
            return fillDomainObjectDao(domainObjectDao, accessToken);
        }

        @Bean
        public CrudService crudService() {
            return new CrudServiceImpl();
        }

        @Bean
        public FormLogicalValidator formLogicalValidator() {
            return formLogicalValidator;
        }

        @Bean
        public NavigationPanelLogicalValidator navigationPanelLogicalValidator() {
            return navigationPanelLogicalValidator;
        }
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        suffix = 0l;
        absDirPath = Paths.get(TEST_OUT_DIR, "/AttachmentServiceImplTest").toAbsolutePath().toString();
        File dir = new File(absDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        if (stubAttachmentService == null) {
            Registry registry = LocateRegistry.createRegistry(PORT_RMI);
            serviceRmi = new AttachmentServiceRmiImpl();
            serviceRmi.setAttachmentService(attachmentService);
            registry.rebind("AttachmentServiceRmi", UnicastRemoteObject.exportObject(serviceRmi, 0));
            stubAttachmentService = (AttachmentServiceRmi) LocateRegistry.getRegistry(PORT_RMI).lookup("AttachmentServiceRmi");
        }
    }

    @After
    public void setDown() throws RemoteException, NotBoundException {
        //UnicastRemoteObject.unexportObject(serviceRmi, true);
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
        try {
            byte[] expBytes = {1, 2, 3, 4, 5, 6, 7, 8, 9};
            ByteArrayInputStream bis = new ByteArrayInputStream(expBytes);
            SimpleRemoteInputStream stream = new SimpleRemoteInputStream(bis);
            DomainObject domainObject = new GenericDomainObjectWrapper();
            RemoteInputStream remoteInputStream = stream.export();
            String path = stubAttachmentService.saveAttachment(remoteInputStream, domainObject);
            Assert.assertTrue(path != null);
            ByteArrayOutputStream actBytes = new ByteArrayOutputStream();
            Files.copy(Paths.get(absDirPath, path), actBytes);
            Assert.assertArrayEquals(expBytes, actBytes.toByteArray());
        } finally {
            //registry.unbind("AttachmentServiceRmi");
            if (istream != null) {
                istream.close();
            }
        }
    }

    @Test
    public void testSaveAttachmentForUpdate() throws Exception {
        InputStream istream = null;
        try {
            byte[] expBytes = {1, 2, 3, 4, 5, 6, 7, 8, 9};
            ByteArrayInputStream bis = new ByteArrayInputStream(expBytes);
            SimpleRemoteInputStream stream = new SimpleRemoteInputStream(bis);
            DomainObject domainObject = new GenericDomainObjectWrapper();
            String path1 = stubAttachmentService.saveAttachment(stream.export(), domainObject);
            ByteArrayOutputStream actBytes = new ByteArrayOutputStream();
            Files.copy(Paths.get(absDirPath, path1), actBytes);
            Assert.assertArrayEquals(expBytes, actBytes.toByteArray());

            expBytes = new byte[]{9, 8, 7, 6, 5, 4, 3, 2, 1};
            bis = new ByteArrayInputStream(expBytes);
            stream = new SimpleRemoteInputStream(bis);
            domainObject.setValue("path", new StringValue(path1));
            String path2 = stubAttachmentService.saveAttachment(stream.export(), domainObject);
            actBytes = new ByteArrayOutputStream();
            Files.copy(Paths.get(absDirPath, path2), actBytes);
            Assert.assertArrayEquals(expBytes, actBytes.toByteArray());
            Assert.assertTrue(!path1.equalsIgnoreCase(path2));
            Assert.assertFalse(new File(path1).exists());
        } finally {
            if (istream != null) {
                istream.close();
            }
        }

    }

    @Test
    public void testGetAttachmentDomainObjectsFor() throws Exception {
        GenericDomainObject domainObject = new GenericDomainObjectWrapper();
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
            Assert.assertTrue(Paths.get(absDirPath, path).toFile().exists());
            stubAttachmentService.deleteAttachment(delDO);
            Assert.assertFalse(Paths.get(absDirPath, path).toFile().exists());
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
            Assert.assertTrue(Paths.get(absDirPath, path).toFile().exists());
            GenericDomainObjectWrapper loadDO = new GenericDomainObjectWrapper();
            loadDO.setValue("path", new StringValue(path));
            Assert.assertTrue(Paths.get(absDirPath, path).toFile().exists());
            RemoteInputStream inputStream = stubAttachmentService.loadAttachment(loadDO);
            contentStream = RemoteInputStreamClient.wrap(inputStream);
            ByteArrayOutputStream actBytes = new ByteArrayOutputStream();
            copy(contentStream, actBytes);
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
        ru.intertrust.cm.core.config.model.base.Configuration configuration =
                new ru.intertrust.cm.core.config.model.base.Configuration();
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

    static private DomainObjectDao fillDomainObjectDao(DomainObjectDao domainObjectDao, AccessToken accessToken) {
        GenericDomainObject domainObject1 = new GenericDomainObjectWrapper();
        domainObject1.setTypeName("Person_Attachment");
        domainObject1.setId(idService.createId("0001000000000001"));
        GenericDomainObject domainObject2 = new GenericDomainObjectWrapper();
        domainObject2.setTypeName("Person_Attachment");
        domainObject2.setId(idService.createId("0001000000000002"));

        when(domainObjectDao.findLinkedDomainObjects(any(Id.class), eq("Person_Attachment"), eq("Person_Attachment"),
                any(AccessToken.class))).
                thenReturn(Arrays.asList(new DomainObject[]{domainObject1, domainObject2}));
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                DomainObject domainObject = (DomainObject) invocation.getArguments()[0];
                return domainObject;
            }
        }).when(domainObjectDao).save(any(DomainObject.class));
        return domainObjectDao;
    }

    private static void copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
        }
    }

    private static InputStream loadContent(DomainObject domainObject) {
        try {
            String fileName = ((StringValue) domainObject.getValue("path")).get();
            return new FileInputStream(Paths.get(absDirPath, fileName).toFile());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String saveContent(InputStream inputStream) {
        Path path = Paths.get(absDirPath, (suffix++).toString());
        try {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            return path.subpath(Paths.get(absDirPath).getNameCount(), path.getNameCount()).toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void deleteContent(DomainObject domainObject) {
        String fileName = ((StringValue) domainObject.getValue("path")).get();
        try {
            Paths.get(absDirPath, fileName).toFile().delete();
        } catch (RuntimeException ex) {
        }
    }
}
