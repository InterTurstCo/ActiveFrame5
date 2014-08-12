package ru.intertrust.cm.core.gui.impl.client.form.widget.messagedialog;

import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 07.08.2014
 *         Time: 22:24
 */
public class InfoMessageDialog extends MessageDialog {
    public InfoMessageDialog(String text) {
        super(text);
    }

    @Override
    protected String getContentStyleName() {
        return "infoDialogContent";
    }

    @Override
    protected String getDialogStyleName() {
        return "infoDialogWindow";
    }

    @Override
    protected String getMessageStyleName() {
        return GlobalThemesManager.getCurrentTheme().commonCss().infoDialogIm();
    }
}
