package ru.intertrust.cm.core.gui.impl.client.event.form;

import com.google.gwt.user.client.ui.DialogBox;
import ru.intertrust.cm.core.gui.impl.client.event.SelfRegisteredEventHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.06.2015
 *         Time: 8:27
 */
public class CloseFormDialogWindowEventHandler extends SelfRegisteredEventHandler {
    private DialogBox dialogBox;

    public CloseFormDialogWindowEventHandler(DialogBox dialogBox) {
        this.dialogBox = dialogBox;
    }
    void forceClosing(){
        dialogBox.hide();
        removeItself();
    }
}
