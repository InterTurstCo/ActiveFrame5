package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.gui.api.server.form.DomainObjectLinkContext;
import ru.intertrust.cm.core.gui.api.server.form.DomainObjectLinkInterceptor;

import java.util.ArrayList;
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
    public boolean beforeLink(DomainObjectLinkContext context) {
        final FieldPathConfig fieldPathConfig = context.getWidgetContext().getFieldPathConfig();
        final OnLinkConfig onLinkConfig = fieldPathConfig.getOnLinkConfig();
        if (onLinkConfig == null) {
            return true;
        }
        final boolean doLink = onLinkConfig.doLink();

        List<OperationConfig> operationConfigs = null;
        if (onLinkConfig.getCreateConfig() != null || onLinkConfig.getUpdateConfig() != null) {
            operationConfigs = new ArrayList<>();
            if (onLinkConfig.getCreateConfig() != null) {
                operationConfigs.add(onLinkConfig.getCreateConfig());
            }
            if (onLinkConfig.getUpdateConfig() != null) {
                operationConfigs.add(onLinkConfig.getUpdateConfig());
            }
        }

        if (operationConfigs == null) {
            return doLink;
        }

        performCreateUpdateOperations(context, operationConfigs);
        return doLink;
    }

    @Override
    public boolean beforeUnlink(DomainObjectLinkContext context) {
        final FieldPathConfig fieldPathConfig = context.getWidgetContext().getFieldPathConfig();
        final OnUnlinkConfig onUnlinkConfig = fieldPathConfig.getOnUnlinkConfig();
        if (onUnlinkConfig == null) {
            return true;
        }
        final boolean doUnlink = onUnlinkConfig.doUnlink();
        final List<OperationConfig> operationConfigs = onUnlinkConfig.getOperationConfigs();
        if (operationConfigs == null) {
            return doUnlink;
        }

        performCreateUpdateOperations(context, operationConfigs);
        return doUnlink;
    }

    private void performCreateUpdateOperations(DomainObjectLinkContext context, List<OperationConfig> operationConfigs) {
        boolean saveLinkedObject = false;
        for (OperationConfig operationConfig : operationConfigs) {
            if (operationConfig instanceof UpdateConfig) {
                updateObject((UpdateConfig) operationConfig, context);
                saveLinkedObject = true;
            } else if (operationConfig instanceof CreateConfig) {
                createObject((CreateConfig) operationConfig, context);
            }
        }

        if (saveLinkedObject) {
            crudService.save(context.getLinkedObject());
        }
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
    }

    private void setFields(DomainObject domainObject, List<FieldValueConfig> fieldValueConfigs, DomainObject parentObject) {
        ((DomainObjectFieldsSetter) applicationContext.getBean("domainObjectFieldsSetter", domainObject, fieldValueConfigs, parentObject)).setFields();
    }
}
