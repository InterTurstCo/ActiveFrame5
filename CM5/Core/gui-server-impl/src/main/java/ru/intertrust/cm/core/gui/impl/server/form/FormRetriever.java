package ru.intertrust.cm.core.gui.impl.server.form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.FormViewerMappingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.DomainObjectUpdater;
import ru.intertrust.cm.core.gui.api.server.plugin.FormMappingHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.MultiObjectNode;
import ru.intertrust.cm.core.gui.model.form.ObjectsNode;
import ru.intertrust.cm.core.gui.model.form.SingleObjectNode;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 21.10.13
 *         Time: 13:03
 */
public class FormRetriever extends FormProcessor {
    private static Logger log = LoggerFactory.getLogger(FormRetriever.class);

    @Autowired
    private FormResolver formResolver;

    public FormDisplayData getForm(String domainObjectType, FormViewerConfig formViewerConfig) {
        DomainObject root = crudService.createDomainObject(domainObjectType);
        // todo: separate empty form?
        return buildDomainObjectForm(root, formViewerConfig);
    }

    public FormDisplayData getForm(String domainObjectType, String updaterComponentName, Dto updaterContext, FormViewerConfig formViewerConfig) {
        DomainObject root = crudService.createDomainObject(domainObjectType);
        DomainObjectUpdater domainObjectUpdater = (DomainObjectUpdater) applicationContext.getBean(updaterComponentName);
        domainObjectUpdater.updateDomainObject(root, updaterContext);
        // todo: separate empty form?
        return buildDomainObjectForm(root, formViewerConfig);
    }

    public FormDisplayData getForm(Id domainObjectId, FormViewerConfig formViewerConfig) {
        DomainObject root = crudService.find(domainObjectId);
        if (root == null) {
            throw new GuiException("Object with id: " + domainObjectId.toStringRepresentation() + " doesn't exist");
        }
        return buildDomainObjectForm(root, formViewerConfig);
    }

    public FormDisplayData getForm(Id domainObjectId, String updaterComponentName, Dto updaterContext, FormViewerConfig formViewerConfig) {
        DomainObject root = crudService.find(domainObjectId);
        if (root == null) {
            throw new GuiException("Object with id: " + domainObjectId.toStringRepresentation() + " doesn't exist");
        }
        DomainObjectUpdater domainObjectUpdater = (DomainObjectUpdater) applicationContext.getBean(updaterComponentName);
        domainObjectUpdater.updateDomainObject(root, updaterContext);
        return buildDomainObjectForm(root, formViewerConfig);
    }


    // форма поиска для доменного объекта указанного типа
    public FormDisplayData getSearchForm(String domainObjectType, HashSet<String> formFields) {
        return buildExtendedSearchForm(domainObjectType, formFields);
    }

    private FormDisplayData buildExtendedSearchForm(String domainObjectType, HashSet<String> formFields) {
        //FormConfig formConfig = formResolver.findFormConfig(root, userUid); was 30.01.2014
        FormConfig formConfig = formResolver.findSearchFormConfig(domainObjectType, getUserUid());
        if (formConfig == null) {
            return null; //throw new GuiException("Конфигурация поиска для ДО " + domainObjectType + " не найдена!");
        }
        List<WidgetConfig> widgetConfigs = findWidgetConfigs(formConfig);

        FormObjects formObjects = new FormObjects();
        DomainObject root = crudService.createDomainObject(domainObjectType);
        final ObjectsNode ROOT_NODE = new SingleObjectNode(root);
        formObjects.setRootNode(ROOT_NODE);

        HashMap<String, WidgetState> widgetStateMap = buildWidgetStatesMap(widgetConfigs, formObjects, formConfig);
        HashMap<String, String> widgetComponents = buildWidgetComponentsMap(widgetConfigs);

        FormState formState = new FormState(formConfig.getName(), widgetStateMap, formObjects, widgetComponents,
                MessageResourceProvider.getMessages());
        return new FormDisplayData(formState, formConfig.getMarkup(), widgetComponents,
                                                                       formConfig.getMinWidth(), formConfig.getDebug());
    }

