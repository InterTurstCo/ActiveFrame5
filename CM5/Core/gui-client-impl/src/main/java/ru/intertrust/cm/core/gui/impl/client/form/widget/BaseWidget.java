package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 14:40
 */
public abstract class BaseWidget extends BaseComponent implements IsWidget {
    protected WidgetState initialData;
    protected WidgetDisplayConfig displayConfig;
    protected boolean isEditable = true;
    protected EventBus eventBus;
    protected Widget impl;

    public <T extends WidgetState> T getInitialData() {
        return (T) initialData;
    }

    public void setInitialData(WidgetState initialData) {
        this.initialData = initialData;
    }

    public WidgetDisplayConfig getDisplayConfig() {
        return displayConfig;
    }

    public void setDisplayConfig(WidgetDisplayConfig displayConfig) {
        this.displayConfig = displayConfig;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public Widget asWidget() {
        return impl;
    }

    public void setState(WidgetState state) {
        if (impl == null) {
            impl = isEditable ? asEditableWidget(state) : asNonEditableWidget(state);
            applySizeTo(impl);
        }
        setCurrentState(state);
        this.initialData = state;
    }

    public abstract void setCurrentState(WidgetState currentState);

    // todo: setNonEditableState, getNonEditableState

    /**
     * Возвращает текущее состояние виджета. Если виджет в режиме "только чтение", возвращает null
     * @return текущее состояние виджета или null, если виджет в режиме "только чтение"
     */
    public abstract WidgetState getCurrentState();

    protected abstract Widget asEditableWidget(WidgetState state);

    protected abstract Widget asNonEditableWidget(WidgetState state);

    protected void applySizeTo(Widget widget) {
        String width = displayConfig.getWidth();
        String height = displayConfig.getHeight();
        if (width != null && !width.isEmpty()) {
            widget.setWidth(width);
        }
        if (height != null && !height.isEmpty()) {
            widget.setHeight(height);
        }
    }

    protected static String getTrimmedText(HasText widget) {
        String text = widget.getText();
        if (text == null) {
            return null;
        }
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            return null;
        }
        return trimmedText;
    }

    protected static void setTrimmedText(HasText widget, String text) {
        widget.setText(text == null ? "" : text.trim());
    }
}
