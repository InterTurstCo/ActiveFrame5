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

public class SchedulerQueryHelperTest {

    private static final String ACCESS_RIGHTS_PART = " and exists (select a.object_id from schedule_read a  " +
            "inner join \"group_group\" gg on a.\"group_id\" = gg.\"parent_group_id\" "
            + "inner join \"group_member\" gm on gg.\"child_group_id\" = gm.\"usergroup\" "
            + "inner join \"schedule\" o on (o.\"access_object_id\" = a.\"object_id\") where gm.person_id = :user_id and o.id = :id)";

    private static final String ACCESS_RIGHTS_PART2 = " and exists (select a.object_id from schedule_read a  " +
            "inner join \"group_group\" gg on a.\"group_id\" = gg.\"parent_group_id\" "
            + "inner join \"group_member\" gm on gg.\"child_group_id\" = gm.\"usergroup\" "
            + "inner join \"schedule\" o on (o.\"access_object_id\" = a.\"object_id\") where gm.person_id = :user_id and o.id = :id)";

    private final SchedulerQueryHelper queryHelper = new SchedulerQueryHelper();

    private ConfigurationExplorerImpl configurationExplorer;

    private DomainObjectTypeConfig domainObjectTypeConfig;

    @Before
    public void setUp() throws Exception {
        initConfigs();
    }

    @Test
    public void testGenerateFindTasksByStatusQuery() throws Exception {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select schedule.* from \"schedule\" schedule inner join \"status\" s on " +
                "(s.\"id\" = schedule.\"status\" and s.\"id_type\" = schedule.\"status_type\") " +
                "where s.\"name\"=:status and \"active\" = 1" + ACCESS_RIGHTS_PART + " order by schedule.\"priority\"";
        Assert.assertEquals(expectedQuery, queryHelper.generateFindTasksByStatusQuery("schedule", accessToken, true));

        String expectedQuery2 = "select schedule.* from \"schedule\" schedule inner join \"status\" s on " +
                "(s.\"id\" = schedule.\"status\" and s.\"id_type\" = schedule.\"status_type\") " +
                "where s.\"name\"=:status" + ACCESS_RIGHTS_PART + " order by schedule.\"priority\"";
        Assert.assertEquals(expectedQuery2, queryHelper.generateFindTasksByStatusQuery("schedule", accessToken, false));
    }

    @Test
    public void testGenerateFindNotInStatusTasksQuery() throws Exception {
        AccessToken accessToken = createMockAccessToken();
        String expectedQuery = "select schedule.* from \"schedule\" schedule  inner join \"status\" s on " +
                "(s.\"id\" = schedule.\"status\" and s.\"id_type\" = schedule.\"status_type\") " +
                "where s.\"name\"!=:status" + ACCESS_RIGHTS_PART;
        Assert.assertEquals(expectedQuery, queryHelper.generateFindNotInStatusTasksQuery("schedule", accessToken));
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

        DomainObjectTypeConfig schedule = new DomainObjectTypeConfig();
        schedule.setName("schedule");

        StringFieldConfig scheduleName = new StringFieldConfig();
        scheduleName.setName("name");
        schedule.getFieldConfigs().add(scheduleName);

        StringFieldConfig taskClass = new StringFieldConfig();
        taskClass.setName("task_class");
        schedule.getFieldConfigs().add(taskClass);


        Configuration configuration = new Configuration();
        configuration.getConfigurationList().add(domainObjectTypeConfig);
        configuration.getConfigurationList().add(internalEmployee);
        configuration.getConfigurationList().add(externalEmployee);
        configuration.getConfigurationList().add(assignment);
        configuration.getConfigurationList().add(globalSettings);
        configuration.getConfigurationList().add(schedule);

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
