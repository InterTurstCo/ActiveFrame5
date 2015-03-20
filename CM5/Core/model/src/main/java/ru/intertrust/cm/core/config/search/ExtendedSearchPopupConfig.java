package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.DialogWindowConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 25.01.2015
 *         Time: 15:03
 */
@Root(name="extended-search-popup")
public class ExtendedSearchPopupConfig implements Dto {
    @Element(name = "dialog-window", required = false)
    private DialogWindowConfig dialogWindowConfig;

    public DialogWindowConfig getDialogWindowConfig() {
        return dialogWindowConfig;
    }

    public void setDialogWindowConfig(DialogWindowConfig dialogWindowConfig) {
        this.dialogWindowConfig = dialogWindowConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExtendedSearchPopupConfig that = (ExtendedSearchPopupConfig) o;

        if (dialogWindowConfig != null ? !dialogWindowConfig.equals(that.dialogWindowConfig) : that.dialogWindowConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return dialogWindowConfig != null ? dialogWindowConfig.hashCode() : 0;
    }
}
