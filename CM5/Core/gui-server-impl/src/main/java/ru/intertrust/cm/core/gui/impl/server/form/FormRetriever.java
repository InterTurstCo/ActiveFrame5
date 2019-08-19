package ru.intertrust.cm.core.gui.impl.server.form;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.FormMappingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageKey;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.DomainObjectUpdater;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.plugin.FormMappingHandler;
import ru.intertrust.cm.core.gui.api.server.widget.FormDefaultValueSetter;
import ru.intertrust.cm.core.gui.api.server.widget.SelfManagingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.*;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.util.PlaceholderResolver;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 21.10.13
 *         Time: 13:03
 */
public class FormRetriever extends FormProcessor {

  private static Logger log = LoggerFactory.getLogger(FormRetriever.class);

  @Autowired
  private FormResolver formResolver;

  @Autowired
  ConfigurationExplorer configurationExplorer;

  @Autowired
  ProfileService profileService;

  public FormDisplayData getForm(FormPluginConfig formPluginConfig) {
    DomainObject root = crudService.createDomainObject(formPluginConfig.getDomainObjectTypeToCreate());
    if (formPluginConfig.getDomainObjectUpdatorComponent() != null) {
      DomainObjectUpdater domainObjectUpdater = (DomainObjectUpdater) applicationContext
          .getBean(formPluginConfig.getDomainObjectUpdatorComponent());
      domainObjectUpdater.updateDomainObject(root, formPluginConfig.getUpdaterContext());
    }
    return buildDomainObjectForm(root, formPluginConfig.getFormViewerConfig(), formPluginConfig, formPluginConfig.getParentFormState(),
        formPluginConfig.getParentId(), formPluginConfig.getLastCollectionRowSelectedId());
  }

  public FormDisplayData getForm(Id domainObjectId, FormViewerConfig formViewerConfig) {
    return getForm(domainObjectId, null, null, formViewerConfig);
  }

  public FormDisplayData getForm(Id domainObjectId, String updaterComponentName, Dto updaterContext, FormViewerConfig formViewerConfig) {
    DomainObject root = (domainObjectId!=null)?crudService.find(domainObjectId):null;
    if (root == null) {
      throw new GuiException(buildMessage(LocalizationKeys.GUI_EXCEPTION_OBJECT_NOT_EXIST,
          "Object with id: ${objectId} doesn't exist", new Pair("objectId",
              (domainObjectId!=null)?domainObjectId.toStringRepresentation():"null")));
    }
    if (updaterComponentName != null) {
      DomainObjectUpdater domainObjectUpdater = (DomainObjectUpdater) applicationContext.getBean(updaterComponentName);
      domainObjectUpdater.updateDomainObject(root, updaterContext);
    }
    return buildDomainObjectForm(root, formViewerConfig, null, null, null, null);
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
    HashMap<String, String> widgetComponents = buildWidgetComponentsMap(widgetConfigs);
    HashMap<String, WidgetState> widgetStateMap = new HashMap<>(widgetConfigs.size());

    FormState formState = new FormState(formConfig.getName(), widgetStateMap, formObjects, widgetComponents,
        MessageResourceProvider.getMessages(GuiContext.getUserLocale()), null);
    fillWidgetStatesMap(widgetConfigs, formState, formConfig);

    return new FormDisplayData(formState, formConfig.getMarkup(), widgetComponents,
        formConfig.getMinWidth(), formConfig.getDebug());
  }

  public FormDisplayData getReportForm(String reportName, String formName) {
    FormConfig formConfig = null;
    if (formName != null) {
      formConfig = getLocalizedFormConfig(formName);
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
      throw new GuiException(String.format(buildMessage(LocalizationKeys.GUI_EXCEPTION_REPORT_FORM_ERROR,
          "Конфигурация формы отчета не найдена или некорректна! Форма: '${formName}', отчет: '${reportName}'",
          new Pair("formName", formName),
          new Pair("reportName", reportName))));
    }
    if (formName == null) {
      formName = formConfig.getName();
    }
    if (reportName == null) {
      reportName = formConfig.getReportTemplate();
    }
    if (reportName == null) {
      throw new GuiException(buildMessage(LocalizationKeys.GUI_EXCEPTION_REPORT_NAME_NOT_FOUND,
          "Имя отчета не сконфигурировано ни в плагине, ни форме!"));
    }
    List<WidgetConfig> widgetConfigs = findWidgetConfigs(formConfig);
    FormObjects formObjects = new FormObjects();
    GenericDomainObject root = new GenericDomainObject();
    root.setTypeName(reportName);
    ObjectsNode ROOT_NODE = new SingleObjectNode(root);
    formObjects.setRootNode(ROOT_NODE);
    HashMap<String, String> widgetComponents = buildWidgetComponentsMap(widgetConfigs);
    HashMap<String, WidgetState> widgetStateMap = new HashMap<>(widgetConfigs.size());
    FormState formState = new FormState(formName, widgetStateMap, formObjects, widgetComponents,
        MessageResourceProvider.getMessages(GuiContext.getUserLocale()), null);
    fillWidgetStatesMap(widgetConfigs, formState, formConfig);

    return new FormDisplayData(formState, formConfig.getMarkup(), widgetComponents,
        formConfig.getMinWidth(), formConfig.getDebug());
  }


