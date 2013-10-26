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
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.*;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;
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

        ObjectsNode rootNode = new ObjectsNode(root);
        formObjects.setRootObjects(rootNode);
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

            rootNode = formObjects.getRootObjects();
            for (Iterator<FieldPath> subPathIterator = fieldPath.childPathIterator(); subPathIterator.hasNext(); ) {
                FieldPath subPath = subPathIterator.next();
                String linkPath = subPath.getLastElement().getName();
                if (!subPathIterator.hasNext() && !linkPath.contains("^")) { // it's a field
                    break;
                }

                // it's a reference then
                String childNodeType;
                if (!linkPath.contains("^")) { // direct reference
                    ReferenceFieldConfig fieldConfig = (ReferenceFieldConfig)
                            configurationExplorer.getFieldConfig(rootNode.getType(), linkPath);
                    childNodeType = fieldConfig.getType();
                } else { // back reference
                    childNodeType = linkPath.split("\\^")[0];
                }

                if (formObjects.isObjectsSet(subPath)) {
                    rootNode = formObjects.getObjects(subPath);
                    continue;
                }
                ObjectsNode linkedNode = findLinkedNode(rootNode, childNodeType, linkPath);

                formObjects.setObjects(subPath, linkedNode);
                rootNode.setChild(linkPath, linkedNode);
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

    private ObjectsNode findLinkedNode(ObjectsNode parentNode, String linkedType, String linkPath) {
        if (parentNode.isEmpty()) {
            return new ObjectsNode(linkedType, 0);
        }

        if (!linkPath.contains("^")) { // direct link
            ObjectsNode result = new ObjectsNode(linkedType, parentNode.size());
            for (DomainObject domainObject : parentNode) {
                Id linkedObjectId = domainObject.getReference(linkPath);
                if (linkedObjectId != null) {
                    result.add(crudService.find(linkedObjectId)); // it can't be null as reference is set
                }
            }
            return result;
        }

        // it's a "back-link" (like country_best_friend^country)
        String[] domainObjectTypeAndReference = linkPath.split("\\^");
        if (domainObjectTypeAndReference.length != 2) {
            throw new GuiException("Invalid reference: " + linkPath);
        }

        String referenceField = domainObjectTypeAndReference[1];

        // todo after CMFIVE-122 is done - get the first two linked objects, not everything!
        // todo after cardinality functionality is developed, check cardinality (static-check, not runtime)

        ObjectsNode result = new ObjectsNode(linkedType, parentNode.size());
        for (DomainObject domainObject : parentNode) {
            List<DomainObject> linkedDomainObjects = domainObject.getId() == null
                    ? new ArrayList<DomainObject>()
                    : crudService.findLinkedDomainObjects(domainObject.getId(), linkedType, referenceField);
            if (linkedDomainObjects.size() > 1 && parentNode.size() > 1) {
                // join 2 multi-references - not supported and usually doesn't make sense
                throw new GuiException(linkPath + " is resulting into many-on-many join which is not supported");
            }
            for (DomainObject linkedDomainObject : linkedDomainObjects) {
                result.add(linkedDomainObject);
            }
        }

        return result;
    }
}
