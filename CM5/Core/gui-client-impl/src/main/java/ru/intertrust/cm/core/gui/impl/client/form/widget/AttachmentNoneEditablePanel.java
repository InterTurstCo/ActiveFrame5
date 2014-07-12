package ru.intertrust.cm.core.gui.impl.client.form.widget;


import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
public class AttachmentNoneEditablePanel extends AbstractNoneEditablePanel {
    public AttachmentNoneEditablePanel(SelectionStyleConfig selectionStyleConfig) {
        super(selectionStyleConfig);
    }
    public void displayAttachment(AttachmentItem item){
        final AbsolutePanel element = new AbsolutePanel();
        element.setStyleName("facebook-element");
        String contentLength = item.getContentLength();
        String anchorTitle = contentLength == null ? item.getName() : item.getName() + " (" + item.getContentLength() + ")";
        Anchor fileNameAnchor = new Anchor(anchorTitle);
        fileNameAnchor.addClickHandler(new DownloadAttachmentHandler(item));
        element.add(fileNameAnchor);
        mainBoxPanel.add(element);
    }
}
