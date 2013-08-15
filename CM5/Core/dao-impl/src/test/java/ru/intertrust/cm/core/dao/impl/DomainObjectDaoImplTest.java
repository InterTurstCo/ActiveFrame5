package ru.intertrust.cm.core.dao.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.model.*;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.impl.utils.MultipleObjectRowMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Юнит тест для DomainObjectDaoImpl
 *
 * @author skashanski
 */

@RunWith(MockitoJUnitRunner.class)
public class DomainObjectDaoImplTest {

    private final IdService idService = new IdService();

    @InjectMocks
    private DomainObjectDaoImpl domainObjectDaoImpl = new DomainObjectDaoImpl();

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ConfigurationExplorerImpl configurationExplorer;

    @Mock
    private DomainObjectCacheServiceImpl domainObjectCacheService;

    private DomainObjectTypeConfig domainObjectTypeConfig;

    @Before
    public void setUp() throws Exception {
        initDomainObjectConfig();
    }

    @Test
    public void testGenerateCreateQuery() throws Exception {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(domainObjectTypeConfig.getName());
        domainObject.setValue("EMail", new StringValue("testCreate@test.com"));
        domainObject.setValue("Login", new StringValue("userCreate"));
        domainObject.setValue("Password", new StringValue("passCreate"));

        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);

        String checkCreateQuery =
                "insert into PERSON (ID, CREATED_DATE, UPDATED_DATE, EMAIL," +
                        "LOGIN,PASSWORD) values " +
                        "(:id , :created_date, :updated_date, :email,:login,:password)";

