package ru.intertrust.cm.core.gui.impl.client.form.widget.editabletablebrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.TextBoxWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.DefaultConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.SelectConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.LinkedHashMap;
import java.util.Map;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.STATE_KEY;


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
    private StretchyTextArea textArea;
    private ConfiguredButton addButton;
    private ConfiguredButton addDefaultButton;

    public EditableTableBrowserWidget() {
        rootFlowPanel = new FlowPanel();
        textArea = new StretchyTextArea();
        rootFlowPanel.add(textArea);
        rootFlowPanel.addStyleName("root-editable-tablebrowser-widget");
        textArea.addStyleName("textarea-editable-tablebrowser-widget");

    }

    @Override
    public Component createNew() {
        return new EditableTableBrowserWidget();
    }

    @Override
    public void setCurrentState(WidgetState state) {
        this.currentState = (EditableTableBrowserState) state;
        ((TextArea) ((FlowPanel) impl).getWidget(0)).setText(currentState.getText());
    }

    @Override
    protected boolean isChanged() {
        getDefaultValue();
        String initValue = trim(((EditableTableBrowserState) getInitialData()).getText());
        final String currentValue = ((TextArea) ((FlowPanel) impl).getWidget(0)).getText();
        return initValue == null ? currentValue != null : !initValue.equals(currentValue);
    }

    @Override
    protected WidgetState createNewState() {
        EditableTableBrowserState nState = new EditableTableBrowserState();
        nState.setText(((TextArea) ((FlowPanel) impl).getWidget(0)).getText());
        return nState;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        currentState = (EditableTableBrowserState) state;
        textArea.setEnabled(true);
        addButton = new SelectConfiguredButton(currentState.getEditableTableBrowserConfig().getSelectButtonConfig());
        addDefaultButton = new DefaultConfiguredButton(currentState.getEditableTableBrowserConfig().getDefaultButtonConfig());
        rootFlowPanel.add(addButton);
        rootFlowPanel.add(addDefaultButton);
        addDefaultButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                getDefaultValue();
            }
        });

        if (!currentState.getEditableTableBrowserConfig().isEnterKeyAllowed()) {
            textArea.addKeyDownHandler(new KeyDownHandler() {
                @Override
                public void onKeyDown(KeyDownEvent event) {
                    int key = event.getNativeEvent().getKeyCode();
                    if (key == KeyCodes.KEY_ENTER) {
                        event.stopPropagation();
                        event.preventDefault();
                    }
                }
            });
            textArea.addKeyPressHandler(new KeyPressHandler() {
                @Override
                public void onKeyPress(KeyPressEvent event) {
                    int key = event.getNativeEvent().getKeyCode();
                    if (key == KeyCodes.KEY_ENTER) {
                        event.stopPropagation();
                        event.preventDefault();
                    }
                }
            });
            textArea.addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent event) {
                    int key = event.getNativeEvent().getKeyCode();
                    if (key == KeyCodes.KEY_ENTER) {
                        event.stopPropagation();
                        event.preventDefault();
                    }
                }
            });
            textArea.addValueChangeHandler(new ValueChangeHandler() {
                @Override
                public void onValueChange(ValueChangeEvent changeEvent) {
                    if (textArea.getText().contains("\n")) {
                        textArea.setText(textArea.getText().replace("\n", " "));
                    }
                }
            });


        }
        return rootFlowPanel;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        currentState = (EditableTableBrowserState) state;
        textArea.setEnabled(false);
        return rootFlowPanel;
    }

    private void getDefaultValue() {
        EditableBrowserRequestData rData = new EditableBrowserRequestData();
        rData.setConfig(((EditableTableBrowserState) currentState).getWidgetConfig());
        if (getContainer().getPlugin() instanceof FormPlugin) {
            rData.setFormState((((FormPlugin) getContainer().getPlugin())).getFormState());
        }

        Command command = new Command("getDefaultValue", "editable-table-browser", rData);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                EditableBrowserRequestData response = (EditableBrowserRequestData) result;
                textArea.clear();
                textArea.setValue(response.getDefaultValue());
            }

            @Override
            public void onFailure(Throwable caught) {

            }
        });

    }
}
