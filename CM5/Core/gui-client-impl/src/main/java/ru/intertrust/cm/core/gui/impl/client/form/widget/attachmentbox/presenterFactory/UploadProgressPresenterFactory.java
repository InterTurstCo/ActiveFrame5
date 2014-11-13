package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.ActionLinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.DeleteButtonConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.ActionPresenter;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.AttachmentElementPresenter;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.DeleteButtonPresenter;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.TextPresenter;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.UploadProgressPresenter;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 11.11.14
 *         Time: 17:46
 */
public class UploadProgressPresenterFactory implements AttachmentElementPresenterFactory {
    private final ActionLinkConfig actionLinkConfig;
    private final DeleteButtonConfig deleteButtonConfig;
    private final EventBus eventBus;

    public UploadProgressPresenterFactory(ActionLinkConfig actionLinkConfig, DeleteButtonConfig deleteButtonConfig,
                                          EventBus eventBus) {
        this.actionLinkConfig = actionLinkConfig;
        this.deleteButtonConfig = deleteButtonConfig;
        this.eventBus = eventBus;
    }

    @Override
    public AttachmentElementPresenter createPresenter(final AttachmentItem item, ClickHandler deleteHandler,
                                                      List<AttachmentItem> attachments) {
        AttachmentElementPresenter presenter = new TextPresenter(item.getName());
        presenter = new UploadProgressPresenter(presenter, eventBus);
        presenter = new DeleteButtonPresenter(presenter, item, deleteButtonConfig, deleteHandler);
        presenter = new ActionPresenter(presenter, actionLinkConfig, item);
        return presenter;
    }
}
