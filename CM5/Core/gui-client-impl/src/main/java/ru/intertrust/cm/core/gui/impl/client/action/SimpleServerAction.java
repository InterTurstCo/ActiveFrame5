package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.event.ActionSuccessListener;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.util.StringUtil;
import ru.intertrust.cm.core.gui.model.validation.ValidationException;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.EXECUTION_ACTION_ERROR;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 18:11
 */
public abstract class SimpleServerAction extends Action {
    private List<ActionSuccessListener> successListeners = new ArrayList<ActionSuccessListener>();

    protected void execute() {
        AsyncCallback<Dto> callback = new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                SimpleServerAction.this.onSuccess((ActionData) result);
                for (ActionSuccessListener listener : successListeners) {
                    listener.onSuccess();
                }
                showOnSuccessMessage((ActionData) result, getDefaultOnSuccessMessage());
            }

            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof GuiException) {
//                    if (caught.getCause() instanceof ValidationException) {
//                        onValidationFailure((ValidationException) caught.getCause());
//                    } else {
                        throw ((GuiException) caught);
//                    }
                } else {
                    throw new GuiException(LocalizeUtil.get(EXECUTION_ACTION_ERROR) + caught.getMessage(), caught);
                }
            }
        };
        try {
            ActionContext currentContext = appendCurrentContext(initialContext);
            Command command = new Command("executeAction", getName(), currentContext);
            final AbstractActionConfig abstractActionConfig = getInitialContext().getActionConfig();
            if (abstractActionConfig instanceof ActionConfig) {
                command.setDirtySensitivity(((ActionConfig) abstractActionConfig).isDirtySensitivity());
            }
            BusinessUniverseServiceAsync.Impl.executeCommand(command, callback, true, true);
        } catch (GuiException e) {
            ApplicationWindow.errorAlert(LocalizeUtil.get(EXECUTION_ACTION_ERROR) + e.getMessage());
        }
    }

    protected ActionContext appendCurrentContext(ActionContext initialContext) {
        return initialContext;
    }

    protected void onSuccess(ActionData result) {
    }

    protected String getDefaultOnSuccessMessage() {
        return LocalizeUtil.get(BusinessUniverseConstants.DONE_SUCCESSFULLY_MESSAGE);
    }

    public void addActionSuccessListener(ActionSuccessListener listener) {
        successListeners.add(listener);
    }

    // TODO: [validation] move it to some better place
    //FIXME: the dialogBox is not shown
     private void onValidationFailure(ValidationException validationException) {
        final DialogBox dialogBox = new DialogBox(false, true);
        dialogBox.setText("Validation errors");
        dialogBox.addStyleName("validation-error-box");
        dialogBox.setStylePrimaryName("validation-error-box");
        VerticalPanel content = new VerticalPanel();
        content.add(new HTML(StringUtil.join(validationException.getValidationErrors(), "<br/>")));
        Button closeButton = new Button("Close");
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
        buttonPanel.setWidth("100%");
        buttonPanel.add(closeButton);
        content.add(buttonPanel);
        dialogBox.add(content);
        dialogBox.center();
    }
}
