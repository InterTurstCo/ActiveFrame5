package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterInnerScrollEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterInnerScrollEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.LabelWidget;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 15:29
 */
public class FormPluginView extends PluginView {

    private FormPanel  formPanel;
    // локальная шина событий
    protected EventBus eventBus;
    private FlowPanel flowPanel;

    // установка локальной шины событий плагину
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    protected FormPluginView(FormPlugin plugin, FormDisplayData formDisplayData) {
        super(plugin);
        // установка локальной шины событий
        this.eventBus = plugin.getEventBus();
        int formWidth = plugin.getOwner().getVisibleWidth();
        int formHeight = plugin.getOwner().getVisibleHeight();
        formPanel = new FormPanel(formDisplayData, formWidth, formHeight);
        flowPanel = formPanel.getPanel();
        // добавляем обработчики
        addHandlers();
    }

    @Override
    public IsWidget getViewWidget() {
        return formPanel;
    }

    public void addHandlers() {

        eventBus.addHandler(SplitterInnerScrollEvent.TYPE, new SplitterInnerScrollEventHandler() {
            @Override
            public void setScrollPanelHeight(SplitterInnerScrollEvent event) {

                flowPanel.setHeight(event.getDownPanelHeight() + "px");
                flowPanel.setWidth(flowPanel.getParent().getParent().getOffsetWidth() + "px");

            }
        });

        eventBus.addHandler(SplitterWidgetResizerEvent.TYPE, new SplitterWidgetResizerEventHandler() {

            @Override
            public void setWidgetSize(SplitterWidgetResizerEvent event) {
                if (event.isType()){
                    if ((event.getFirstWidgetHeight() * 2) < Window.getClientHeight()) {
                        flowPanel.setHeight(((event.getFirstWidgetHeight()*2) ) + "px");
                    }  else {
                        flowPanel.setHeight((event.getFirstWidgetHeight()) + "px");
                    }
                }
                else
                {
                   flowPanel.setHeight((event.getFirstWidgetHeight() ) + "px");
                }

                flowPanel.setWidth(event.getFirstWidgetWidth()+"px");


            }
        });
    }

    public Map<String, WidgetState> getWidgetsState() {
        List<BaseWidget> widgets = formPanel.getWidgets();
        HashMap<String, WidgetState> result = new HashMap<String, WidgetState>(widgets.size());
        for (BaseWidget widget : widgets) {
            if (widget instanceof LabelWidget) {
                continue;
            }
            String id = widget.getDisplayConfig().getId();
            if (widget.isEditable()) {
                try {
                    WidgetState state = widget.getCurrentState();
                    result.put(id, state);
                } catch (GuiException e) {
                    Window.alert(e.getMessage()); // todo something more interesting
                }
            }
        }
        return result;
    }

    void update(FormState formState) {
        formPanel.update(formState);
    }



}