  private void fillWidgetStatesMap(List<WidgetConfig> widgetConfigs, FormState formState,
                                   FormConfig formConfig) {
    Map<String, WidgetState> widgetStateMap = formState.getFullWidgetsState();
    HashMap<String, WidgetConfig> widgetConfigsById = buildWidgetConfigsById(widgetConfigs);
    for (WidgetConfig config : widgetConfigs) {
      String widgetId = config.getId();

      WidgetContext widgetContext = new WidgetContext(config, formState.getObjects(), widgetConfigsById);
      widgetContext.setFormType(formConfig.getType());
      WidgetHandler componentHandler = getWidgetHandler(config);
      if (config.getFieldPathConfig() != null) {
        String fieldPathValue = config.getFieldPathConfig().getValue();
        if (fieldPathValue != null && !fieldPathValue.isEmpty()) {
          FieldPath[] paths = FieldPath.createPaths(fieldPathValue);
          FormDefaultValueSetter formDefaultValueSetter;
          formDefaultValueSetter = obtainFormDefaultValueSetter(formConfig, null);
          applyDefaultValuesToFormObjects(formState, paths, formDefaultValueSetter);
        }
      }
      WidgetState initialState = componentHandler.getInitialState(widgetContext);
      fillSubscriptionAndRules(initialState,widgetContext);
      WidgetContext context = new WidgetContext(config, formState.getObjects(), widgetConfigsById); // why don't we re-use widgetContext?

      context.setFormType(formConfig.getType());
      List<Constraint> constraints = buildConstraints(context);
      initialState.setConstraints(constraints);
      initialState.setWidgetProperties(buildWidgetProps(constraints, formConfig.getType()));
      boolean readOnly = widgetContext.getWidgetConfig().isReadOnly();
      initialState.setEditable(!readOnly);
      widgetStateMap.put(widgetId, initialState);
    }
    removeNotApplicableConstraints(widgetStateMap);
    buildForceRequiredConstraints(widgetStateMap, widgetConfigsById, formConfig.getType(), formState.getObjects());

  }

  private void removeNotApplicableConstraints(Map<String, WidgetState> widgetStateMap) {
    for (WidgetState widgetState : widgetStateMap.values()) {
      for (Iterator<Constraint> iter = widgetState.getConstraints().iterator(); iter.hasNext(); ) {
        if (!iter.next().isApplicableInSearchAndReportForm()) {
          iter.remove();
        }
      }
    }
  }

  private void buildForceRequiredConstraints(Map<String, WidgetState> widgetStateMap,
                                             Map<String, WidgetConfig> widgetConfigsById, String formConfig,
                                             FormObjects formObjects) {
    for (Map.Entry<String, WidgetState> entry : widgetStateMap.entrySet()) {
      String widgetId = entry.getKey();
      WidgetState widgetState = entry.getValue();
      WidgetConfig widgetConfig = widgetConfigsById.get(widgetId);
      if ("label".equals(widgetConfig.getComponentName())) {
        LabelState labelState = (LabelState) widgetState;
        if (labelState.isAsteriskRequired()) {
          String relatedWidget = labelState.getRelatedWidgetId();
          WidgetState relatedWidgetState = widgetStateMap.get(relatedWidget);
          if (relatedWidgetState != null) {
            WidgetConfig relatedWidgetConfig = widgetConfigsById.get(relatedWidget);
            FieldPath fieldPath = new FieldPath(relatedWidgetConfig.getFieldPathConfig().getValue());
            if (fieldPath.isField() || fieldPath.isOneToOneReference()) {
              HashMap<String, String> params = new HashMap<>();
              params.put(Constraint.PARAM_PATTERN, Constraint.KEYWORD_NOT_EMPTY);
              params.put(Constraint.PARAM_WIDGET_ID, relatedWidget);
              String fieldName = fieldPath.getPath();
              params.put(Constraint.PARAM_FIELD_NAME, fieldName);
              String domainObjectType = formObjects.getDomainObjectType(fieldPath);
              params.put(Constraint.PARAM_DOMAIN_OBJECT_TYPE, domainObjectType);
              relatedWidgetState.getConstraints().add(new Constraint(Constraint.Type.SIMPLE, params));
              relatedWidgetState.setWidgetProperties(buildWidgetProps(relatedWidgetState.getConstraints(), formConfig));
            }
          }
        }
      }
    }
  }

