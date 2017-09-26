package ru.intertrust.cm.core.gui.impl.client.form.widget.editabletablebrowser;

import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.DefaultConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.SelectConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.EditableTableBrowserState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * Created by Ravil on 26.09.2017.
 */
@ComponentName("editable-table-browser")
public class EditableTableBrowserWidget extends BaseWidget {

    private HandlerRegistration checkBoxRegistration;
    private HandlerRegistration rowSelectedRegistration;
    private CollectionPlugin collectionPlugin;
    private String collectionName;
    protected CollectionViewerConfig initialCollectionViewerConfig;
    protected EditableTableBrowserState currentState;
    private FlowPanel rootFlowPanel;
    private TextArea textArea;
    private ConfiguredButton addButton;
    private ConfiguredButton addDefaultButton;

    public EditableTableBrowserWidget(){
        rootFlowPanel = new FlowPanel();
        textArea = new TextArea();
        rootFlowPanel.add(textArea);
    }

    @Override
    public Component createNew() {
        return new EditableTableBrowserWidget();
    }

    @Override
    public void setCurrentState(WidgetState state) {
        this.currentState = (EditableTableBrowserState)state;
        ((TextArea)((FlowPanel)impl).getWidget(0)).setText(currentState.getText());
    }

    @Override
    protected boolean isChanged() {
        String initValue = trim(((EditableTableBrowserState) getInitialData()).getText());
        final String currentValue = ((TextArea)((FlowPanel)impl).getWidget(0)).getText();
        return initValue == null ? currentValue != null : !initValue.equals(currentValue);
    }

    @Override
    protected WidgetState createNewState() {
        EditableTableBrowserState nState = new EditableTableBrowserState();
        nState.setText(((TextArea)((FlowPanel)impl).getWidget(0)).getText());
        return nState;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        currentState = (EditableTableBrowserState)state;
        textArea.setEnabled(true);
        addButton = new SelectConfiguredButton(currentState.getEditableTableBrowserConfig().getSelectButtonConfig());
        addDefaultButton = new DefaultConfiguredButton(currentState.getEditableTableBrowserConfig().getDefaultButtonConfig());
        rootFlowPanel.add(addButton);
        rootFlowPanel.add(addDefaultButton);
        return rootFlowPanel;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        currentState = (EditableTableBrowserState)state;
        textArea.setEnabled(false);
        return rootFlowPanel;
    }


}
