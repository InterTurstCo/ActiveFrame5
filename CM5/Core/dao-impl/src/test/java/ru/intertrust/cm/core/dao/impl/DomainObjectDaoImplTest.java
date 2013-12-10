package ru.intertrust.cm.core.dao.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.impl.utils.MultipleObjectRowMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Юнит тест для DomainObjectDaoImpl
 *
 * @author skashanski
 */

@RunWith(MockitoJUnitRunner.class)
public class DomainObjectDaoImplTest {

    @InjectMocks
    private final DomainObjectDaoImpl domainObjectDaoImpl = new DomainObjectDaoImpl();

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ConfigurationExplorerImpl configurationExplorer;

    @Mock
    private AccessControlService accessControlService;

    @Mock
    private DomainObjectCacheServiceImpl domainObjectCacheService;

    @Mock
    private ApplicationContext context;

    private DomainObjectTypeConfig domainObjectTypeConfig;

    @Before
    public void setUp() throws Exception {
        initConfigs();

        accessControlService = mock(AccessControlService.class);
        AccessToken mockAccessToken = createMockAccessToken();
        when(accessControlService.createSystemAccessToken(anyString())).thenReturn(mockAccessToken);
        domainObjectDaoImpl.setAccessControlService(accessControlService);

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
                "insert into \"person\" (\"id\", \"id_type\", \"created_date\", \"updated_date\", \"status\", " +
                        "\"status_type\", \"email\", \"login\", \"password\", \"boss\", \"boss_type\") values " +
                        "(:id , :type_id, :created_date, :updated_date, :status, :status_type, :email,:login," +
                        ":password,:boss,:boss_type)";

        String query = domainObjectDaoImpl.generateCreateQuery(domainObjectTypeConfig);
        assertEquals(checkCreateQuery, query);
    }

    @Test
    public void testGenerateFindQuery() throws Exception {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select person.* from \"person\" person where person.\"id\"=:id  " +
                "and exists (select a.object_id from Person_read a  inner join \"group_group\" gg " +
                "on a.\"group_id\" = gg.\"parent_group_id\" inner join \"group_member\" gm on gg.\"child_group_id\" " +
                "= gm.\"usergroup\" where gm.person_id = :user_id and a.object_id = :id)";
        Assert.assertEquals(expectedQuery, domainObjectDaoImpl.generateFindQuery("Person", accessToken, true));
    }

    @Test
    public void testGenerateFindQueryWithLock() throws Exception {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select person.* from \"person\" person where person.\"id\"=:id for update"/* +
                "  and " +
                "exists (select a.object_id from Person_READ a inner join group_member gm on " +
                "a.group_id = gm.usergroup where gm.person_id = :user_id and a.object_id = :id)"*/;
        Assert.assertEquals(expectedQuery, domainObjectDaoImpl.generateFindQuery("Person", accessToken, true));
    }

    //@Test Метод update удален, так как непонятно чем он отличается от save
    /*public void testUpdateThrowsInvalidIdException() {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setId(null);
        domainObject.setTypeName(domainObjectTypeConfig.getName());
        domainObject.setValue("EMail", new StringValue("testUpdate@test.com"));
        domainObject.setValue("Login", new StringValue("userUpdate"));
        domainObject.setValue("Password", new StringValue("passUpdate"));

        AccessToken accessToken = createMockAccessToken();
        //проверяем что идентификатор не нулевой
        try {
            domainObjectDaoImpl.update(domainObject, accessToken);
        } catch (Exception e) {

            assertTrue(e instanceof InvalidIdException);

        }

        //проверяем что обрабатываеться неккоректный тип идентификатора
        try {
            domainObject.setId(new TestId());
            domainObjectDaoImpl.update(domainObject, accessToken);
        } catch (Exception e) {

            assertTrue(e instanceof InvalidIdException);

        }
    }*/

    @Test
    public void testGenerateUpdateQuery() throws Exception {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(domainObjectTypeConfig.getName());
        domainObject.setValue("EMail", new StringValue("testUpdate@test.com"));
        domainObject.setValue("Login", new StringValue("userUpdate"));
        domainObject.setValue("Password", new StringValue("passUpdate"));

        Date currentDate = new Date();
        domainObject.setId(new RdbmsId(1, 1));
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);

