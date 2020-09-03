package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 15:28
 */
@ComponentName("form.plugin")
public class FormPlugin extends Plugin implements IsActive, IsDomainObjectEditor, PluginPanelSizeChangedEventHandler {

    // поле для локальной шины событий
    protected EventBus eventBus;
    private FormPluginState initialVisualState;
    private FormState initialState;

    // установка локальной шины событий плагину
    public void setLocalEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    // получение локальной шины событий плагина
    @Override
    public EventBus getLocalEventBus() {
        return eventBus;
    }

    public FormPlugin() {
        showBreadcrumbs = false;
    }

    @Override
    public void setInitialData(PluginData initialData) {
        super.setInitialData(initialData);
        initialVisualState = (FormPluginState) initialData.getPluginState().createClone();
    }

    public FormPluginState getInitialVisualState() {
        return initialVisualState;
    }

    @Override
    public PluginView createView() {
        FormPluginData initialData = getInitialData();
        return new FormPluginView(this, initialData.getFormDisplayData());
    }

    @Override
    public FormPlugin createNew() {
        return new FormPlugin();
    }

    @Override
    public FormState getCurrentState() {
        return getFormState();
    }

    @Override
    protected void afterInitialDataChange(PluginData oldData, PluginData newData) {
        super.afterInitialDataChange(oldData, newData);
        ((FormPluginView) getView()).update(((FormPluginData) newData).getFormDisplayData().getFormState());
    }

    public DomainObject getRootDomainObject() {
        return this.<FormPluginData>getInitialData().getFormDisplayData().getFormState().getObjects().getRootNode().getDomainObject();
    }

    @Override
    public void replaceForm(FormPluginConfig formPluginConfig) {
        final FormPlugin newPlugin = ComponentRegistry.instance.get("form.plugin");
        newPlugin.setConfig(formPluginConfig);
        getOwner().open(newPlugin);
        newPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
                                             @Override
                                             public void onViewCreation(PluginViewCreatedEvent source) {
                                                 eventBus.fireEvent(new PluginPanelSizeChangedEvent());
                                             }
                                         }
        );
    }

    @Override
    public FormState getFormState() {
        return getFormState(null, false);
    }

    public FormState getFormState(IWidgetStateFilter widgetStateFilter, boolean deepClone) {
        FormState initialFormState = this.<FormPluginData>getInitialData().getFormDisplayData().getFormState();
        FormPluginView view = (FormPluginView) getView();
        Map<String, WidgetState> widgetsState = view.getWidgetsState(widgetStateFilter, deepClone);
        FormState result = new FormState(initialFormState.getName(), widgetsState, initialFormState.getObjects(), initialFormState.getWidgetComponents(),
                initialFormState.getMessages(), initialFormState.getFormViewerConfig());
        return result;
    }

    @Override
    public void setFormState(FormState formState) {
        ((FormPluginView) getView()).update(formState);
        FormPluginData initialData = getInitialData();
        initialData.getFormDisplayData().setFormState(formState);
    }

    @Override
    public void setFormToolbarContext(final ToolbarContext toolbarContext) {
        setToolbarContext(toolbarContext);
    }

    @Override
    public FormPluginState getFormPluginState() {
        return getPluginState();
    }

    @Override
    public FormPluginState getPluginState() {
        return initialVisualState;
    }

    @Override
    public void setPluginState(PluginState pluginState) {
        this.initialVisualState = (FormPluginState) pluginState;
    }

    @Override
    public void updateSizes() {
        getView().onPluginPanelResize();
    }

    @Override
    public boolean restoreHistory() {
        final FormPluginView view = (FormPluginView) getView();
        view.updateViewFromHistory();
        return false;
    }

    @Override
    public void refresh() {
        AsyncCallback<Dto> callback = new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                setInitialData((PluginData) result);
            }

            @Override
            public void onFailure(Throwable caught) {
                onDataLoadFailure(caught);
            }
        };
        final Command command = new Command("initialize", this.getName(), getConfig());
        BusinessUniverseServiceAsync.Impl.executeCommand(command, callback, true, indicateLockScreen);
    }

    @Override
    public boolean isDirty() {
        final FormPluginView view = (FormPluginView) getView();
        return view == null ? false : view.isDirty();
    }

    @Override
    public FormViewerConfig getFormViewerConfig() {
        return ((FormPluginConfig) this.getConfig()).getFormViewerConfig();
    }
}
