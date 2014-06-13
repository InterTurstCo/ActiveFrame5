package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Sergey.Okolot
 *         Created on 16.04.2014 18:46.
 */
public class ToolbarContext implements Dto {

    public static enum FacetName {LEFT, RIGHT}

    private List<ActionContext> leftFacetActions = new ArrayList<>();
    private List<ActionContext> rightFacetActions = new ArrayList<>();

    public void addContexts(final List<ActionContext> contexts, final FacetName facetName) {
        if (contexts != null) {
            if (FacetName.RIGHT == facetName) {
                rightFacetActions.addAll(contexts);
            } else {
                leftFacetActions.addAll(contexts);
            }
        }
    }

    public void setContexts(final List<ActionContext> contexts, final FacetName facetName) {
        if (FacetName.RIGHT == facetName) {
            rightFacetActions.clear();
            rightFacetActions.addAll(contexts);
        } else {
            leftFacetActions.clear();
            leftFacetActions.addAll(contexts);
        }
    }

    public List<ActionContext> getContexts(final FacetName facetName) {
        final List<ActionContext> result;
        if (FacetName.RIGHT == facetName) {
            result = rightFacetActions;
        } else {
            result = leftFacetActions;
        }
        return result;
    }

    public void copyToolbar(final ToolbarContext toolbarContext) {
        if (toolbarContext != null) {
            setContexts(toolbarContext.getContexts(FacetName.LEFT), FacetName.LEFT);
            setContexts(toolbarContext.getContexts(FacetName.RIGHT), FacetName.RIGHT);
        }
    }

    public void mergeToolbar(final ToolbarContext toolbarContext) {
        if (toolbarContext != null) {
            List<ActionContext> toMergeContexts = getMergedContexts(toolbarContext.getContexts(FacetName.LEFT));
            // FIXME remove by weight
            leftFacetActions.addAll(toMergeContexts);
            toMergeContexts = getMergedContexts(toolbarContext.getContexts(FacetName.RIGHT));
            rightFacetActions.addAll(toMergeContexts);
        }
    }

    public void sortActionContexts() {
        if (!leftFacetActions.isEmpty()) {
            Collections.sort(leftFacetActions, new ContextComparator());
        }
        if (!rightFacetActions.isEmpty()) {
            Collections.sort(rightFacetActions, new ContextComparator());
        }
    }

    private static List<ActionContext> getMergedContexts(final List<ActionContext> contexts) {
        final List<ActionContext> result = new ArrayList<>();
        for (ActionContext context : contexts) {
                if (context.getActionConfig().isMerged()) {
                    result.add(context);
            }
        }
        return result;
    }

    private static class ContextComparator implements Comparator<ActionContext> {
        @Override
        public int compare(ActionContext ac1, ActionContext ac2) {
            return ac1.getActionConfig().getOrder() - ac2.getActionConfig().getOrder();
        }
    }
}
