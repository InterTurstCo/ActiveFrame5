package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 14:40
 */
public abstract class BaseWidget extends BaseComponent implements IsWidget {
    protected WidgetData initialData;
    protected WidgetDisplayConfig displayConfig;
    protected boolean isEditable = true;

    protected Widget impl;

    public <T extends WidgetData> T getInitialData() {
        return (T) initialData;
    }

    public void setInitialData(WidgetData initialData) {
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

    @Override
    public Widget asWidget() {
        return impl;
    }

    public void setState(WidgetData state) {
        if (impl == null) {
            impl = isEditable ? asEditableWidget() : asNonEditableWidget();
            applySizeTo(impl);
        }
        setCurrentState(state);
        this.initialData = state;
    }

    public abstract void setCurrentState(WidgetData state);

    /**
     * Возвращает текущее состояние виджета. Если виджет в режиме "только чтение", возвращает null
     * @return текущее состояние виджета или null, если виджет в режиме "только чтение"
     */
    public abstract WidgetData getCurrentState();

    protected abstract Widget asEditableWidget();

    protected abstract Widget asNonEditableWidget();

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