  private FormDefaultValueSetter obtainFormDefaultValueSetter(FormConfig formConfig, FormMappingConfig formViewerMappingConfig) {
    FormDefaultValueSetter formDefaultValueSetter;
    String lowerPriorityUserValueSetter = formConfig.getDefaultValueSetter();
    String higherPriorityUserValueSetter = formConfig.getDefaultValueSetterConfig() == null ? null
        : formConfig.getDefaultValueSetterConfig().getComponent();
    if (higherPriorityUserValueSetter != null && !higherPriorityUserValueSetter.isEmpty()) {
      formDefaultValueSetter = (FormDefaultValueSetter) applicationContext.getBean(higherPriorityUserValueSetter);
    } else if (lowerPriorityUserValueSetter != null && !lowerPriorityUserValueSetter.isEmpty()) {
      formDefaultValueSetter = (FormDefaultValueSetter) applicationContext.getBean(lowerPriorityUserValueSetter);
    } else {
      formDefaultValueSetter = (FormDefaultValueSetter) applicationContext.getBean("formDefaultValueSetter", formConfig, formViewerMappingConfig);
    }
    return formDefaultValueSetter;
  }

  private HashMap<String, String> buildWidgetComponentsMap(List<WidgetConfig> widgetConfigs) {
    HashMap<String, String> widgetComponents = new HashMap<>(widgetConfigs.size());
    for (WidgetConfig config : widgetConfigs) {
      String widgetId = config.getId();
      widgetComponents.put(widgetId, config.getComponentName());
    }
    return widgetComponents;
  }

