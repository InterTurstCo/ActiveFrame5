package ru.intertrust.cm.core.gui.impl.client;

import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.*;

import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 15:28
 */
@ComponentName("form.plugin")
public class FormPlugin extends Plugin implements IsActive, IsDomainObjectEditor, PluginPanelSizeChangedEventHandler {
    private int temporaryWidth;
    private int temporaryHeight;
    // поле для локальной шины событий
    protected EventBus eventBus;

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
    }

    public int getTemporaryWidth() {
        return temporaryWidth;
    }

    public void setTemporaryWidth(int temporaryWidth) {
        this.temporaryWidth = temporaryWidth;
    }

    public int getTemporaryHeight() {
        return temporaryHeight;
    }

    public void setTemporaryHeight(int temporaryHeight) {
        this.temporaryHeight = temporaryHeight;
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
        return getFormState(null);
    }

    public FormState getFormState(IWidgetStateFilter widgetStateFilter) {
        FormState initialFormState = this.<FormPluginData>getInitialData().getFormDisplayData().getFormState();
        FormPluginView view = (FormPluginView) getView();
        Map<String, WidgetState> widgetsState = view.getWidgetsState(widgetStateFilter);

        return new FormState(initialFormState.getName(), widgetsState, initialFormState.getObjects(), initialFormState.getWidgetComponents(),
                initialFormState.getMessages());
    }

    @Override
    public void setFormState(FormState formState) {
        ((FormPluginView) getView()).update(formState);
        FormPluginData initialData = getInitialData();
        initialData.getFormDisplayData().setFormState(formState);
    }

    @Override
    public FormPluginState getFormPluginState() {
        return getPluginState();
    }

    @Override
    public FormPluginState getPluginState() {
        final FormPluginData data = getInitialData();
        return (FormPluginState) data.getPluginState().createClone();
    }

    @Override
    public void setPluginState(PluginState pluginState) {
        final FormPluginData data = getInitialData();
        data.setPluginState(pluginState);
    }

    @Override
    public void updateSizes() {
        getView().onPluginPanelResize();

    }
}
