package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory;

import com.google.gwt.event.dom.client.ClickHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.AttachmentElementPresenter;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

/**
 * @author Lesia Puhova
 *         Date: 17.10.14
 *         Time: 17:18
 */
public interface AttachmentElementPresenterFactory {

    public AttachmentElementPresenter createPresenter(AttachmentItem item);
    public AttachmentElementPresenter createPresenter(AttachmentItem item, ClickHandler handler);
}