    private FormDisplayData buildDomainObjectForm(DomainObject root,
                                                  FormViewerConfig formViewerConfig,
                                                  FormPluginConfig formPluginConfig,
                                                  FormState parentFormState,
                                                  Id parentId,
                                                  Id lastCollectionRowSelectedId) {
    //, , FormState parentFormState, Id parentId
    // todo validate that constructions like A^B.C.D aren't allowed or A.B^C
    // allowed are such definitions only:
    // a.b.c.d - direct links
    // a^b - link defining 1:N relationship (widgets changing attributes can't have such field path)
    // a^b.c - link defining N:M relationship (widgets changing attributes can't have such field path)
    FormConfig formConfig = loadFormConfig(root, formViewerConfig, formPluginConfig);
    FormMappingConfig formViewerMappingConfig = findFormViewerMappingConfig(root, formViewerConfig);
    FormDefaultValueSetter formDefaultValueSetter = obtainFormDefaultValueSetter(formConfig, formViewerMappingConfig);

    List<WidgetConfig> widgetConfigs = findWidgetConfigs(formConfig);
    final HashMap<String, WidgetConfig> widgetConfigsById = buildWidgetConfigsById(widgetConfigs);
    final HashMap<String, WidgetState> widgetStateMap = new HashMap<>(widgetConfigs.size());
    final HashMap<String, String> widgetComponents = new HashMap<>(widgetConfigs.size());
    final FormObjects formObjects = new FormObjects();
    final ObjectsNode ROOT_NODE = new SingleObjectNode(root);
    formObjects.setRootNode(ROOT_NODE);
    FormState formState = new FormState(formConfig.getName(), widgetStateMap, formObjects, widgetComponents,
        MessageResourceProvider.getMessages(GuiContext.getUserLocale()), formViewerConfig);

    formState.setParentState(parentFormState);
    formState.setParentId(parentId);
    formState.setLastCollectionRowSelectedId(lastCollectionRowSelectedId);

    /*
     Если это свободный тип формы, филдпасы компонентов не проверяем, за их содержимое и дальнейшую
     обработку несет ответственность разработчик
    */

    for (final WidgetConfig config : widgetConfigs) {
      String widgetId = config.getId();
      WidgetHandler widgetHandler = getWidgetHandler(config);
      final boolean selfManagingWidget = widgetHandler instanceof SelfManagingWidgetHandler;
      WidgetContext widgetContext = new WidgetContext(config, formObjects, formConfig.getType(), widgetConfigsById);
      FieldPathConfig fieldPathConfig = config.getFieldPathConfig();
      if (fieldPathConfig == null || fieldPathConfig.getValue() == null || selfManagingWidget) {
        if (!selfManagingWidget
            &&
            !(config instanceof LabelConfig)
            &&
            !(config instanceof AttachmentViewerConfig)
            &&
            !"free-form".equals(formConfig.getType())) {
          throw new GuiException(buildMessage(LocalizationKeys.GUI_EXCEPTION_WIDGET_ID_NOT_FOUND,
              "Widget, id: ${widgetId} is not configured with Field Path",
              new Pair("widgetId", widgetId)));
        }

        if( "free-form".equals(formConfig.getType())
            && fieldPathConfig!=null){
          fieldPathConfig.setValue("fakeFieldPath");
        }
          WidgetState initialState = widgetHandler.getInitialState(widgetContext);
          fillSubscriptionAndRules(initialState,widgetContext);
          if (initialState != null) {
            boolean readOnly = widgetContext.getWidgetConfig().isReadOnly();
            initialState.setEditable(!readOnly);
            widgetStateMap.put(widgetId, initialState);
            widgetComponents.put(widgetId, config.getComponentName());
            if(config.getTranslateId()){
              initialState.setTranslateId(config.getTranslateId());
              initialState.setWidgetId(config.getId());
            }
          }

        continue;
      }


      // field path config can point to multiple paths`
      boolean readOnly = config.isReadOnly();
      FieldPath[] fieldPaths = FieldPath.createPaths(fieldPathConfig.getValue());
      final ExactTypesConfig exactTypesConfig = fieldPathConfig.getExactTypesConfig();
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
          ObjectsNode linkedNode = findLinkedNode((SingleObjectNode) currentRootNode, childPath, exactTypesConfig);
          if (configurationExplorer.isAuditLogType(linkedNode.getType())) {
            readOnly = true;
          }
          formObjects.setNode(childPath, linkedNode);
          currentRootNode = linkedNode;
        }
      }

      //apply default values
      if (root.isNew()) {
        applyDefaultValuesToFormObjects(formState, fieldPaths, formDefaultValueSetter);
      }

