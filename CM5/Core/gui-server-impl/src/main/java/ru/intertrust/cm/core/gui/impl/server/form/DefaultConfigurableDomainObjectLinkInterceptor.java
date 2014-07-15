package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.gui.api.server.form.BeforeLinkResult;
import ru.intertrust.cm.core.gui.api.server.form.BeforeUnlinkResult;
import ru.intertrust.cm.core.gui.api.server.form.DomainObjectLinkContext;
import ru.intertrust.cm.core.gui.api.server.form.DomainObjectLinkInterceptor;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 19.06.2014
 *         Time: 15:54
 */
public class DefaultConfigurableDomainObjectLinkInterceptor implements DomainObjectLinkInterceptor {
    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    private CrudService crudService;

    @Override
    public BeforeLinkResult beforeLink(DomainObjectLinkContext context) {
        final FieldPathConfig fieldPathConfig = context.getWidgetContext().getFieldPathConfig();
        final OnLinkConfig onLinkConfig = fieldPathConfig.getOnLinkConfig();
        if (onLinkConfig == null) {
            return new BeforeLinkResult(true, context.getLinkedObject());
        }
        final boolean doLink = onLinkConfig.doLink();

        final List<OperationConfig> operationConfigs = onLinkConfig.getOperationConfigs();
        if (operationConfigs == null) {
            return new BeforeLinkResult(doLink, context.getLinkedObject());
        }

        final DomainObject updatedLinkedObject = performCreateUpdateOperations(context, operationConfigs);
        return new BeforeLinkResult(doLink, updatedLinkedObject);
    }

    @Override
    public BeforeUnlinkResult beforeUnlink(DomainObjectLinkContext context) {
        final FieldPathConfig fieldPathConfig = context.getWidgetContext().getFieldPathConfig();
        final OnUnlinkConfig onUnlinkConfig = fieldPathConfig.getOnUnlinkConfig();
        if (onUnlinkConfig == null) {
            return new BeforeUnlinkResult(true, context.getLinkedObject());
        }
        final boolean doUnlink = onUnlinkConfig.doUnlink();
        final List<OperationConfig> operationConfigs = onUnlinkConfig.getOperationConfigs();
        if (operationConfigs == null) {
            return new BeforeUnlinkResult(doUnlink, context.getLinkedObject());
        }

        final DomainObject updatedUnlinkedObject = performCreateUpdateOperations(context, operationConfigs);
        return new BeforeUnlinkResult(doUnlink, updatedUnlinkedObject);
    }

    private DomainObject performCreateUpdateOperations(DomainObjectLinkContext context, List<OperationConfig> operationConfigs) {
        boolean saveLinkedObject = false;
        for (OperationConfig operationConfig : operationConfigs) {
            if (operationConfig instanceof UpdateConfig) {
                updateObject((UpdateConfig) operationConfig, context);
                saveLinkedObject = true;
            } else if (operationConfig instanceof CreateConfig) {
                createObject((CreateConfig) operationConfig, context);
            }
        }

        final DomainObject linkedObject = context.getLinkedObject();
        DomainObject updatedLinkedObject = linkedObject;
        if (saveLinkedObject) {
            updatedLinkedObject = crudService.save(linkedObject);
        }
        return updatedLinkedObject;
    }

    private void updateObject(UpdateConfig updateConfig, DomainObjectLinkContext context) {
        final List<FieldValueConfig> fieldValueConfigs = updateConfig.getFieldValueConfigs();
        if (fieldValueConfigs == null || fieldValueConfigs.isEmpty()) {
            return;
        }
        setFields(context.getLinkedObject(), fieldValueConfigs, context.getParentObject());
    }

    private void createObject(CreateConfig createConfig, DomainObjectLinkContext context) {
        DomainObject createdObject = crudService.createDomainObject(createConfig.getType());
        final List<FieldValueConfig> fieldValueConfigs = createConfig.getFieldValueConfigs();
        if (fieldValueConfigs == null || fieldValueConfigs.isEmpty()) {
            return;
        }
        setFields(createdObject, fieldValueConfigs, context.getParentObject());

        crudService.save(createdObject);
    }

    private void setFields(DomainObject domainObject, List<FieldValueConfig> fieldValueConfigs, DomainObject parentObject) {
        ((DomainObjectFieldsSetter) applicationContext.getBean("domainObjectFieldsSetter", domainObject, fieldValueConfigs, parentObject)).setFields();
    }
}
