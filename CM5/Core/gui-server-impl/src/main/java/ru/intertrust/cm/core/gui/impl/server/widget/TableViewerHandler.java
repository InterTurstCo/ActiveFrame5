package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectsTableConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.tableviewer.TableViewerConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.SelfManagingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.impl.server.widget.util.WidgetConfigUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TableViewerState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 13.12.2014
 *         Time: 20:29
 */
@ComponentName("table-viewer")
public class TableViewerHandler extends LinkEditingWidgetHandler implements SelfManagingWidgetHandler {
    @Override
    public TableViewerState getInitialState(WidgetContext context) {
        TableViewerConfig widgetConfig = (TableViewerConfig) context.getWidgetConfig();
        TableViewerState initialState = new TableViewerState(widgetConfig);
        initialState.setParentWidgetIdsForNewFormMap(createParentWidgetIdsForNewFormMap(widgetConfig,
                context.getWidgetConfigsById().values()));
        DomainObject root = context.getFormObjects().getRootNode().getDomainObject();
        CreatedObjectsConfig restrictedCreatedObjectsConfig = createRestrictedCreateObjectsConfig(root, widgetConfig);
        initialState.setRestrictedCreatedObjectsConfig(restrictedCreatedObjectsConfig);
        return initialState;
    }

    @Override
    public Value getValue(WidgetState state) {
        return null;
    }

    protected Map<String, Collection<String>> createParentWidgetIdsForNewFormMap(HasLinkedFormMappings config,
                                                                                 Collection<WidgetConfig> parentWidgetConfigs) {
        Map<String, Collection<String>> result = new HashMap<>();
        if (config.getLinkedFormMappingConfig() != null) {
            List<LinkedFormConfig> linkedFormConfigs = config.getLinkedFormMappingConfig().getLinkedFormConfigs();
            for (LinkedFormConfig linkedFormConfig : linkedFormConfigs) {
                String domainObjectType = linkedFormConfig.getDomainObjectType().toLowerCase();
                result.put(domainObjectType, getRequiredWidgetIdsFromForm(linkedFormConfig.getName(), parentWidgetConfigs));

            }
        } else if (config.getLinkedFormConfig() != null && config.getLinkedFormConfig().getDomainObjectType() != null) {
            String domainObjectType = config.getLinkedFormConfig().getDomainObjectType().toLowerCase();
            result.put(domainObjectType, getRequiredWidgetIdsFromForm(config.getLinkedFormConfig().getName(),
                    parentWidgetConfigs));

        }
        return result;
    }

    private Collection<String> getRequiredWidgetIdsFromForm(String formName, Collection<WidgetConfig> parentWidgetConfigs) {
        FormConfig formConfig = configurationService.getConfig(FormConfig.class, formName);
        return WidgetConfigUtil.getRequiredWidgetIdsFromForm(formConfig, parentWidgetConfigs);
    }

    private CreatedObjectsConfig createRestrictedCreateObjectsConfig(DomainObject root, TableViewerConfig widgetConfig) {
        CreatedObjectsConfig restrictedCreatedObjectsConfig = null;
        if (widgetConfig.getCreatedObjectsConfig() != null) {
            restrictedCreatedObjectsConfig = ObjectCloner.getInstance().cloneObject(widgetConfig.getCreatedObjectsConfig(),
                    CreatedObjectsConfig.class);
            abandonAccessed(root, restrictedCreatedObjectsConfig, null);
        } else {
            restrictedCreatedObjectsConfig = new CreatedObjectsConfig();
            List<CreatedObjectConfig> createdObjectConfigs = new ArrayList<CreatedObjectConfig>(1);
            restrictedCreatedObjectsConfig.setCreateObjectConfigs(createdObjectConfigs);
            String linkedFormName = widgetConfig.getLinkedFormConfig().getName();
            if (linkedFormName != null && !linkedFormName.isEmpty()) {
                FormConfig defaultFormConfig = configurationService.getConfig(FormConfig.class, linkedFormName);
                String domainObjectType = defaultFormConfig.getDomainObjectType();
                if(accessVerificationService.isCreatePermitted(domainObjectType)){
                    CreatedObjectConfig createdObjectConfig = new CreatedObjectConfig();
                    createdObjectConfig.setDomainObjectType(domainObjectType);
                    createdObjectConfig.setText(domainObjectType);
                    createdObjectConfigs.add(createdObjectConfig);
                }
            }
        }
        return restrictedCreatedObjectsConfig;
    }
}
