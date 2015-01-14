package ru.intertrust.cm.core.gui.impl.client.form.widget.panel;

import com.google.gwt.user.client.ui.AbsolutePanel;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 07.01.2015
 *         Time: 19:35
 */
public class IdentifiedPanel extends AbsolutePanel{
    private Id id;

    public IdentifiedPanel(Id id) {
        this.id = id;
    }


    public Id getId() {
        return id;
    }
}
