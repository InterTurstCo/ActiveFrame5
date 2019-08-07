package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.FormMappingConfig;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.FormPluginView;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;
import ru.intertrust.cm.core.gui.model.util.StringUtil;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 * Date: 23.09.13
 * Time: 20:03
 */
@ComponentName("create.new.object.action")
public class CreateNewObjectAction extends Action {

    private static final String OBJECT_TYPE_PROP = "create.object.type";
    private static final String OBJECT_FORM_PROP = "create.object.form";

    @Override
    protected void execute() {
        final ActionConfig actionConfig = getInitialContext().getActionConfig();
        IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        final String domainObjectTypeToCreate;
        if (actionConfig.getProperty(OBJECT_TYPE_PROP) == null) {
            domainObjectTypeToCreate = editor.getRootDomainObject().getTypeName();
        } else {
            domainObjectTypeToCreate = actionConfig.getProperty(OBJECT_TYPE_PROP);
        }
        final FormPluginConfig formPluginConfig = new FormPluginConfig(domainObjectTypeToCreate);
        formPluginConfig.setDomainObjectTypeToCreate(domainObjectTypeToCreate);
        final FormPluginState state = editor.getFormPluginState().createClone();
        formPluginConfig.setPluginState(state);

        if (actionConfig.getProperty(OBJECT_TYPE_PROP) == null || actionConfig.getProperty(OBJECT_FORM_PROP) != null) {
            final FormViewerConfig viewerConfig = new FormViewerConfig();
            final FormMappingConfig formMappingConfig = new FormMappingConfig();
            formMappingConfig.setDomainObjectType(domainObjectTypeToCreate);
            formMappingConfig.setForm(actionConfig.getProperty(OBJECT_FORM_PROP));
            final List<FormMappingConfig> formMappingConfigList = new ArrayList<>();
            if (actionConfig.getProperty(OBJECT_TYPE_PROP) != null) {
                formMappingConfigList.add(formMappingConfig);
            }
            viewerConfig.setFormMappingConfigList(formMappingConfigList);
            formPluginConfig.setFormViewerConfig(viewerConfig);
        } else {
            formPluginConfig.setFormViewerConfig(editor.getFormViewerConfig());
        }

        //CMFIVE-4330
        /**
         * Это просто временное решение т.к. прямой возможности передать Id в DefailtValueSetter отсюда
         * пока нет.
         */
        if (actionConfig.getText().equals("collection-row-button-action") && formPluginConfig.getParentId() == null) {
            formPluginConfig.setParentId(getInitialContext().getRootObjectId());
        }

        if (state.isToggleEdit()) {
            state.setEditable(true);
            final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
            formPlugin.setConfig(formPluginConfig);
            formPlugin.setDisplayActionToolBar(true);
            formPlugin.setLocalEventBus(plugin.getLocalEventBus());
            state.setInCentralPanel(true); //CMFIVE-2252

            setLastSelectedHierarchyCollectionRow(editor, formPluginConfig);

            addPluginViewCreatedEventListener(formPlugin);
            getPlugin().getOwner().openChild(formPlugin);
        } else {
            editor.replaceForm(formPluginConfig);
        }

    }

    /**
     * Добавляет слушатель события создания представления, который выделит первую вкладку на форме, если они имеются.
     *
     * @param formPlugin объект плагина формы
     */
    private void addPluginViewCreatedEventListener(FormPlugin formPlugin) {
        PluginViewCreatedEventListener listener = new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                selectFirstTab(source);
            }
        };
        formPlugin.addViewCreatedListener(listener);
    }

    /**
     * Программно выделяет первую вкладку на панели формы, которая открывается после срабатывания экшена создания нового объекта
     *
     * @param source объект события создания представления ({@link ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent})
     */
    private void selectFirstTab(PluginViewCreatedEvent source) {
        FormPlugin sourceFormPlugin = (FormPlugin) source.getPlugin();
        FormPluginView formPluginView = (FormPluginView) sourceFormPlugin.getView();

        final FormPanel formPanel = (FormPanel) formPluginView.getViewWidget();
        formPanel.selectFirstTab();
    }

    /**
     * Устанавливает, если это возможно Id последнего выбранного элемента в иерархической коллекции в объект конфигурации плагина для последующей передачи в объект состояния формы.
     *
     * @param editor           объект плагина {@link DomainObjectSurferPlugin}
     * @param formPluginConfig объект конфигурации плагина формы
     */
    private void setLastSelectedHierarchyCollectionRow(IsDomainObjectEditor editor, FormPluginConfig formPluginConfig) {
        if (editor instanceof DomainObjectSurferPlugin) {

            final PluginConfig dosPluginConfig = ((DomainObjectSurferPlugin) editor).getConfig();
            final Object linkHistoryObject = dosPluginConfig.getHistoryValue(UserSettingsHelper.LINK_KEY);

            if (linkHistoryObject != null) {
                String linkHistory = (String) linkHistoryObject;

                if (!linkHistory.isEmpty()) {
                    final String lastIdStr = StringUtil.getLastIdStrFromHistoryLink(linkHistory);
                    if (lastIdStr != null) {

                        Id lastSelectedId = new RdbmsId(lastIdStr);
                        formPluginConfig.setLastCollectionRowSelectedId(lastSelectedId);
                    }
                }
            }
        }
    }

    @Override
    public CreateNewObjectAction createNew() {
        return new CreateNewObjectAction();
    }

}