    public FormDisplayData getReportForm(String reportName, String formName) {
        FormConfig formConfig = null;
        if (formName != null) {
            formConfig = configurationExplorer.getConfig(FormConfig.class, formName);
        }

        boolean formIsInvalid = (formConfig == null) ||
                !FormConfig.TYPE_REPORT.equals(formConfig.getType()) ||
                (formConfig.getReportTemplate() != null && reportName != null && !formConfig.getReportTemplate().equals(reportName));


        if (formIsInvalid) {
            if (reportName != null) {
                formConfig = formResolver.findReportFormConfig(reportName, getUserUid());
            }
        }
        if (formConfig == null) {
            throw new GuiException(String.format("Конфигурация формы отчета не найдена или некорректна! Форма: '%s', отчет: '%s'", formName, reportName));
        }
        if (formName == null) {
            formName = formConfig.getName();
        }
        if (reportName == null) {
            reportName = formConfig.getReportTemplate();
        }
        if  (reportName == null) {
            throw new GuiException("Имя отчета не сконфигурировано ни в плагине, ни форме!");
        }

        List<WidgetConfig> widgetConfigs = findWidgetConfigs(formConfig);

        FormObjects formObjects = new FormObjects();

        GenericDomainObject root = new GenericDomainObject();
        root.setTypeName(reportName);
        ObjectsNode ROOT_NODE = new SingleObjectNode(root);
        formObjects.setRootNode(ROOT_NODE);

        HashMap<String, WidgetState> widgetStateMap = buildWidgetStatesMap(widgetConfigs, formObjects, formConfig);
        HashMap<String, String> widgetComponents = buildWidgetComponentsMap(widgetConfigs);

        FormState formState = new FormState(formName, widgetStateMap, formObjects, widgetComponents,
                MessageResourceProvider.getMessages());
        return new FormDisplayData(formState, formConfig.getMarkup(), widgetComponents,
                formConfig.getMinWidth(), formConfig.getDebug());
    }


    private HashMap<String, WidgetState> buildWidgetStatesMap(List<WidgetConfig> widgetConfigs, FormObjects formObjects,
                                                              FormConfig formConfig) {
        HashMap<String, WidgetState> widgetStateMap = new HashMap<>(widgetConfigs.size());

        for (WidgetConfig config : widgetConfigs) {
            String widgetId = config.getId();

            WidgetContext widgetContext = new WidgetContext(config, formObjects);
            widgetContext.setFormType(formConfig.getType());
            WidgetHandler componentHandler = (WidgetHandler) applicationContext.getBean(config.getComponentName());
            WidgetState initialState = componentHandler.getInitialState(widgetContext);

            // TODO: [report-plugin] validation...
            WidgetContext context = new WidgetContext(config, formObjects); // why don't we re-use widgetContext?
            context.setFormType(formConfig.getType());
            List<Constraint> constraints = buildConstraints(context);
            initialState.setConstraints(constraints);
            initialState.setWidgetProperties(buildWidgetProps(widgetContext, constraints));
            boolean readOnly = widgetContext.getWidgetConfig().isReadOnly();
            initialState.setEditable(!readOnly);
            widgetStateMap.put(widgetId, initialState);
        }
        return widgetStateMap;
    }

    private HashMap<String, String> buildWidgetComponentsMap(List<WidgetConfig> widgetConfigs) {
        HashMap<String, String> widgetComponents = new HashMap<>(widgetConfigs.size());
        for (WidgetConfig config : widgetConfigs) {
            String widgetId = config.getId();
            widgetComponents.put(widgetId, config.getComponentName());
        }
        return widgetComponents;
    }

