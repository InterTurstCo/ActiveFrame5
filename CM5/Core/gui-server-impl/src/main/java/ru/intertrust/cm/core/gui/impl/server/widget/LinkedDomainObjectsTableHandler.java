package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
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
            state.setLinkedFormConfig(formConfig);
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
        String parentLinkFieldName = fieldPath.getLinkToParentName();

        for (Map.Entry<String, FormState> entry : entries) {
            FormState formState = entry.getValue();
            FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver", formState);
            DomainObject savedObject = formSaver.saveForm();
            savedObject.setReference(parentLinkFieldName, rootDomainObject);
            DomainObject savedDomainObject = crudService.save(savedObject);
            ((LinkedDomainObjectsTableState) state).getIds().add(savedDomainObject.getId());
        }

    }
}
