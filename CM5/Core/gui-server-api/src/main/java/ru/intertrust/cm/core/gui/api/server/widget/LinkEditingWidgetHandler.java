package ru.intertrust.cm.core.gui.api.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.access.AccessVerificationService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FillParentOnAddConfig;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.SortOrderHelper;
import ru.intertrust.cm.core.gui.impl.server.widget.util.WidgetConfigUtil;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.LinkCreatorWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.LinkEditingWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 14.10.13
 *         Time: 14:58
 */
public abstract class LinkEditingWidgetHandler extends WidgetHandler {

    @Autowired
    protected WidgetItemsHandler widgetItemsHandler;

    @Autowired
    protected TitleBuilder titleBuilder;

    @Autowired
    protected AccessVerificationService accessVerificationService;

    @Autowired
    protected SortOrderHelper sortOrderHelper;

    @Override
    public Value getValue(WidgetState state) {
        ArrayList<Id> ids = ((LinkEditingWidgetState) state).getIds();
        return ids == null || ids.isEmpty() ? null : new ReferenceValue(ids.get(0));

    }

    protected String getLinkedObjectType(WidgetContext context, FieldPath fieldPath) {
        if (fieldPath.isField()) {
            // such situation happens when link-editing widget is assigned to a field which is actually a reference
            String parentType = context.getFormObjects().getParentNode(fieldPath).getType();
            return ((ReferenceFieldConfig) configurationService.getFieldConfig(parentType, fieldPath.getFieldName())).getType();
        }

        if (fieldPath.isOneToManyReference()) {
            return fieldPath.getReferenceType();
        } else { // many-to-many
            return ((ReferenceFieldConfig) configurationService.getFieldConfig(
                    fieldPath.getReferenceType(), fieldPath.getLinkToChildrenName())).getType();
        }
    }

    protected String[] getLinkedObjectTypes(WidgetContext context, FieldPath[] fieldPaths) {
        String[] result = new String[fieldPaths.length];
        for (int i = 0; i < fieldPaths.length; ++i) {
            result[i] = getLinkedObjectType(context, fieldPaths[i]);
        }
        return result;
    }

    /**
     * Метод возвращает true, если при разрыве связи со связанными объектами, сами связанные объекты удаляются. Если false,
     * то разрывается связь, а бывшие связанные объекты остаются в системе.
     *
     * @param config
     * @return true, если при разрыве связи со связанными объекты, сами связанные объекты удаляются, false - в противном случае
     */
    public boolean deleteEntriesOnLinkDrop(WidgetConfig config) {
        return false;
    }

    protected void fillTypeTitleMap(DomainObject root, LinkedFormMappingConfig mappingConfig, LinkCreatorWidgetState state) {
        Map<String, PopupTitlesHolder> typeTitleMap = titleBuilder.buildTypeTitleMap(mappingConfig, root);
        state.setTypeTitleMap(typeTitleMap);
    }

    protected boolean abandonAccessed(DomainObject root, CreatedObjectsConfig createdObjectsConfig,
                                      FillParentOnAddConfig fillParentOnAddConfig) {
        if (createdObjectsConfig != null) {
            List<CreatedObjectConfig> createdObjectConfigs = createdObjectsConfig.getCreateObjectConfigs();
            if (WidgetUtil.isNotEmpty(createdObjectConfigs)) {
                Iterator<CreatedObjectConfig> iterator = createdObjectConfigs.iterator();
                while (iterator.hasNext()) {
                    CreatedObjectConfig createdObjectConfig = iterator.next();
                    String domainObjectType = createdObjectConfig.getDomainObjectType();
                    Id rootId = root == null ? null : root.getId();
                    boolean displayingCreateButton = fillParentOnAddConfig == null
                            ? accessVerificationService.isCreatePermitted(domainObjectType)
                            : (rootId == null || accessVerificationService.isCreateChildPermitted(domainObjectType, root.getId()));
                    if (!displayingCreateButton) {
                        iterator.remove();
                    }
                }
                if (!createdObjectConfigs.isEmpty()) {
                    return true;
                }
            }
        }
        return false;

    }

    protected Map<String, Collection<String>> createParentWidgetIdsForNewFormMap(HasLinkedFormMappings config,
                                                                                 Collection<WidgetConfig> parentWidgetConfigs) {
        Map<String, Collection<String>> result = new HashMap<>();
        if (config.getLinkedFormMappingConfig() != null) {
            List<LinkedFormConfig> linkedFormConfigs = config.getLinkedFormMappingConfig().getLinkedFormConfigs();
            for (LinkedFormConfig linkedFormConfig : linkedFormConfigs) {
                String domainObjectType = Case.toLower(linkedFormConfig.getDomainObjectType());
                result.put(domainObjectType, getRequiredWidgetIdsFromForm(linkedFormConfig.getName(), parentWidgetConfigs));

            }
        } else if (config.getLinkedFormConfig() != null && config.getLinkedFormConfig().getDomainObjectType() != null) {
            String domainObjectType = Case.toLower(config.getLinkedFormConfig().getDomainObjectType());
            result.put(domainObjectType, getRequiredWidgetIdsFromForm(config.getLinkedFormConfig().getName(),
                    parentWidgetConfigs));

        }
        return result;
    }

    protected Collection<String> getRequiredWidgetIdsFromForm(String formName, Collection<WidgetConfig> parentWidgetConfigs) {
        FormConfig formConfig = configurationService.getConfig(FormConfig.class, formName);
        return WidgetConfigUtil.getRequiredWidgetIdsFromForm(formConfig, parentWidgetConfigs);
    }

}
