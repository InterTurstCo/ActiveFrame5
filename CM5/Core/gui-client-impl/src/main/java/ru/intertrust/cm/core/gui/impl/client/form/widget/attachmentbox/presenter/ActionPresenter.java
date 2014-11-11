package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import ru.intertrust.cm.core.config.gui.form.widget.ActionLinkConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.AttachmentActionLinkHandler;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

/**
 * @author Lesia Puhova
 *         Date: 16.10.14
 *         Time: 15:47
 */
public class ActionPresenter implements AttachmentElementPresenter {

    private final AttachmentElementPresenter presenter;
    private final ActionLinkConfig actionLinkConfig;
    private final AttachmentItem item;

    public ActionPresenter(AttachmentElementPresenter presenter, ActionLinkConfig actionLinkConfig, AttachmentItem item)  {
        this.presenter = presenter;
        this.actionLinkConfig = actionLinkConfig;
        this.item = item;
    }

    @Override
    public Panel presentElement() {
        Panel element = (presenter != null ? presenter.presentElement() : new AbsolutePanel());
        if (actionLinkConfig != null) {
            element.add(buildActionLink(actionLinkConfig, item));
        }
        return element;
    }

    private Button buildActionLink(ActionLinkConfig actionLinkConfig, final AttachmentItem attachmentItem) {
        Button actionLinkButton = new Button();
        actionLinkButton.removeStyleName("gwt-Button");
        actionLinkButton.addStyleName("dialog-box-button");
        actionLinkButton.setText(actionLinkConfig.getText());
        final String actionName = actionLinkConfig.getActionName();

        actionLinkButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AttachmentActionLinkHandler action = ComponentRegistry.instance.get(actionName);
                action.setAttachmentItem(attachmentItem);
                action.execute();
            }
        });

        return actionLinkButton;
    }


}