        String query = domainObjectDaoImpl.generateCreateQuery(domainObjectTypeConfig);
        assertEquals(checkCreateQuery, query);
    }

    @Test
    public void testUpdateThrowsInvalidIdException() {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setId(null);
        domainObject.setTypeName(domainObjectTypeConfig.getName());
        domainObject.setValue("EMail", new StringValue("testUpdate@test.com"));
        domainObject.setValue("Login", new StringValue("userUpdate"));
        domainObject.setValue("Password", new StringValue("passUpdate"));

        //проверяем что идентификатор не нулевой
        try {
            domainObjectDaoImpl.update(domainObject);
        } catch (Exception e) {

            assertTrue(e instanceof InvalidIdException);

        }

        //проверяем что обрабатываеться неккоректный тип идентификатора
        try {
            domainObject.setId(new TestId());
            domainObjectDaoImpl.update(domainObject);
        } catch (Exception e) {

            assertTrue(e instanceof InvalidIdException);

        }
    }

    @Test
    public void testGenerateUpdateQuery() throws Exception {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(domainObjectTypeConfig.getName());
        domainObject.setValue("EMail", new StringValue("testUpdate@test.com"));
        domainObject.setValue("Login", new StringValue("userUpdate"));
        domainObject.setValue("Password", new StringValue("passUpdate"));

        Date currentDate = new Date();
        domainObject.setId(new RdbmsId("person", 1));
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);

        String checkUpdateQuery = "update PERSON set UPDATED_DATE=:current_date, " +
                "EMAIL=:email, LOGIN=:login, PASSWORD=:password where ID=:id and UPDATED_DATE=:updated_date";

        String query = domainObjectDaoImpl.generateUpdateQuery(domainObjectTypeConfig);
        assertEquals(checkUpdateQuery, query);
    }

    @Test
    public void testGenerateDeleteQuery() throws Exception {
        String checkDeleteQuery = "delete from PERSON where id=:id";

        String query = domainObjectDaoImpl.generateDeleteQuery(domainObjectTypeConfig);
        assertEquals(checkDeleteQuery, query);
    }

    @Test
    public void testGenerateDeleteAllQuery() throws Exception {
        String checkDeleteQuery = "delete from PERSON";

        String query = domainObjectDaoImpl.generateDeleteAllQuery(domainObjectTypeConfig);
        assertEquals(checkDeleteQuery, query);
    }


    @Test
    public void testGenerateExistsQuery() throws Exception {
        String checkExistsQuery = "select id from PERSON where id=:id";

        String query = domainObjectDaoImpl.generateExistsQuery(domainObjectTypeConfig.getName());
        assertEquals(checkExistsQuery, query);
    }


    @Test
    public void testGenerateFindChildrenQuery() {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select t.* from assignment t where t.author = :domain_object_id and exists" +
                " (select r.object_id from assignment_READ r inner join group_member " +
                "gm on r.group_id = gm.parent where gm.person_id = :user_id and r.object_id = t.id)";
        Assert.assertEquals(expectedQuery, domainObjectDaoImpl.buildFindChildrenQuery("assignment", "author", accessToken));

    }

    @Test
    public void testGenerateFindChildrenIdsQuery() {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select t.id from assignment t where t.author = :domain_object_id and exists" +
                " (select r.object_id from assignment_READ r inner join group_member " +
                "gm on r.group_id = gm.parent where gm.person_id = :user_id and r.object_id = t.id)";
        Assert.assertEquals(expectedQuery, domainObjectDaoImpl.buildFindChildrenIdsQuery("assignment", "author", accessToken));

    }


    private void initDomainObjectConfig() {

        /*
         * Создаем конфигурацию следующего ввида <domain-object name="Person">
         * <fields> <string name="EMail" length="128" /> <string name="Login"
         * length="64" not-null="true" /> <password name="Password" length="128"
         * /> </fields> <uniqueKey> <!-- This key means automatic key + index
         * creation--> <field name="EMail"/> </uniqueKey> </domain-object>
         */

        domainObjectTypeConfig = new DomainObjectTypeConfig();
        domainObjectTypeConfig.setName("Person");
        StringFieldConfig email = new StringFieldConfig();
        email.setName("EMail");
        email.setLength(128);
        domainObjectTypeConfig.getFieldConfigs().add(email);

        StringFieldConfig Login = new StringFieldConfig();
        Login.setName("Login");
        Login.setLength(64);
        Login.setNotNull(true);
        domainObjectTypeConfig.getFieldConfigs().add(Login);

        StringFieldConfig Password = new StringFieldConfig();
        Password.setName("Password");
        Password.setLength(128);
        domainObjectTypeConfig.getFieldConfigs().add(Password);

        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        domainObjectTypeConfig.getUniqueKeyConfigs().add(uniqueKeyConfig);

        UniqueKeyFieldConfig uniqueKeyFieldConfig1 = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig1.setName("EMail");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig1);

    }

    @Test
    public void testGetAttachmentDomainObjectsFor() throws Exception {
        ru.intertrust.cm.core.config.model.Configuration configuration = new ru.intertrust.cm.core.config.model.Configuration();
        DomainObjectTypeConfig dot = new DomainObjectTypeConfig();
        dot.setName("Person");
        AttachmentTypesConfig attachmentTypesConfig = new AttachmentTypesConfig();
        List<AttachmentTypeConfig> attachmentTypeConfigs = new ArrayList<>();
        AttachmentTypeConfig typeConfig = new AttachmentTypeConfig();
        typeConfig.setName("Person1_Attachment");
        attachmentTypeConfigs.add(typeConfig);
        typeConfig = new AttachmentTypeConfig();
        typeConfig.setName("Person2_Attachment");
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
        dot = configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Person1_Attachment");
        Assert.assertNotNull(dot);
        Assert.assertFalse(dot.isTemplate());
        dot = configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Person2_Attachment");
        Assert.assertNotNull(dot);
        Assert.assertFalse(dot.isTemplate());

        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        ArrayList<DomainObject> result = mock(ArrayList.class);
        when(result.size()).thenReturn(2);

        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName("Person1_Attachment");
        domainObject.setId(idService.createId("Person1_Attachment|1"));
        when(result.get(0)).thenReturn(domainObject);

        domainObject = new GenericDomainObject();
        domainObject.setTypeName("Person1_Attachment");
        domainObject.setId(idService.createId("Person1_Attachment|2"));
        when(result.get(1)).thenReturn(domainObject);

        any(MultipleObjectRowMapper.class);

        when(jdbcTemplate.query("select t.* from PERSON1_ATTACHMENT t where parent = :parent_id",
                any(HashMap.class),
                any(MultipleObjectRowMapper.class))).thenReturn(result);

        DomainObjectDaoImpl domainObjectDao = new DomainObjectDaoImpl();

        when(domainObjectCacheService.getObjectToCache(any(Id.class),
                any(String.class), any(String.class))).thenReturn(null);
        domainObjectDao.setDomainObjectCacheService(domainObjectCacheService);
        ReflectionTestUtils.setField(domainObjectDao, "jdbcTemplate", jdbcTemplate);

        AccessToken accessToken = createMockAccessToken();

        List<DomainObject> l = domainObjectDao.findChildren(idService.createId("PERSON|1"), "Person1_Attachment", accessToken);
        Assert.assertEquals(1, ((RdbmsId) l.get(0).getId()).getId());
        Assert.assertEquals(2, ((RdbmsId) l.get(1).getId()).getId());
    }


    private AccessToken createMockAccessToken() {
        AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.isDeferred()).thenReturn(true);

        UserSubject subject = mock(UserSubject.class);
        when(subject.getUserId()).thenReturn(1);
        when(accessToken.getSubject()).thenReturn(subject);
        return accessToken;
    }

    /**
     * Тестовый класс для проверки обработки неккоректного типа идентификатора
     *
     * @author skashanski
     */
    class TestId implements Id {

        @Override
        public void setFromStringRepresentation(String stringRep) {
            // TODO Auto-generated method stub

        }

        @Override
        public String toStringRepresentation() {
            // TODO Auto-generated method stub
            return null;
        }

    }

}
