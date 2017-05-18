package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.ListBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.ListCellState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * Created by Ravil on 18.05.2017.
 */
@ComponentName("list-cell")
public class ListCellWidget extends BaseWidget {

    private ListCellState currentState;

    @Override
    public Component createNew() {
        return new ListCellWidget();
    }

    @Override
    public void setCurrentState(WidgetState currentState) {}

    @Override
    protected boolean isChanged() {
        return false;
    }

    @Override
    protected WidgetState createNewState() {
        return new ListBoxState();
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        return asNonEditableWidget(state);
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        currentState = (ListCellState) state;
        FlowPanel rootPanel = new FlowPanel();
        FlowPanel buttonPanel = new FlowPanel();
        final FlowPanel listPanel = new FlowPanel();
        buttonPanel.add(new Label(currentState.getHeaderValue()+((currentState.getCounterRequired())?currentState.getItems().size():"")));

        final ToggleButton openCloseBtn = new ToggleButton(">","<");
        openCloseBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                Window.alert("Нажал таки...");
                if(openCloseBtn.isDown()){
                    listPanel.setVisible(true);
                } else {
                    listPanel.setVisible(false);
                }
            }
        });


        buttonPanel.add(openCloseBtn);
        rootPanel.add(buttonPanel);
        rootPanel.add(listPanel);
        return rootPanel;
    }
}
