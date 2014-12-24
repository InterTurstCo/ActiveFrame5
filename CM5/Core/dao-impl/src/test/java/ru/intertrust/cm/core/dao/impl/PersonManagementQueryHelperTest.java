package ru.intertrust.cm.core.dao.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonManagementQueryHelperTest {

    private static final String ACCESS_RIGHTS_PART = " and exists (select a.object_id from person_read a  " +
            "inner join \"group_group\" gg on a.\"group_id\" = gg.\"parent_group_id\" "
            + "inner join \"group_member\" gm on gg.\"child_group_id\" = gm.\"usergroup\" "
            + "inner join \"person\" o on (o.\"access_object_id\" = a.\"object_id\") where gm.person_id = :user_id and o.id = :id)";

    private static final String ACCESS_RIGHTS_PART2 = " and exists (select a.object_id from user_group_read a  " +
            "inner join \"group_group\" gg on a.\"group_id\" = gg.\"parent_group_id\" "
            + "inner join \"group_member\" gm on gg.\"child_group_id\" = gm.\"usergroup\" "
            + "inner join \"user_group\" o on (o.\"access_object_id\" = a.\"object_id\") where gm.person_id = :user_id and o.id = :id)";

    private final PersonManagementQueryHelper queryHelper = new PersonManagementQueryHelper();

    private ConfigurationExplorerImpl configurationExplorer;

    private DomainObjectTypeConfig domainObjectTypeConfig;

    @Before
    public void setUp() throws Exception {
        initConfigs();
    }

    @Test
    public void testGenerateFindPersonsInGroupQuery() throws Exception {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select person.* from \"person\" person inner join \"group_member\" gm on " +
                "(gm.\"person_id\" = person.\"id\" and gm.\"person_id_type\" = person.\"id_type\") where " +
                "gm.\"usergroup\"=:id and gm.\"usergroup_type\"=:id_type" + ACCESS_RIGHTS_PART;
        Assert.assertEquals(expectedQuery, queryHelper.generateFindPersonsInGroupQuery("Person", accessToken));
    }

    @Test
    public void testGenerateFindAllPersonsInGroupQuery() throws Exception {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select person.* from \"person\" person inner join \"group_member\" gm on " +
                "(gm.\"person_id\" = person.\"id\" and gm.\"person_id_type\" = person.\"id_type\") " +
                "inner join \"group_group\" gg on (gg.\"child_group_id\" = gm.\"usergroup\" and " +
                "gg.\"child_group_id_type\" = gm.\"usergroup_type\") " +
                "where gg.\"parent_group_id\"=:id and gg.\"parent_group_id_type\"=:id_type" + ACCESS_RIGHTS_PART;
        Assert.assertEquals(expectedQuery, queryHelper.generateFindAllPersonsInGroupQuery("Person", accessToken));
    }

    @Test
    public void testGenerateFindPersonGroups() throws Exception {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select user_group.* from \"user_group\" user_group  inner join \"group_group\" gg on " +
                "(gg.\"parent_group_id\" = user_group.\"id\" and gg.\"parent_group_id_type\" = user_group.\"id_type\") " +
                "inner join \"group_member\" gm on (gm.\"usergroup\" = gg.\"child_group_id\" and " +
                "gm.\"usergroup_type\" = gg.\"child_group_id\") " +
                "where gm.\"person_id\"=:id and gm.\"person_id_type\"=:id_type" + ACCESS_RIGHTS_PART2;
        Assert.assertEquals(expectedQuery, queryHelper.generateFindPersonGroups("User_Group", accessToken));
    }

    @Test
    public void testGenerateFindAllParentGroups() throws Exception {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select user_group.* from \"user_group\" user_group  inner join \"group_group\" gg on " +
                "(gg.\"parent_group_id\" = user_group.\"id\" and gg.\"parent_group_id_type\" = user_group.\"id_type\") " +
                "where gg.\"child_group_id\"=:id and gg.\"child_group_id_type\"=:id_type and " +
                "(\"user_group\".\"id\" <> :id or \"user_group\".\"id_type\" <> :id_type)" + ACCESS_RIGHTS_PART2;
        Assert.assertEquals(expectedQuery, queryHelper.generateFindAllParentGroups("User_Group", accessToken));
    }

    @Test
    public void testGenerateFindChildGroups() throws Exception {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select user_group.* from \"user_group\" user_group  inner join \"group_group_settings\" ggs on " +
                "(ggs.\"child_group_id\" = user_group.\"id\" and ggs.\"child_group_id_type\" = user_group.\"id_type\") " +
                "where ggs.\"parent_group_id\"=:id and ggs.\"parent_group_id_type\"=:id_type" + ACCESS_RIGHTS_PART2;
        Assert.assertEquals(expectedQuery, queryHelper.generateFindChildGroups("User_Group", accessToken));
    }

    @Test
    public void tesGenerateFindAllChildGroups() throws Exception {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select user_group.* from \"user_group\" user_group  inner join \"group_group\" gg on " +
                "(gg.\"child_group_id\" = user_group.\"id\" and gg.\"child_group_id_type\" = user_group.\"id_type\") " +
                "where gg.\"parent_group_id\"=:id and gg.\"parent_group_id_type\"=:id_type and " +
                "(\"user_group\".\"id\" <> :id or \"user_group\".\"id_type\" <> :id_type)" + ACCESS_RIGHTS_PART2;
        Assert.assertEquals(expectedQuery, queryHelper.generateFindAllChildGroups("User_Group", accessToken));
    }

    @Test
    public void tesGenerateFindDynamicGroup() throws Exception {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select user_group.* from \"user_group\" user_group " +
                "where \"user_group\".\"object_id\"=:id and \"user_group\".\"object_id_type\"=:id_type and " +
                "\"user_group\".\"group_name\"=:name" + ACCESS_RIGHTS_PART2;
        Assert.assertEquals(expectedQuery, queryHelper.generateFindDynamicGroup("User_Group", accessToken));
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
        internalEmployee.setExtendsAttribute("Person");

        DomainObjectTypeConfig assignment = new DomainObjectTypeConfig();
        assignment.setName("assignment");

        ReferenceFieldConfig author = new ReferenceFieldConfig();
        author.setName("author");
        author.setType("Person");

        assignment.getFieldConfigs().add(author);

        DomainObjectTypeConfig userGroup = new DomainObjectTypeConfig();
        userGroup.setName("User_Group");

        StringFieldConfig userGroupName = new StringFieldConfig();
        userGroupName.setName("group_name");
        userGroup.getFieldConfigs().add(userGroupName);

        ReferenceFieldConfig userGroupObjectId = new ReferenceFieldConfig();
        userGroupObjectId.setName("object_id");
        userGroupObjectId.setType("*");
        userGroup.getFieldConfigs().add(userGroupObjectId);


        Configuration configuration = new Configuration();
        configuration.getConfigurationList().add(domainObjectTypeConfig);
        configuration.getConfigurationList().add(internalEmployee);
        configuration.getConfigurationList().add(externalEmployee);
        configuration.getConfigurationList().add(assignment);
        configuration.getConfigurationList().add(globalSettings);
        configuration.getConfigurationList().add(userGroup);

        configurationExplorer = new ConfigurationExplorerImpl(configuration);
        queryHelper.setConfigurationExplorer(configurationExplorer);
    }

    private AccessToken createMockAccessToken() {
        AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.isDeferred()).thenReturn(true);

        UserSubject subject = mock(UserSubject.class);
        when(subject.getUserId()).thenReturn(1);
        when(accessToken.getSubject()).thenReturn(subject);
        return accessToken;
    }
}
