package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.FormPluginView;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

import java.util.List;
import java.util.Set;

/**
 * @author Sergey.Okolot
 */
@ComponentName("toggle.edit.on.action")
public class ToggleEditOnAction extends ToggleAction {

    @Override
    protected void execute() {
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        final Id id = editor.getFormState().getObjects().getRootNode().getDomainObject().getId();
        final FormPluginConfig config;
        if (id == null) {
            config = new FormPluginConfig(editor.getRootDomainObject().getTypeName());
        } else {
            config = new FormPluginConfig(id);
        }
        final FormPluginState state = editor.getFormPluginState().createClone();
        config.setPluginState(state);
        state.setEditable(true);
        config.setFormViewerConfig(editor.getFormViewerConfig());
        final FormPlugin formPlugin = createFormPlugin(config);
        final boolean isCentral = state.isInCentralPanel();
        if (!isCentral) {
            state.setInCentralPanel(true);
        }
        PluginViewCreatedEventListener listener = new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                if (isCentral) {
                    getPlugin().getOwner().closeCurrentPlugin();
                }
                focusFirstEditableWidget(source);
            }
        };
        formPlugin.addViewCreatedListener(listener);
        Application.getInstance().getEventBus().fireEvent(new CentralPluginChildOpeningRequestedEvent(formPlugin));

    }

    @Override
    public Component createNew() {
        return new ToggleEditOnAction();
    }

    private FormPlugin createFormPlugin(final FormPluginConfig config) {
        final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
        formPlugin.setConfig(config);
        formPlugin.setDisplayActionToolBar(true);
        formPlugin.setLocalEventBus(plugin.getLocalEventBus());
        return formPlugin;
    }

    /**
     * Устанавливает фокус на первый редактируемый виджет, если это возможно.
     *
     * @param source объект события создания представления плагина
     */
    private void focusFirstEditableWidget(PluginViewCreatedEvent source) {
        FormPlugin sourceFormPlugin = (FormPlugin) source.getPlugin();
        FormPluginView formPluginView = (FormPluginView) sourceFormPlugin.getView();

        final FormPanel formPanel = (FormPanel) formPluginView.getViewWidget();
        focusFirstEditableWidget(formPanel);
    }

    /**
     * Устанавливает фокус на первый редактируемый виджет, если это возможно.<br>
     * Порядок установки фокуса такой (всегда берется первый подходящий из целевой области выборки):<br>
     * <ol>
     * <li>первый (самый верхний), если вкладки отсутствуют на форме</li>
     * <li>первый в шапке (над вкладками), если она существует</li>
     * <li>первый на выбранной вкладке, если они (вкладки) есть</li>
     * </ol>
     *
     * @param formPanel объект формы
     */
    private void focusFirstEditableWidget(final FormPanel formPanel) {
        final List<BaseWidget> allWidgetsList = formPanel.getWidgets();
        final int allWidgetsCount = (allWidgetsList != null) ? allWidgetsList.size() : 0;

        if (allWidgetsCount > 0) {
            final Set<String> allTabsWidgetIds = formPanel.getAllTabsWidgetIds();
            final int allTabsWidgetsCount = allTabsWidgetIds.size();

            // вариант, когда вкладки вовсе отсутствуют, просто выбираем первый подходящий виджет
            if (allTabsWidgetsCount == 0) {
                final boolean firstCaseFocusResult = focusFirstWidget(allWidgetsList);
                if (firstCaseFocusResult) {
                    return;
                }
                // если есть виджеты где-то, кроме вкладок (в панели хедера над панелью вкладок)
            } else if (allWidgetsCount > allTabsWidgetsCount) {
                final boolean secondCaseFocusResult = focusFirstNotTabWidget(allWidgetsList, allTabsWidgetIds);
                if (secondCaseFocusResult) {
                    return;
                }
            }
            // если прошлые блоки не сработали, то выбираем первый подходящий виджет на выбранной вкладке
            if (allTabsWidgetsCount > 0) {
                final Set<String> selectedTabWidgetIds = formPanel.getSelectedTabWidgetIds();
                focusFirstSelectedTabWidget(allWidgetsList, selectedTabWidgetIds);
            }
        }
    }

    /**
     * Устанавливает фокус на первый редактируемый виджет из списка, если это возможно.
     *
     * @param allWidgetsList список всех виджетов
     * @return true - фокус был установлен<br>
     * false - не был
     */
    private boolean focusFirstWidget(List<BaseWidget> allWidgetsList) {
        for (BaseWidget widget : allWidgetsList) {
            final boolean isFocused = GuiUtil.focusEditableWidget(widget);
            if (isFocused) {
                return true;
            }
        }
        return false;
    }

    /**
     * Устанавливает фокус на первый редактируемый виджет, который расположен не на вкладках, если подобные имеются.
     *
     * @param allWidgetsList   список всех виджетов
     * @param allTabsWidgetIds набор идентификаторов виджетов, расположенных на всех вкладках
     * @return true - фокус был установлен<br>
     * false - не был
     */
    private boolean focusFirstNotTabWidget(List<BaseWidget> allWidgetsList, Set<String> allTabsWidgetIds) {
        for (BaseWidget widget : allWidgetsList) {
            final WidgetDisplayConfig widgetDisplayConfig = widget.getDisplayConfig();
            final String widgetId = widgetDisplayConfig.getId();

            // берем только виджеты, которые не содержатся во вкладках
            if (!allTabsWidgetIds.contains(widgetId)) {
                final boolean isFocused = GuiUtil.focusEditableWidget(widget);
                if (isFocused) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Устанавливает фокус на первый редактируемый виджет, который расположен не на вкладках, если подобные имеются.
     *
     * @param allWidgetsList       список всех виджетов
     * @param selectedTabWidgetIds набор идентификаторов виджетов, расположенных на активной вкладке
     */
    private void focusFirstSelectedTabWidget(List<BaseWidget> allWidgetsList, Set<String> selectedTabWidgetIds) {
        if (selectedTabWidgetIds.size() > 0) {
            for (BaseWidget widget : allWidgetsList) {

                final WidgetDisplayConfig widgetDisplayConfig = widget.getDisplayConfig();
                final String widgetId = widgetDisplayConfig.getId();

                if (selectedTabWidgetIds.contains(widgetId)) {
                    final boolean isFocused = GuiUtil.focusEditableWidget(widget);
                    if (isFocused) {
                        return;
                    }
                }
            }
        }
    }

}