      WidgetState initialState = widgetHandler.getInitialState(widgetContext);
      fillSubscriptionAndRules(initialState,widgetContext);
      List<Constraint> constraints = buildConstraints(widgetContext);
      initialState.setConstraints(constraints);
      initialState.setWidgetProperties(buildWidgetProps(constraints, formConfig.getType()));
      initialState.setEditable(!readOnly);
      if(config.getTranslateId()){
        initialState.setTranslateId(config.getTranslateId());
        initialState.setWidgetId(config.getId());
      }
      widgetStateMap.put(widgetId, initialState);
      widgetComponents.put(widgetId, config.getComponentName());
    }

    buildForceRequiredConstraints(widgetStateMap, widgetConfigsById, formConfig.getType(), formObjects);
    formState.clearParentStateAndId(); //no need to send parentState for client
    final FormDisplayData result = new FormDisplayData(formState, formConfig.getMarkup(), widgetComponents,
        formConfig.getMinWidth(), formConfig.getDebug());
    result.setToolBarConfig(formConfig.getToolbarConfig());
    result.setScriptFileConfig(formConfig.getScriptFileConfig());
    return result;
  }

  private void applyDefaultValuesToFormObjects(FormState formState, FieldPath[] fieldPaths, FormDefaultValueSetter formDefaultValueSetter) {
    if (formDefaultValueSetter != null) {
      FormObjects formObjects = formState.getObjects();
      for (FieldPath fieldPath : fieldPaths) {
        if (fieldPath.isField() || fieldPath.isOneToOneReference()) {
          Value defaultValue = formDefaultValueSetter.getDefaultValue(formState, fieldPath);
          if (defaultValue != null) {
            if (formObjects.getFieldValue(fieldPath) == null) {
              formObjects.setFieldValue(fieldPath, defaultValue);
            }
          }
        } else if (fieldPath.isManyToManyReference() || fieldPath.isOneToManyReference()) {
          Value[] defaultValues = formDefaultValueSetter.getDefaultValues(formState, fieldPath);
          if (defaultValues == null || defaultValues.length == 0) {
            return;
          }

          ObjectsNode node = formObjects.getNode(fieldPath);
          MultiObjectNode multiObjectNode = (MultiObjectNode) node;

          if (multiObjectNode.getDomainObjects().isEmpty()) {
            createIntermediateObjectsForDefaultValues(formObjects, fieldPath, defaultValues, multiObjectNode);
            formObjects.setNode(fieldPath, multiObjectNode);
          }
        }
      }
    }
  }

  private void createIntermediateObjectsForDefaultValues(FormObjects formObjects, FieldPath fieldPath, Value[] defaultValues, MultiObjectNode multiObjectNode) {
    for (Value defaultValue : defaultValues) {
      DomainObject linkedObjectDefault = crudService.createDomainObject(fieldPath.getReferenceType());
      DomainObject defaultReferencedObject = crudService.find(((ReferenceValue) defaultValue).get());
      linkedObjectDefault.setValue(fieldPath.getLinkToParentName(), new ReferenceValue(formObjects.getRootDomainObject().getId()));
      linkedObjectDefault.setValue(fieldPath.getReferenceName(), new ReferenceValue(defaultReferencedObject.getId()));
      multiObjectNode.add(linkedObjectDefault);
    }
  }

  private FormMappingConfig findFormViewerMappingConfig(DomainObject root, FormViewerConfig formViewerConfig) {
    if (formViewerConfig != null && formViewerConfig.getFormMappingConfigList() != null) {
      for (FormMappingConfig mappingConfig : formViewerConfig.getFormMappingConfigList()) {
        if (root.getTypeName().equals(mappingConfig.getDomainObjectType())) {
          return mappingConfig;
        }
      }
    }
    return null;
  }

  private FormConfig loadFormConfig(DomainObject root, FormViewerConfig formViewerConfig, FormPluginConfig formPluginConfig) {
    FormConfig formConfig = null;

    formConfig = findLinkedForm(formPluginConfig, formViewerConfig, root.getTypeName());
    if (formConfig != null) {
      return formConfig;
    }

    if (formViewerConfig != null && formViewerConfig.getFormMappingComponent() != null) {
      FormMappingHandler formMappingHandler = (FormMappingHandler) applicationContext.getBean(formViewerConfig.getFormMappingComponent());
      if (formMappingHandler != null) {
        formConfig = formMappingHandler.findEditingFormConfig(root, getUserUid());
      }
    } else if (formViewerConfig != null && formViewerConfig.getFormMappingConfigList() != null) {
      for (FormMappingConfig mappingConfig : formViewerConfig.getFormMappingConfigList()) {
        if (root.getTypeName().equals(mappingConfig.getDomainObjectType())) {
          String formName = mappingConfig.getForm();
          formConfig = getLocalizedFormConfig(formName);
        }
      }
    }
    if (formConfig == null) {
      formConfig = formResolver.findEditingFormConfig(root, getUserUid());
    }
    return formConfig;
  }

  private FormConfig findLinkedForm(FormPluginConfig formPluginConfig, FormViewerConfig formViewerConfig, final String domainObjectType) {

    if(formPluginConfig != null) {
      final String linkedFormName = formPluginConfig.getLinkedFormName();
      if (linkedFormName != null) {
        return getLocalizedFormConfig(linkedFormName);
      }
    }

    if (formViewerConfig != null && formViewerConfig instanceof LinkedFormViewerConfig) {
      LinkedFormViewerConfig linkedFormMappingConfig = (LinkedFormViewerConfig) formViewerConfig;
      List<LinkedFormConfig> linkedFormConfigs = linkedFormMappingConfig.getLinkedFormConfigs();
      LinkedFormConfig result = findLinkedFormConfig(linkedFormConfigs, domainObjectType);
      if (result != null) {
        return getLocalizedFormConfig(result.getName());
      }
    }
    return null;
  }

  private LinkedFormConfig findLinkedFormConfig(List<LinkedFormConfig> linkedFormConfigs, final String domainObjectType) {
    if (WidgetUtil.isNotEmpty(linkedFormConfigs) && linkedFormConfigs.size() == 1) {
      return linkedFormConfigs.get(0);
    }
    LinkedFormConfig result = (LinkedFormConfig) CollectionUtils.find(linkedFormConfigs, new Predicate() {
      @Override
      public boolean evaluate(Object input) {
        LinkedFormConfig formConfig = (LinkedFormConfig) input;
        return domainObjectType.equalsIgnoreCase(formConfig.getDomainObjectType());
      }
    });
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
      //fieldName = fieldPath.getReferenceName();
      //doTypeName = fieldPath.getLinkedObjectType();
      return constraints; /* back-reference -> ignore constraints */
    } else /* ManyToMany */ {
      //fieldName = fieldPath.getReferenceName();
      //doTypeName = fieldPath.getLinkingObjectType();
      return constraints; /* back-reference -> ignore constraints */
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

  private ObjectsNode findLinkedNode(SingleObjectNode parentNode, FieldPath childPath, ExactTypesConfig exactTypesConfig) {
    if (childPath.isOneToOneDirectReference()) { // direct reference
      return findOneToOneDirectReferenceLinkedNode(parentNode, childPath);
    } else { // back reference
      return findBackReferenceLinkedNode(parentNode, childPath, exactTypesConfig);
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

  private ObjectsNode findBackReferenceLinkedNode(SingleObjectNode parentNode, FieldPath childPath, ExactTypesConfig exactTypesConfig) {
    final String linkedType = childPath.getReferenceType();
    final boolean oneToOneBackReference = childPath.isOneToOneBackReference();
    if (parentNode.isEmpty()) {
      return oneToOneBackReference ? new SingleObjectNode(linkedType) : new MultiObjectNode(linkedType);
    }
    String referenceField = childPath.getLinkToParentName();
    DomainObject parentDomainObject = parentNode.getDomainObject();
    // it's either a list of intermediate objects (linked objects ids are taken from it) in case of N:M or a list of
    // linked objects themselves in case of 1:N (it can be optimized later)
    // todo: exact types support
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

  private HashMap<String, Object> buildWidgetProps(List<Constraint> constraints, String formType) {
    HashMap<String, Object> props = new HashMap<String, Object>();
    String domainObjectKey = MessageResourceProvider.DOMAIN_OBJECT;
    String fieldKey = MessageResourceProvider.FIELD;
    if (FormConfig.TYPE_SEARCH.equals(formType)) {
      domainObjectKey = MessageResourceProvider.SEARCH_DOMAIN_OBJECT;
      fieldKey = MessageResourceProvider.SEARCH_FIELD;
    }
    for (Constraint constraint : constraints) {
      Map<String, String> params = constraint.getParams();
      //localize domain object name:
      String domainObjectName = params.get(Constraint.PARAM_DOMAIN_OBJECT_TYPE);
      String localizedDomainObjectName = MessageResourceProvider.getMessage(new MessageKey(domainObjectName,
          domainObjectKey), GuiContext.getUserLocale());
      //localize field name:
      String fieldName = params.get(Constraint.PARAM_FIELD_NAME);
      String localizedFieldName = MessageResourceProvider.getMessage(new MessageKey(fieldName, fieldKey, params),
          GuiContext.getUserLocale());

      params.put(Constraint.PARAM_DOMAIN_OBJECT_TYPE, localizedDomainObjectName);
      params.put(Constraint.PARAM_FIELD_NAME, localizedFieldName);
      props.putAll(params);
    }
    return props;
  }

  private FormConfig getLocalizedFormConfig(String formName) {
    return configurationExplorer.getLocalizedPlainFormConfig(formName, GuiContext.getUserLocale());
  }

  private String buildMessage(String message, String defaultValue) {
    return MessageResourceProvider.getMessage(message,  GuiContext.getUserLocale(),defaultValue);
  }

  private String buildMessage(String message, String defaultValue, Pair<String, String>... params) {
    Map<String, String> paramsMap = new HashMap<>();
    for (Pair<String, String> pair : params) {
      paramsMap.put(pair.getFirst(), pair.getSecond());
    }
    return PlaceholderResolver.substitute(buildMessage(message, defaultValue), paramsMap);
  }

  private void fillSubscriptionAndRules(WidgetState state, WidgetContext context){
    if(context.getWidgetConfig().getEventsTypeConfig()!=null){
      for( SubscribedTypeConfig sType : context.getWidgetConfig().
          getEventsTypeConfig().getSubscriberTypeConfig().getSubscribedTypeConfigs()){
        state.getSubscription().add(sType.getToId());
      }
    }
    if(context.getWidgetConfig().getRulesTypeConfig()!=null){
      state.setRules(context.getWidgetConfig().getRulesTypeConfig());
    }
  }
}
