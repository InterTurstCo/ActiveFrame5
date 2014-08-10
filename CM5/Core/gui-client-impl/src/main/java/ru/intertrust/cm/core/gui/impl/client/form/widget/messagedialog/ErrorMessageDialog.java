package ru.intertrust.cm.core.gui.impl.client.form.widget.messagedialog;

import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 07.08.2014
 *         Time: 22:27
 */
public class ErrorMessageDialog extends MessageDialog {
    public ErrorMessageDialog(String text) {
        super(text);
    }

    @Override
    protected String getContentStyleName() {
        return "errorDialogContent";
    }

    @Override
    protected String getDialogStyleName() {
        return "errorDialogWindow";
    }

    @Override
    protected String getMessageStyleName() {
        return GlobalThemesManager.getCurrentTheme().commonCss().errorDialogIm();
    }
}
