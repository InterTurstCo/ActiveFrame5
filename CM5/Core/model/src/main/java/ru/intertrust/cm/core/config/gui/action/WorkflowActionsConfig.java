package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.Collections;
import java.util.List;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 14.04.2015
 */
@Root(name="workflow-actions")
public class WorkflowActionsConfig extends AbstractActionConfig {
    @ElementListUnion({
            @ElementList(entry = "workflow-action", type = WorkflowActionConfig.class, inline = true, required = true)
    })
    private List<WorkflowActionConfig> actions;

    public List<WorkflowActionConfig> getActions() {
        return actions == null ? Collections.EMPTY_LIST : actions;
    }
}
