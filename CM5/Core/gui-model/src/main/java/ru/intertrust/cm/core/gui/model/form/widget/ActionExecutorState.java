package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 04.09.2014 15:28.
 */
public class ActionExecutorState extends LabelState {

    private ActionContext actionContext;

    public void setLabelStates(LabelState labelState) {
        super.setLabel(labelState.getLabel());
        super.setAsteriskRequired(labelState.isAsteriskRequired());
        super.setPattern(labelState.getPattern());
        super.setFontWeight(labelState.getFontWeight());
        super.setFontStyle(labelState.getFontStyle());
        super.setFontSize(labelState.getFontSize());
    }

    public ActionContext getActionContext() {
        return actionContext;
    }

    public void setActionContext(ActionContext actionContext) {
        this.actionContext = actionContext;
    }
}
