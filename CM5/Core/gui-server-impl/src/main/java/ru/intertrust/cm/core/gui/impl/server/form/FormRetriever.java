package ru.intertrust.cm.core.gui.impl.server.form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.gui.form.FormConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.*;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 21.10.13
 *         Time: 13:03
 */
public class FormRetriever {
    private static Logger log = LoggerFactory.getLogger(FormRetriever.class);

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private CrudService crudService;
    @Autowired
    private FormResolver formResolver;

    private String userUid;

    public FormRetriever(String userUid) {
        this.userUid = userUid;
    }

    public FormDisplayData getForm(String domainObjectType) {
        DomainObject root = crudService.createDomainObject(domainObjectType);
        // todo: separate empty form?
        return buildDomainObjectForm(root);
    }

    public FormDisplayData getForm(Id domainObjectId) {
        DomainObject root = crudService.find(domainObjectId);
        if (root == null) {
            throw new GuiException("Object with id: " + domainObjectId.toStringRepresentation() + " doesn't exist");
        }
        return buildDomainObjectForm(root);
    }

    private FormDisplayData buildDomainObjectForm(DomainObject root) {
        // todo validate that constructions like A^B.C.D aren't allowed or A.B^C
        // allowed are such definitions only:
        // a.b.c.d - direct links
        // a^b - link defining 1:N relationship (widgets changing attributes can't have such field path)
        // a^b.c - link defining N:M relationship (widgets changing attributes can't have such field path)
        FormConfig formConfig = formResolver.findFormConfig(root, userUid);
        List<WidgetConfig> widgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();
        HashMap<String, WidgetState> widgetStateMap = new HashMap<>(widgetConfigs.size());
        HashMap<String, String> widgetComponents = new HashMap<>(widgetConfigs.size());
        FormObjects formObjects = new FormObjects();

        ObjectsNode rootNode = new SingleObjectNode(root);
        formObjects.setRootNode(rootNode);
        for (WidgetConfig config : widgetConfigs) {
            String widgetId = config.getId();
            FieldPathConfig fieldPathConfig = config.getFieldPathConfig();
            if (fieldPathConfig == null || fieldPathConfig.getValue() == null) {
                if (!(config instanceof LabelConfig)) {
                    throw new GuiException("Widget, id: " + widgetId + " is not configured with Field Path");
                }

                //todo refactor
                WidgetContext widgetContext = new WidgetContext(config, formObjects);
                WidgetHandler componentHandler = (WidgetHandler) applicationContext.getBean(config.getComponentName());
                WidgetState initialState = componentHandler.getInitialState(widgetContext);
                widgetStateMap.put(widgetId, initialState);
                widgetComponents.put(widgetId, config.getComponentName());
                continue;
            }
            FieldPath fieldPath = new FieldPath(fieldPathConfig.getValue());

            rootNode = formObjects.getRootNode();
            for (Iterator<FieldPath> childrenIterator = fieldPath.childrenIterator(); childrenIterator.hasNext(); ) {
                FieldPath childPath = childrenIterator.next();
                if (childPath.isField()) {
                    break;
                }

                if (formObjects.containsNode(childPath)) {
                    rootNode = formObjects.getNode(childPath);
                    continue;
                }

                // it's a reference. linked objects can exist only for Single-Object Nodes. class-cast exception will
                // raise if that's not true
                ObjectsNode linkedNode = findLinkedNode((SingleObjectNode) rootNode, childPath);

                formObjects.setNode(childPath, linkedNode);
                rootNode = linkedNode;
            }

            WidgetContext widgetContext = new WidgetContext(config, formObjects);
            WidgetHandler componentHandler = (WidgetHandler) applicationContext.getBean(config.getComponentName());
            WidgetState initialState = componentHandler.getInitialState(widgetContext);
            initialState.setEditable(true);
            widgetStateMap.put(widgetId, initialState);
            widgetComponents.put(widgetId, config.getComponentName());
        }
        FormState formState = new FormState(formConfig.getName(), widgetStateMap, formObjects);
        return new FormDisplayData(formState, formConfig.getMarkup(), widgetComponents, formConfig.getDebug(), true);
    }

    private ObjectsNode findLinkedNode(SingleObjectNode parentNode, FieldPath childPath) {
        if (childPath.isOneToOneReference()) { // direct reference
            return findOneToOneLinkedNode(parentNode, childPath);
        } else { // back reference
            return findBackReferenceLinkedNode(parentNode, childPath);
        }
    }

    private SingleObjectNode findOneToOneLinkedNode(SingleObjectNode parentNode, FieldPath childPath) {
        String referenceFieldName = childPath.getOneToOneReferenceName();
        ReferenceFieldConfig fieldConfig = (ReferenceFieldConfig)
                configurationExplorer.getFieldConfig(parentNode.getType(), referenceFieldName);
        String linkedType = fieldConfig.getType();
        if (parentNode.isEmpty()) {
            return new SingleObjectNode(linkedType);
        }

        Id linkedObjectId = parentNode.getDomainObject().getReference(referenceFieldName);
        if (linkedObjectId == null) {
            return new SingleObjectNode(linkedType);
        } else {
            return new SingleObjectNode(crudService.find(linkedObjectId));
        }
    }

    private MultiObjectNode findBackReferenceLinkedNode(SingleObjectNode parentNode, FieldPath childPath) {
        String linkedType = childPath.getReferenceType();
        if (parentNode.isEmpty()) {
            return new MultiObjectNode(linkedType);
        }

        String referenceField = childPath.getLinkToParentName();

        // todo after cardinality functionality is developed, check cardinality (static-check, not runtime)

        DomainObject parentDomainObject = parentNode.getDomainObject();
        List<DomainObject> linkedDomainObjects = parentDomainObject.getId() == null
                ? new ArrayList<DomainObject>()
                : crudService.findLinkedDomainObjects(parentDomainObject.getId(), linkedType, referenceField);
        return new MultiObjectNode(linkedType, linkedDomainObjects);
    }
}
