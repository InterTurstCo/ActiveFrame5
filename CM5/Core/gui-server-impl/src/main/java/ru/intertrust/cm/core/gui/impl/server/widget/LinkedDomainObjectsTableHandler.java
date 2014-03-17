package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.form.FormSaver;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormState;
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
    public void saveNewObjects(WidgetContext context, WidgetState state) {
        LinkedDomainObjectsTableState linkedDomainObjectsTableState = (LinkedDomainObjectsTableState) state;
        DomainObject rootDomainObject = context.getFormObjects().getRootNode().getDomainObject();
        LinkedHashMap<String, FormState> newFormStates = linkedDomainObjectsTableState.getNewFormStates();
        Set<Map.Entry<String, FormState>> entries = newFormStates.entrySet();
        LinkedDomainObjectsTableConfig linkedDomainObjectsTableConfig = linkedDomainObjectsTableState.getLinkedDomainObjectsTableConfig();
        FieldPath fieldPath = new FieldPath(linkedDomainObjectsTableConfig.getFieldPathConfig().getValue());

        for (Map.Entry<String, FormState> entry : entries) {
            FormState formState = entry.getValue();
            FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver", formState);
            DomainObject savedObject = formSaver.saveForm();
            if (fieldPath.isOneToManyReference()) {
                savedObject.setReference(fieldPath.getLinkToParentName(), rootDomainObject);
                crudService.save(savedObject);
            } else if (fieldPath.isManyToManyReference()) {
                String referenceType = fieldPath.getReferenceType();
                FieldConfig fieldConfig = configurationExplorer.getFieldConfig(referenceType, fieldPath.getReferenceName());
                DomainObject referencedObject = crudService.createDomainObject(referenceType);
                if (fieldConfig != null) {
                    referencedObject.setReference(fieldConfig.getName(), savedObject);
                }
                fieldConfig = configurationExplorer.getFieldConfig(referenceType, rootDomainObject.getTypeName());
                if (fieldConfig != null) {
                    referencedObject.setReference(fieldConfig.getName(), rootDomainObject);
                }
                crudService.save(referencedObject);
            }
        }
    }
}
