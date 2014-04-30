package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.form.FormSaver;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.SingleObjectNode;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectsTableState;
import ru.intertrust.cm.core.gui.model.form.widget.RowItem;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.*;

@ComponentName("linked-domain-objects-table")
public class LinkedDomainObjectsTableHandler extends LinkEditingWidgetHandler {

    @Autowired
    CrudService crudService;
    @Autowired
    ConfigurationExplorer configurationExplorer;
    @Autowired
    ApplicationContext applicationContext;

    @Override
    public LinkedDomainObjectsTableState getInitialState(WidgetContext context) {

        LinkedDomainObjectsTableState state = new LinkedDomainObjectsTableState();
        LinkedDomainObjectsTableConfig domainObjectsTableConfig = context.getWidgetConfig();
        state.setLinkedDomainObjectTableConfig(domainObjectsTableConfig);
        SingleChoiceConfig singleChoiceConfig = domainObjectsTableConfig.getSingleChoiceConfig();
        boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        state.setSingleChoice(singleChoiceFromConfig);

        List<DomainObject> domainObjects = new ArrayList<>();
        if (context.getAllObjectIds() != null && !context.getAllObjectIds().isEmpty()) {
            domainObjects = crudService.find(context.getAllObjectIds());
        }

        String linkedFormName = domainObjectsTableConfig.getLinkedFormConfig().getName();
        if (linkedFormName != null && !linkedFormName.isEmpty()) {
            FormConfig formConfig = configurationExplorer.getConfig(FormConfig.class, linkedFormName);
            state.setObjectTypeName(formConfig.getDomainObjectType());
        }

        List<RowItem> rowItems = new ArrayList<>();
        List<SummaryTableColumnConfig> summaryTableColumnConfigs = domainObjectsTableConfig
                .getSummaryTableConfig().getSummaryTableColumnConfig();

        for (DomainObject domainObject : domainObjects) {
            RowItem rowItem;
            rowItem = new RowItemMapper(domainObject, summaryTableColumnConfigs).map();
            rowItem.setObjectId(domainObject.getId());
            rowItems.add(rowItem);
        }
        state.setRowItems(rowItems);
        return state;
    }

    @Override
    public List<DomainObject> saveNewObjects(WidgetContext context, WidgetState state) {
        LinkedDomainObjectsTableState linkedDomainObjectsTableState = (LinkedDomainObjectsTableState) state;
        final FormObjects formObjects = context.getFormObjects();
        DomainObject rootDomainObject = formObjects.getRootNode().getDomainObject();
        LinkedHashMap<String, FormState> newFormStates = linkedDomainObjectsTableState.getNewFormStates();
        Set<Map.Entry<String, FormState>> entries = newFormStates.entrySet();

        LinkedDomainObjectsTableConfig linkedDomainObjectsTableConfig = linkedDomainObjectsTableState.getLinkedDomainObjectsTableConfig();
        FieldPath fieldPath = new FieldPath(linkedDomainObjectsTableConfig.getFieldPathConfig().getValue());
        ArrayList<DomainObject> newObjects = new ArrayList<>(entries.size());
        for (Map.Entry<String, FormState> entry : entries) {
            FormState formState = entry.getValue();
            DomainObject savedObject;

            if (fieldPath.isOneToManyReference()) {
                final HashMap<FieldPath, Value> rootObjectValues = new HashMap<>();
                rootObjectValues.put(new FieldPath(fieldPath.getLinkToParentName()), new ReferenceValue(rootDomainObject.getId()));
                FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver", formState, rootObjectValues);
                savedObject = formSaver.saveForm();
            } else if (fieldPath.isManyToManyReference()) {
                FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver", formState, null);
                savedObject = formSaver.saveForm();
                String referenceType = fieldPath.getReferenceType();
                DomainObject referencedObject = crudService.createDomainObject(referenceType);
                referencedObject.setReference(fieldPath.getLinkToChildrenName(), savedObject);
                referencedObject.setReference(fieldPath.getLinkToParentName(), rootDomainObject);
                crudService.save(referencedObject);
            } else { // one-to-one reference
                // todo: not-null constraint will fail!
                FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver", formState, null);
                savedObject = formSaver.saveForm();
                rootDomainObject.setReference(fieldPath.getFieldName(), savedObject);
                crudService.save(rootDomainObject);
            }

            newObjects.add(savedObject);
        }
        return newObjects;
    }

    @Override
    public boolean deleteEntriesOnLinkDrop(WidgetConfig config) {
        return ((LinkedDomainObjectsTableConfig) config).isDeleteLinkedObjects();
    }
}
