package ru.intertrust.cm.core.business.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.AttachmentTypesConfig;
import ru.intertrust.cm.core.config.AuditLog;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.model.CrudException;

/**
 * Юнит тест для CrudServiceImpl на проверку сохранения Attachments
 * как правило, ожидается CrudException
 */

@RunWith(MockitoJUnitRunner.class)
public class CrudServiceImplTest {

    public final static String EXCEPTION_TEXT = "Working with Attachments allowed only through AttachmentService";

    private CrudServiceBaseImpl crudService = new CrudServiceBaseImpl();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Before
    public void setUp() throws Exception {
        ConfigurationClassesCache.getInstance().build();
        Configuration configuration = createConfiguration();
        ConfigurationExplorerImpl configExplorer = new ConfigurationExplorerImpl(configuration);
        configExplorer.init();

        crudService.setConfigurationExplorer(configExplorer);

        DomainObjectTypeIdCache domainObjectTypeIdCache = mock(DomainObjectTypeIdCache.class);
        when(domainObjectTypeIdCache.getName(any(Id.class))).thenReturn("SomeDoc");
        crudService.setDomainObjectTypeIdCache(domainObjectTypeIdCache);
    }

    @Test
    public void testCreateMissingType() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Domain Object Type '_some_missing_type_' doesn't exist");
        crudService.createDomainObject("_some_missing_type_");
    }


    @Test
    public void testSaveAttachment() throws Exception {
        GenericDomainObject attachDomainObject = new GenericDomainObject();
        attachDomainObject.setTypeName("SomeDoc");
        expectedException.expect(CrudException.class);
        expectedException.expectMessage(EXCEPTION_TEXT);
        crudService.save(attachDomainObject);
    }

    @Test
    public void testSaveManyAttachment() throws Exception {
        List<DomainObject> domainObjectList = new ArrayList<>();

        GenericDomainObject employeeDomainObject = new GenericDomainObject();
        employeeDomainObject.setTypeName("Employee");
        domainObjectList.add(employeeDomainObject);

        GenericDomainObject attachDomainObject = new GenericDomainObject();
        attachDomainObject.setTypeName("SomeDoc");
        domainObjectList.add(attachDomainObject);

        expectedException.expect(CrudException.class);
        expectedException.expectMessage(EXCEPTION_TEXT);
        crudService.save(domainObjectList);
    }

    @Test
    public void testDeleteAttachment() throws Exception {
        expectedException.expect(CrudException.class);
        expectedException.expectMessage(EXCEPTION_TEXT);
        crudService.delete(new RdbmsId());
    }

    @Test
    public void testDeleteManyAttachment() throws Exception {
        List<Id> ids = new ArrayList<>();
        assertEquals(0, crudService.delete(ids));

        ids.add(new RdbmsId());
        ids.add(new RdbmsId());

        expectedException.expect(CrudException.class);
        expectedException.expectMessage(EXCEPTION_TEXT);
        crudService.delete(ids);
    }


    private DomainObjectTypeConfig createEmployee() {
        DomainObjectTypeConfig result = new DomainObjectTypeConfig();
        result.setName("Employee");
        AttachmentTypesConfig attachmentTypesConfig = new AttachmentTypesConfig();
        ArrayList<AttachmentTypeConfig> attachmentTypeConfigs = new ArrayList<>();
        AttachmentTypeConfig attachmentTypeConfig = new AttachmentTypeConfig();
        attachmentTypeConfig.setName("SomeDoc");
        attachmentTypeConfigs.add(attachmentTypeConfig);
        attachmentTypesConfig.setAttachmentTypeConfigs(attachmentTypeConfigs);
        result.setAttachmentTypesConfig(attachmentTypesConfig);

        return result;
    }


    private Configuration createConfiguration() {
        Configuration configuration = new Configuration();
        GlobalSettingsConfig globalSettings = new GlobalSettingsConfig();
        AuditLog auditLog = new AuditLog();
        globalSettings.setAuditLog(auditLog);

        DomainObjectTypeConfig attachment = new DomainObjectTypeConfig();
        attachment.setTemplate(true);
        attachment.setName(GenericDomainObject.ATTACHMENT_TEMPLATE);
        configuration.getConfigurationList().add(attachment);
        configuration.getConfigurationList().add(globalSettings);
        configuration.getConfigurationList().add(createEmployee());
        return configuration;
    }


}