        String checkUpdateQuery = "update \"person\" set \"updated_date\"=:current_date, \"status\"=:status, " +
                "\"email\"=:email, \"login\"=:login, \"password\"=:password, \"boss\"=:boss, " +
                "\"boss_type\"=:boss_type where \"id\"=:id and \"updated_date\"=:updated_date";

        String query = domainObjectDaoImpl.generateUpdateQuery(domainObjectTypeConfig, true);
        assertEquals(checkUpdateQuery, query);
    }

    @Test
    public void testGenerateDeleteQuery() throws Exception {
        String checkDeleteQuery = "delete from \"person\" where \"id\"=:id";

        String query = domainObjectDaoImpl.generateDeleteQuery(domainObjectTypeConfig);
        assertEquals(checkDeleteQuery, query);
    }

    @Test
    public void testGenerateDeleteAllQuery() throws Exception {
        String checkDeleteQuery = "delete from \"person\"";

        String query = domainObjectDaoImpl.generateDeleteAllQuery(domainObjectTypeConfig);
        assertEquals(checkDeleteQuery, query);
    }


    @Test
    public void testGenerateExistsQuery() throws Exception {
        String checkExistsQuery = "select \"id\" from \"person\" where \"id\"=:id";

        String query = domainObjectDaoImpl.generateExistsQuery(domainObjectTypeConfig.getName());
        assertEquals(checkExistsQuery, query);
    }


    @Test
    public void testGenerateFindChildrenQuery() {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select assignment.* from \"assignment\" assignment where assignment.\"author\" = " +
                ":domain_object_id"/* +
        		" and exists" +
                " (select r.object_id from assignment_READ r inner join group_member " +
                "gm on r.group_id = gm.usergroup where gm.person_id = :user_id and r.object_id = t.id)"*/;
        Assert.assertEquals(expectedQuery, domainObjectDaoImpl.buildFindChildrenQuery("assignment", "author",
                0, 0, accessToken));

    }

    @Test
    public void testGenerateFindChildrenQueryForInheritedField() {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select internal_employee.*, " +
                "person.\"email\", person.\"login\", person.\"password\", person.\"boss\", person.\"boss_type\", " +
                "\"created_date\", \"updated_date\", \"status\", \"status_type\" " +
                "from \"internal_employee\" internal_employee inner join \"person\" person on " +
                "internal_employee.\"id\" = person.\"id\" where person.\"boss\" = :domain_object_id"/* +
        		" and exists" +
                " (select r.object_id from assignment_READ r inner join group_member " +
                "gm on r.group_id = gm.usergroup where gm.person_id = :user_id and r.object_id = t.id)"*/;
        Assert.assertEquals(expectedQuery, domainObjectDaoImpl.buildFindChildrenQuery("Internal_Employee", "Boss",
                0, 0, accessToken));

    }

    @Test
    public void testGenerateFindChildrenIdsQuery() {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select t.id from assignment t where t.author = :domain_object_id and exists " +
                "(select r.object_id from assignment_read r  inner join \"group_group\" gg on r.\"group_id\" = " +
                "gg.\"parent_group_id\" inner join \"group_member\" gm on gg.\"child_group_id\" = " +
                "gm.\"usergroup\"where gm.person_id = :user_id and r.object_id = t.id)";
        Assert.assertEquals(expectedQuery, domainObjectDaoImpl.buildFindChildrenIdsQuery("assignment", "author",
                0, 0, accessToken));

    }

    @Test
    public void testGenerateFindChildrenIdsQueryForInheritedField() {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery =
                "select person.\"id\" from \"person\" person where person.\"boss\" = :domain_object_id and exists " +
                "(select r.object_id from Internal_Employee_read r  inner join \"group_group\" gg on " +
                "r.\"group_id\" = gg.\"parent_group_id\" inner join \"group_member\" gm on " +
                "gg.\"child_group_id\" = gm.\"usergroup\"where gm.person_id = :user_id and r.object_id = t\"id\")";
                

        Assert.assertEquals(expectedQuery, domainObjectDaoImpl.buildFindChildrenIdsQuery("Internal_Employee", "Boss",
                0, 0, accessToken));

    }

    private void initConfigs() {

        /*
         * Создаем конфигурацию следующего ввида <domain-object name="Person">
         * <fields> <string name="EMail" length="128" /> <string name="Login"
         * length="64" not-null="true" /> <password name="Password" length="128"
         * /> </fields> <uniqueKey> <!-- This key means automatic key + index
         * creation--> <field name="EMail"/> </uniqueKey> </domain-object>
         */
        GlobalSettingsConfig globalSettings = new GlobalSettingsConfig();
        domainObjectTypeConfig = new DomainObjectTypeConfig();
        domainObjectTypeConfig.setName("Person");
        StringFieldConfig email = new StringFieldConfig();
        email.setName("EMail");
        email.setLength(128);
        domainObjectTypeConfig.getFieldConfigs().add(email);

        StringFieldConfig login = new StringFieldConfig();
        login.setName("Login");
        login.setLength(64);
        login.setNotNull(true);
        domainObjectTypeConfig.getFieldConfigs().add(login);

        StringFieldConfig password = new StringFieldConfig();
        password.setName("Password");
        password.setLength(128);
        domainObjectTypeConfig.getFieldConfigs().add(password);

        ReferenceFieldConfig boss = new ReferenceFieldConfig();
        boss.setName("Boss");
        boss.setType("Internal_Employee");
        domainObjectTypeConfig.getFieldConfigs().add(boss);


        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        domainObjectTypeConfig.getUniqueKeyConfigs().add(uniqueKeyConfig);

        UniqueKeyFieldConfig uniqueKeyFieldConfig1 = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig1.setName("EMail");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig1);

        DomainObjectTypeConfig internalEmployee = new DomainObjectTypeConfig();
        internalEmployee.setName("Internal_Employee");
        internalEmployee.setExtendsAttribute("Person");

        DomainObjectTypeConfig externalEmployee = new DomainObjectTypeConfig();
        externalEmployee.setName("External_Employee");

        DomainObjectTypeConfig assignment = new DomainObjectTypeConfig();
        assignment.setName("assignment");

        ReferenceFieldConfig author = new ReferenceFieldConfig();
        author.setName("author");
        author.setType("Person");

        assignment.getFieldConfigs().add(author);


        Configuration configuration = new Configuration();
        configuration.getConfigurationList().add(domainObjectTypeConfig);
        configuration.getConfigurationList().add(internalEmployee);
        configuration.getConfigurationList().add(externalEmployee);
        configuration.getConfigurationList().add(assignment);
        configuration.getConfigurationList().add(globalSettings);

        configurationExplorer = new ConfigurationExplorerImpl(configuration);
        domainObjectDaoImpl.setConfigurationExplorer(configurationExplorer);
    }

    @Test
    public void testGetAttachmentDomainObjectsFor() throws Exception {
        Configuration configuration = new Configuration();
        GlobalSettingsConfig globalSettings = new GlobalSettingsConfig();
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
        configuration.getConfigurationList().add(globalSettings);

        ConfigurationExplorer configurationExplorer = new ConfigurationExplorerImpl(configuration);

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
        domainObject.setId(new RdbmsId(1, 1));
        when(result.get(0)).thenReturn(domainObject);

        domainObject = new GenericDomainObject();
        domainObject.setTypeName("Person1_Attachment");
        domainObject.setId(new RdbmsId(1, 2));
        when(result.get(1)).thenReturn(domainObject);

        any(MultipleObjectRowMapper.class);

        when(jdbcTemplate.query("select t.* from PERSON1_ATTACHMENT t where usergroup = :usergroup",
                any(HashMap.class),
                any(MultipleObjectRowMapper.class))).thenReturn(result);

        DomainObjectDaoImpl domainObjectDao = new DomainObjectDaoImpl();
        domainObjectDao.setConfigurationExplorer(configurationExplorer);
        domainObjectDaoImpl.setConfigurationExplorer(configurationExplorer);

        when(domainObjectCacheService.getObjectToCache(any(Id.class),
                any(String.class), any(String.class), any(String.class), any(String.class))).thenReturn(null);
        domainObjectDao.setDomainObjectCacheService(domainObjectCacheService);
        ReflectionTestUtils.setField(domainObjectDao, "jdbcTemplate", jdbcTemplate);

        AccessToken accessToken = createMockAccessToken();

        List<DomainObject> l = domainObjectDao.findLinkedDomainObjects(new RdbmsId(1, 1), "Person1_Attachment",
                "Person", accessToken);
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
