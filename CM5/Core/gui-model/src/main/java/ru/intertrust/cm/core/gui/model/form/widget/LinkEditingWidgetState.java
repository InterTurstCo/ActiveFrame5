package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.ArrayList;

/**
 * If widget is capable of setting links between objects, similarly it can set a single reference field, in case the
 * widget itself is limit by a single choice
 *
 * @author Denis Mitavskiy
 *         Date: 26.10.13
 *         Time: 22:13
 */
public abstract class LinkEditingWidgetState extends ValueEditingWidgetState {
    private boolean singleChoice;

    public boolean isSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(boolean singleChoice) {
        this.singleChoice = singleChoice;
    }

    public abstract ArrayList<Id> getIds();

}
