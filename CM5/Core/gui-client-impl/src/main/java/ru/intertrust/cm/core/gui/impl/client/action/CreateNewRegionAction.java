package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.DefaultValueConfig;
import ru.intertrust.cm.core.config.gui.form.FormMappingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldValueConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

import java.util.ArrayList;
import java.util.List;

@ComponentName("create.new.region.action")
public class CreateNewRegionAction extends SimpleServerAction {
    private static final String OBJECT_TYPE_PROP = "create.object.types";
    private static final String OBJECT_FORM_PROP = "create.object.form";
    public static final String TYPE_ADDITIONS = "typeAdditions";
    public static final String LEVEL = "level";
    public static final String REGION_TYPE = "Region_Type";
    public static final String SUBORDINATION = "Subordination";
    public static final String REGION_TYPE_ONE = "1";
    public static final String REGION_TYPE_TWO = "2";
    public static final String REGION_TYPE_THREE = "3";
    public static final String REGION_TYPE_FOUR = "4";

    @Override
    protected ActionContext appendCurrentContext(ActionContext context) {
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        FormState formState = editor.getFormState();
        context.setRootObjectId(formState.getObjects().getRootNode().getDomainObject().getId());

        return context;
    }


    @Override
    protected void execute() {
        final ActionConfig actionConfig = getInitialContext().getActionConfig();
        String typeAdditions = actionConfig.getProperty(TYPE_ADDITIONS);
        String level = actionConfig.getProperty(LEVEL);
        IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        final String domainObjectTypeToCreate = editor.getRootDomainObject().getTypeName();
        DomainObject domainObject = editor.getRootDomainObject();

        Id parentId;
        try {
            parentId = new RdbmsId(((DomainObjectSurferConfig)((DomainObjectSurferPlugin)editor).getConfig()).getCollectionViewerConfig().getHierarchicalFiltersConfig().getFilterConfigs().get(0).getParamConfigs().get(0).getValue());
        } catch (Exception e){
            parentId = null;
        }

        LongValue regionType;
        Id subbordination;

        Id currentId = domainObject.getId();
        if(currentId != null){
            regionType = domainObject.getValue(REGION_TYPE);
            subbordination = domainObject.getReference(SUBORDINATION);
        } else {
            regionType = null;
            subbordination = null;
        }

        final FormPluginConfig formPluginConfig = new FormPluginConfig(domainObjectTypeToCreate);
        formPluginConfig.setDomainObjectTypeToCreate(domainObjectTypeToCreate);
        final FormPluginState state = editor.getFormPluginState().createClone();
        formPluginConfig.setPluginState(state);

        final FormViewerConfig viewerConfig = new FormViewerConfig();
        final FormMappingConfig formMappingConfig = new FormMappingConfig();
        formMappingConfig.setDomainObjectType(domainObjectTypeToCreate);
        formMappingConfig.setForm(actionConfig.getProperty(OBJECT_FORM_PROP));

        final List<FormMappingConfig> formMappingConfigList = new ArrayList<>();
        final FormMappingConfig newFormMappingConfig = new FormMappingConfig();
        newFormMappingConfig.setDomainObjectType(domainObjectTypeToCreate);
        newFormMappingConfig.setForm(actionConfig.getProperty(OBJECT_FORM_PROP));

        final List<FieldPathConfig> fieldPathConfigList = new ArrayList<>();

        if("group".equals(typeAdditions)){
            if(parentId == null){
                fieldPathConfigList.add(BuildFieldPathConfig(REGION_TYPE, REGION_TYPE_ONE));
            } else {
                String nextRegionType;
                if (level.equals(REGION_TYPE_ONE)) {
                    nextRegionType = REGION_TYPE_ONE;

                } else if (level.equals(REGION_TYPE_TWO)) {
                    nextRegionType = REGION_TYPE_TWO;

                } else if (level.equals(REGION_TYPE_THREE)) {
                    nextRegionType = REGION_TYPE_FOUR;

                } else {
                    nextRegionType = REGION_TYPE_ONE;

                }
                fieldPathConfigList.add(BuildFieldPathConfig(SUBORDINATION, parentId.toStringRepresentation()));
                fieldPathConfigList.add(BuildFieldPathConfig(REGION_TYPE, nextRegionType));
            }
        }

        if("item".equals(typeAdditions)){
            if (currentId == null){
                ApplicationWindow.errorAlert("Не выбран элемент");
                return;
            } else {
                if (regionType.get() == 1L) {
                    fieldPathConfigList.add(BuildFieldPathConfig(REGION_TYPE, REGION_TYPE_TWO));
                    fieldPathConfigList.add(BuildFieldPathConfig(SUBORDINATION, currentId.toStringRepresentation()));
                }
                if ((regionType.get() == 2L) || (regionType.get() == 3L)) {
                    fieldPathConfigList.add(BuildFieldPathConfig(REGION_TYPE, REGION_TYPE_FOUR));
                    fieldPathConfigList.add(BuildFieldPathConfig(SUBORDINATION, currentId.toStringRepresentation()));
                }
                if ((regionType.get() == 4L) && (subbordination != null)){
                    fieldPathConfigList.add(BuildFieldPathConfig(REGION_TYPE, REGION_TYPE_FOUR));
                    fieldPathConfigList.add(BuildFieldPathConfig(SUBORDINATION, subbordination.toStringRepresentation()));
                }
            }
        }

        newFormMappingConfig.setFieldPathConfigs(fieldPathConfigList);

        formMappingConfigList.add(newFormMappingConfig);
        viewerConfig.setFormMappingConfigList(formMappingConfigList);
        formPluginConfig.setFormViewerConfig(viewerConfig);

        state.setEditable(true);
        final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
        formPlugin.setConfig(formPluginConfig);
        formPlugin.setDisplayActionToolBar(true);
        formPlugin.setLocalEventBus(plugin.getLocalEventBus());
        getPlugin().getOwner().openChild(formPlugin);
    }

    private FieldPathConfig BuildFieldPathConfig(String fieldPath, String value)    {
        final FieldPathConfig fieldPathConfig = new FieldPathConfig();
        fieldPathConfig.setValue(fieldPath);
        final DefaultValueConfig defaultValueConfig = new DefaultValueConfig();
        final FieldValueConfig fieldValueConfig = new FieldValueConfig();
        fieldValueConfig.setName(fieldPath);
        fieldValueConfig.setValue(value);
        defaultValueConfig.setFieldValueConfig(fieldValueConfig);
        fieldPathConfig.setDefaultValueConfig(defaultValueConfig);
        return fieldPathConfig;
    }

    @Override
    public Component createNew() {
        return new CreateNewRegionAction() ;
    }
}