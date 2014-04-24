package ru.intertrust.cm.core.gui.model.action;

import java.util.ArrayList;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 16.04.2014 18:46.
 */
public class ToolbarContext implements Dto {

    private ArrayList<ActionEntryContext> leftFacetActions = new ArrayList<ActionEntryContext>();
    private ArrayList<ActionEntryContext> rightFacetActions = new ArrayList<ActionEntryContext>();

    public void addAction(final ActionEntryContext actionCtx, final boolean isRightFacet) {
        if (isRightFacet) {
            rightFacetActions.add(actionCtx);
        } else {
            leftFacetActions.add(actionCtx);
        }
    }

    public ArrayList<ActionEntryContext> getLeftFacetActions() {
        return leftFacetActions;
    }

    public ArrayList<ActionEntryContext> getRightFacetActions() {
        return rightFacetActions;
    }
}
