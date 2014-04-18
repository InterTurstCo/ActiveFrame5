package ru.intertrust.cm.core.gui.model.action;

import java.util.ArrayList;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 16.04.2014 18:46.
 */
public class ToolbarContext implements Dto {

    private ArrayList<String> leftFacetActionIds = new ArrayList<String>();
    private ArrayList<String> rightFacetActionIds = new ArrayList<String>();

    public void addActionId(final String actionId, final boolean isRightFacet) {
        if (isRightFacet) {
            rightFacetActionIds.add(actionId);
        } else {
            leftFacetActionIds.add(actionId);
        }
    }

    public ArrayList<String> getLeftFacetActionIds() {
        return leftFacetActionIds;
    }

    public ArrayList<String> getRightFacetActionIds() {
        return rightFacetActionIds;
    }
}
