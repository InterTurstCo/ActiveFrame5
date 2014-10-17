package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.EventBus;

import ru.intertrust.cm.core.gui.impl.client.event.UploadCompletedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.UploadCompletedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.UploadUpdatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.UploadUpdatedEventHandler;

/**
 * @author Lesia Puhova
 *         Date: 16.10.14
 *         Time: 17:27
 */
class UploadProgressPresenter implements AttachmentElementPresenter,
        UploadUpdatedEventHandler, UploadCompletedEventHandler {

    private AttachmentElementPresenter presenter;
    private EventBus eventBus;
    private Label percentage = new Label("0%");

    UploadProgressPresenter(AttachmentElementPresenter presenter, EventBus eventBus) {
        this.presenter = presenter;
        this.eventBus = eventBus;
    }

    @Override
    public Panel presentElement() {
        Panel element = (presenter != null ? presenter.presentElement() : new AbsolutePanel());

        element.addStyleName("linkedWidgetsBorderStyle");
        element.addStyleName("loading-attachment");

        Image progressbar = new Image();
        progressbar.setUrl("CMJSpinner.gif");
        element.add(progressbar);

        percentage = new Label("0%");
        percentage.addStyleName("loading-attachment");
        element.add(percentage);

        eventBus.addHandler(UploadUpdatedEvent.TYPE, this);
        eventBus.addHandler(UploadCompletedEvent.TYPE, this);
        return element;
    }

    @Override
    public void onPercentageUpdated(UploadUpdatedEvent event) {
        percentage.setText(event.getPercentageValue() + "%");
    }

    @Override
    public void onUploadCompleted(UploadCompletedEvent event) {
        percentage.getParent().removeFromParent();
    }
}
