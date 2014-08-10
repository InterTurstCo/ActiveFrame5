package ru.intertrust.cm.core.gui.impl.client;

import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.impl.client.form.widget.confirm.ConfirmDialog;
import ru.intertrust.cm.core.gui.impl.client.form.widget.messagedialog.ErrorMessageDialog;
import ru.intertrust.cm.core.gui.impl.client.form.widget.messagedialog.InfoMessageDialog;
import ru.intertrust.cm.core.gui.impl.client.form.widget.messagedialog.MessageDialog;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 07.08.2014
 *         Time: 22:33
 */
public class ApplicationWindow {

    public static void errorAlert(String message) {
        MessageDialog messageDialog = new ErrorMessageDialog(message);
        messageDialog.alert();
    }


    public static void infoAlert(String message) {
        MessageDialog messageDialog = new InfoMessageDialog(message);
        messageDialog.alert();

    }

    public static void confirm(String message, ConfirmCallback confirmCallback) {
        ConfirmDialog confirmDialog = new ConfirmDialog(message, confirmCallback);
        confirmDialog.confirm();
    }
}
