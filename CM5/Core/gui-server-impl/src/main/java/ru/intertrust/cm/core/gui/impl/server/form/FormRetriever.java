package ru.intertrust.cm.core.gui.impl.server.form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.*;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.*;

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

    // форма поиска для доменного объекта указанного типа
    public FormDisplayData getSearchForm(String domainObjectType, HashSet<String> formFields) {
        return buildExtendedSearchForm(domainObjectType, formFields);
    }

    private FormDisplayData buildExtendedSearchForm(String domainObjectType, HashSet<String> formFields) {
        //FormConfig formConfig = formResolver.findFormConfig(root, userUid); was 30.01.2014
        FormConfig formConfig = formResolver.findSearchForm(domainObjectType, userUid);
        if (formConfig == null) {
            return null; //throw new GuiException("Конфигурация поиска для ДО " + domainObjectType + " не найдена!");
        }
        List<WidgetConfig> widgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();

        HashMap<String, WidgetState> widgetStateMap = new HashMap<>(widgetConfigs.size());
        HashMap<String, String> widgetComponents = new HashMap<>(widgetConfigs.size());

        FormObjects formObjects = new FormObjects();
        //Iterator it = widgetConfigs.iterator();
        DomainObject root = crudService.createDomainObject(domainObjectType);
        final ObjectsNode ROOT_NODE = new SingleObjectNode(root);
        formObjects.setRootNode(ROOT_NODE);

        //try {
        for (WidgetConfig config : widgetConfigs) {
        //while(it.hasNext()) {
            try {
                //config = (WidgetConfig) it.next();
                String widgetId = config.getId();

                WidgetContext widgetContext = new WidgetContext(config, formObjects);
                WidgetHandler componentHandler = (WidgetHandler) applicationContext.getBean(config.getComponentName());
                WidgetState initialState = componentHandler.getInitialState(widgetContext);

                initialState.setEditable(true);
                widgetStateMap.put(widgetId, initialState);
                widgetComponents.put(widgetId, config.getComponentName());
            } catch (NullPointerException npe) {continue;}
        }

        //} catch (ConcurrentModificationException cme) {}

        FormState formState = new FormState(formConfig.getName(), widgetStateMap, formObjects,
                MessageResourceProvider.getMessages());
        return new FormDisplayData(formState, formConfig.getMarkup(), widgetComponents,
                                                                       formConfig.getMinWidth(), formConfig.getDebug());
    }

    private FormDisplayData buildDomainObjectForm(DomainObject root) {
        // todo validate that constructions like A^B.C.D aren't allowed or A.B^C
        // allowed are such definitions only:
        // a.b.c.d - direct links
        // a^b - link defining 1:N relationship (widgets changing attributes can't have such field path)
        // a^b.c - link defining N:M relationship (widgets changing attributes can't have such field path)
        FormConfig formConfig = formResolver.findFormConfig(root, userUid);
        List<WidgetConfig> widgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();
        HashMap<String, WidgetConfig> widgetConfigsById = buildWidgetConfigsById(widgetConfigs);
        HashMap<String, WidgetState> widgetStateMap = new HashMap<>(widgetConfigs.size());
        HashMap<String, String> widgetComponents = new HashMap<>(widgetConfigs.size());
        FormObjects formObjects = new FormObjects();

        final ObjectsNode ROOT_NODE = new SingleObjectNode(root);
        formObjects.setRootNode(ROOT_NODE);
        for (WidgetConfig config : widgetConfigs) {
            String widgetId = config.getId();
            FieldPathConfig fieldPathConfig = config.getFieldPathConfig();
            if (fieldPathConfig == null || fieldPathConfig.getValue() == null) {
                if (!(config instanceof LabelConfig)) {
                    throw new GuiException("Widget, id: " + widgetId + " is not configured with Field Path");
                }

                //todo refactor
                WidgetContext widgetContext = new WidgetContext(config, formObjects, widgetConfigsById);
                WidgetHandler componentHandler = (WidgetHandler) applicationContext.getBean(config.getComponentName());
                WidgetState initialState = componentHandler.getInitialState(widgetContext);
                widgetStateMap.put(widgetId, initialState);
                widgetComponents.put(widgetId, config.getComponentName());
                continue;
            }

            // field path config can point to multiple paths
            FieldPath[] fieldPaths = FieldPath.createPaths(fieldPathConfig.getValue());

            for (FieldPath fieldPath : fieldPaths) {
                ObjectsNode currentRootNode = ROOT_NODE;
                for (Iterator<FieldPath> childrenIterator = fieldPath.childrenIterator(); childrenIterator.hasNext(); ) {
                    FieldPath childPath = childrenIterator.next();
                    if (childPath.isField()) {
                        break;
                    }

                    if (formObjects.containsNode(childPath)) {
                        currentRootNode = formObjects.getNode(childPath);
                        continue;
                    }

                    // it's a reference. linked objects can exist only for Single-Object Nodes. class-cast exception will
                    // raise if that's not true
                    ObjectsNode linkedNode = findLinkedNode((SingleObjectNode) currentRootNode, childPath);

                    formObjects.setNode(childPath, linkedNode);
                    currentRootNode = linkedNode;
                }
            }

            WidgetContext widgetContext = new WidgetContext(config, formObjects, widgetConfigsById);
            WidgetHandler componentHandler = (WidgetHandler) applicationContext.getBean(config.getComponentName());
            WidgetState initialState = componentHandler.getInitialState(widgetContext);
            List<Constraint> constraints = buildConstraints(widgetContext);
            initialState.setConstraints(constraints);
            initialState.setWidgetProperties(buildWidgetProps(widgetContext, constraints));

            initialState.setEditable(true);
            widgetStateMap.put(widgetId, initialState);
            widgetComponents.put(widgetId, config.getComponentName());
        }
        FormState formState = new FormState(formConfig.getName(), widgetStateMap, formObjects,
                MessageResourceProvider.getMessages());
        return new FormDisplayData(formState, formConfig.getMarkup(), widgetComponents,
                formConfig.getMinWidth(), formConfig.getDebug());
    }

    private List<Constraint> buildConstraints(WidgetContext context) {
        List<Constraint> constraints = new ArrayList<Constraint>();
        String doTypeName = context.getFormObjects().getRootNode().getType();
        WidgetConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        String fieldName = fieldPath.getFieldName();

        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(doTypeName, fieldName);

        if (fieldConfig != null) {
           constraints.addAll(fieldConfig.getConstraints());
        }
        return constraints;
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

    private HashMap<String, WidgetConfig> buildWidgetConfigsById(List<WidgetConfig> widgetConfigs) {
        HashMap<String, WidgetConfig> widgetConfigsById = new HashMap<>(widgetConfigs.size());
        for (WidgetConfig config : widgetConfigs) {
            widgetConfigsById.put(config.getId(), config);
        }
        return widgetConfigsById;
    }

    private HashMap<String, Object> buildWidgetProps(WidgetContext context, List<Constraint> constraints) {
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("domain-object-type", context.getFormObjects().getRootNode().getType());

        WidgetConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        props.put(Constraint.FIELD_NAME, fieldPath.getFieldName());

        for (Constraint constraint : constraints) {
            props.putAll(constraint.getParams());
        }
        return props;
    }
}
