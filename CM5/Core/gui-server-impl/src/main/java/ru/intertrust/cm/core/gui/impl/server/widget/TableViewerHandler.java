package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.gui.action.SimpleActionConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectsTableConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.tableviewer.TableViewerConfig;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.SelfManagingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.impl.server.widget.util.WidgetConfigUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.TableViewerData;
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

    @Autowired
    ActionService actionService;

    @Override
    public TableViewerState getInitialState(WidgetContext context) {
        TableViewerConfig widgetConfig = (TableViewerConfig) context.getWidgetConfig();
        TableViewerState initialState = new TableViewerState(widgetConfig);
        initialState.setRootObject(context.getFormObjects().getRootNode().getDomainObject());
        if (widgetConfig.getLinkedFormMappingConfig() != null &&
                widgetConfig.getCreatedObjectsConfig() != null) {
            initialState.setParentWidgetIdsForNewFormMap(createParentWidgetIdsForNewFormMap(widgetConfig,
                    context.getWidgetConfigsById().values()));
            DomainObject root = context.getFormObjects().getRootNode().getDomainObject();
            CreatedObjectsConfig restrictedCreatedObjectsConfig = createRestrictedCreateObjectsConfig(root, widgetConfig);
            initialState.setRestrictedCreatedObjectsConfig(restrictedCreatedObjectsConfig);
        }
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
                if (accessVerificationService.isCreatePermitted(domainObjectType)) {
                    CreatedObjectConfig createdObjectConfig = new CreatedObjectConfig();
                    createdObjectConfig.setDomainObjectType(domainObjectType);
                    createdObjectConfig.setText(domainObjectType);
                    createdObjectConfigs.add(createdObjectConfig);
                }
            }
        }
        return restrictedCreatedObjectsConfig;
    }

    public Dto getActionsById(Dto request) {
        Id id = (Id) request;
        TableViewerData data = new TableViewerData();
        data.setAvailableActions(actionService.getActions(id));
        return data;
    }

    public Dto getActionsByIds(Dto request) {
        TableViewerData data = (TableViewerData) request;
/**
 *  #1 Выбираем все доступные действия формируя структуру Имя действия - список Id и контекстов.
 */
        for (final Id id : data.getSelectedIds()) {
            for (final ActionContext aContext : actionService.getActions(id)) {
                SimpleActionContext simpleActionContext = (SimpleActionContext) aContext;
                if (data.getIdsActions().size() == 0 ||
                        !data.getIdsActions().containsKey(((SimpleActionConfig) simpleActionContext.getActionConfig()).getText())
                        ) {
                    data.getIdsActions().put(((SimpleActionConfig) simpleActionContext.getActionConfig()).getText(), new HashMap() {{
                        put(id, aContext);
                    }});
                } else {
                    data.getIdsActions().get(((SimpleActionConfig) simpleActionContext.getActionConfig()).getText()).put(id,aContext);
                }
            }
        }
/**
 *  #2 Оставляем только те связки у которых одинаковые наборы Id в парах Id - контекст. Т.е. в итоге, действия могут
 *  быть разные (если они одинаковы по имени но не по логике) но для одних и техже айтемов будет одинаковый список меню.
 */

        return data;
    }
}