    private FormDisplayData buildDomainObjectForm(DomainObject root, FormViewerConfig formViewerConfig) {
        // todo validate that constructions like A^B.C.D aren't allowed or A.B^C
        // allowed are such definitions only:
        // a.b.c.d - direct links
        // a^b - link defining 1:N relationship (widgets changing attributes can't have such field path)
        // a^b.c - link defining N:M relationship (widgets changing attributes can't have such field path)

        FormConfig formConfig = null;
        if (formViewerConfig != null && formViewerConfig.getFormMappingComponent() != null) {
            FormMappingHandler formMappingHandler = (FormMappingHandler) applicationContext.getBean(formViewerConfig.getFormMappingComponent());
            if (formMappingHandler != null) {
                formConfig = formMappingHandler.findEditingFormConfig(root, getUserUid());
            }
        } else if (formViewerConfig != null && formViewerConfig.getFormMappingConfigList() != null) {
            for (FormViewerMappingConfig mappingConfig : formViewerConfig.getFormMappingConfigList()) {
                if (root.getTypeName().equals(mappingConfig.getDomainObjectType())) {
                    String formName = mappingConfig.getForm();
                    formConfig = configurationExplorer.getConfig(FormConfig.class, formName);
                }
            }
        }
        if (formConfig == null) {
            formConfig = formResolver.findEditingFormConfig(root, getUserUid());
        }
        List<WidgetConfig> widgetConfigs = findWidgetConfigs(formConfig);

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
                widgetContext.setFormType(formConfig.getType());
                WidgetHandler componentHandler = (WidgetHandler) applicationContext.getBean(config.getComponentName());
                WidgetState initialState = componentHandler.getInitialState(widgetContext);
                boolean readOnly = widgetContext.getWidgetConfig().isReadOnly();
                initialState.setEditable(!readOnly);
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
            widgetContext.setFormType(formConfig.getType());
            WidgetHandler componentHandler = (WidgetHandler) applicationContext.getBean(config.getComponentName());
            WidgetState initialState = componentHandler.getInitialState(widgetContext);
            List<Constraint> constraints = buildConstraints(widgetContext);
            initialState.setConstraints(constraints);
            initialState.setWidgetProperties(buildWidgetProps(widgetContext, constraints));
            boolean readOnly = widgetContext.getWidgetConfig().isReadOnly();
            initialState.setEditable(!readOnly);
            widgetStateMap.put(widgetId, initialState);
            widgetComponents.put(widgetId, config.getComponentName());
        }
        FormState formState = new FormState(formConfig.getName(), widgetStateMap, formObjects, widgetComponents,
                MessageResourceProvider.getMessages());
        final FormDisplayData result = new FormDisplayData(formState, formConfig.getMarkup(), widgetComponents,
                formConfig.getMinWidth(), formConfig.getDebug());
        result.setToolBarConfig(formConfig.getToolbarConfig());
        return result;
    }

    private List<WidgetConfig> findWidgetConfigs(FormConfig formConfig) {
        List<WidgetConfig> widgetConfigs = new ArrayList<>();
        Collection<String> widgetsToHide = formResolver.findWidgetsToHide(getUserUid(), formConfig.getName(), formConfig.getType());
        for (WidgetConfig widgetConfig : formConfig.getWidgetConfigurationConfig().getWidgetConfigList()) {
            if (!widgetsToHide.contains(widgetConfig.getId())) {
                widgetConfigs.add(widgetConfig);
            }
        }
        return widgetConfigs;
    }

    private List<Constraint> buildConstraints(WidgetContext context) {
        List<Constraint> constraints = new ArrayList<Constraint>();

        String doTypeName = null;
        String fieldName = null;

        WidgetConfig widgetConfig = context.getWidgetConfig();
        if (widgetConfig instanceof LabelConfig) {
            return constraints;
        }
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());

        if (fieldPath.isField() || fieldPath.isOneToOneReference()) {
            fieldName = fieldPath.getPath(); // fieldPath.getFieldName(); //TODO: looks like fieldPath.isOneToOneReference() works incorrectly
            doTypeName = context.getFormObjects().getRootNode().getType();
        } else if (fieldPath.isOneToManyReference()) {
            fieldName = fieldPath.getReferenceName();
            doTypeName = fieldPath.getLinkedObjectType();
        } else /* ManyToMany */ {
            fieldName = fieldPath.getReferenceName();
            doTypeName = fieldPath.getLinkingObjectType();
        }
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(doTypeName, fieldName);

        String widgetId = widgetConfig.getId();
        if (fieldConfig != null) {
            List<Constraint> fieldConfigConstraints = fieldConfig.getConstraints();
            for (Constraint constraint : fieldConfigConstraints) {
                constraint.addParam(Constraint.PARAM_WIDGET_ID, widgetId);
                constraint.addParam(Constraint.PARAM_DOMAIN_OBJECT_TYPE, doTypeName);
                constraint.addParam(Constraint.PARAM_FIELD_NAME, fieldName);
            }
            constraints.addAll(fieldConfigConstraints);
        }
        return constraints;
    }

    private ObjectsNode findLinkedNode(SingleObjectNode parentNode, FieldPath childPath) {
        if (childPath.isOneToOneDirectReference()) { // direct reference
            return findOneToOneDirectReferenceLinkedNode(parentNode, childPath);
        } else { // back reference
            return findBackReferenceLinkedNode(parentNode, childPath);
        }
    }

    private SingleObjectNode findOneToOneDirectReferenceLinkedNode(SingleObjectNode parentNode, FieldPath childPath) {
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

    private ObjectsNode findBackReferenceLinkedNode(SingleObjectNode parentNode, FieldPath childPath) {
        final String linkedType = childPath.getReferenceType();
        final boolean oneToOneBackReference = childPath.isOneToOneBackReference();
        if (parentNode.isEmpty()) {
            return oneToOneBackReference ? new SingleObjectNode(linkedType) : new MultiObjectNode(linkedType);
        }

        String referenceField = childPath.getLinkToParentName();

        // todo after cardinality functionality is developed, check cardinality (static-check, not runtime)
        // todo: limit result rows
        DomainObject parentDomainObject = parentNode.getDomainObject();
        List<DomainObject> linkedDomainObjects = parentDomainObject.getId() == null
                ? new ArrayList<DomainObject>()
                : crudService.findLinkedDomainObjects(parentDomainObject.getId(), linkedType, referenceField);
        if (!oneToOneBackReference) {
            return new MultiObjectNode(linkedType, linkedDomainObjects);
        }
        return linkedDomainObjects.isEmpty() ? new SingleObjectNode(linkedType) : new SingleObjectNode(linkedDomainObjects.get(0));
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
        for (Constraint constraint : constraints) {
            props.putAll(constraint.getParams());
        }
        return props;
    }
}
