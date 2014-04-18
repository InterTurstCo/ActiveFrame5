package ru.intertrust.cm.core.gui.model.action;

import java.util.HashMap;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.action.AbstractActionEntryConfig;

/**
 * @author Sergey.Okolot
 *         Created on 16.04.2014 18:44.
 */
public class PluginActionEntryContext implements Dto {

    private HashMap<String, ActionEntryContext> actionContexts = new HashMap<String, ActionEntryContext>();

    private ToolbarContext toolbarContext;

    public ActionEntryContext addAction(final AbstractActionEntryConfig config) {
        final ActionEntryContext ctx = configToContext(config);
        actionContexts.put(ctx.getId(), ctx);
        return ctx;
    }

    public ActionEntryContext addToolbarAction(final AbstractActionEntryConfig config, final boolean isRightFacet) {
        final ActionEntryContext ctx = configToContext(config);
        actionContexts.put(ctx.getId(), ctx);
        toolbarContext.addActionId(ctx.getId(), isRightFacet);
        return ctx;
    }

    public HashMap<String, ActionEntryContext> getActionContexts() {
        return actionContexts;
    }

    public ToolbarContext getToolbarContext() {
        return toolbarContext;
    }

    public ActionEntryContext getActionContext(final String id) {
        return actionContexts.get(id);
    }

    private ActionEntryContext configToContext(final AbstractActionEntryConfig config) {
        final ActionEntryContext ctx = new ActionEntryContext();
        return ctx;
    }
}

