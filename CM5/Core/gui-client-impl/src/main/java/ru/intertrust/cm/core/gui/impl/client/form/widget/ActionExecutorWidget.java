package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ActionExecutorConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.ActionExecutorState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Sergey.Okolot
 *         Created on 04.09.2014 15:25.
 */
@ComponentName(ActionExecutorConfig.COMPONENT_NAME)
public class ActionExecutorWidget extends LabelWidget {

    @Override
    public Component createNew() {
        return new ActionExecutorWidget();
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        final HorizontalPanel result = (HorizontalPanel) super.asEditableWidget(state);
        if (((ActionExecutorState) state).getActionContext() != null) {
            result.getWidget(1).getElement().setClassName("actionExecutorItem");
            result.addDomHandler(new ClickHandlerImpl(), ClickEvent.getType());
        }
        return result;
    }

    private class ClickHandlerImpl implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            final ActionExecutorState state = getInitialData();
            final ActionContext actionContext = state.getActionContext();
            final Action action =
                    ComponentRegistry.instance.get(((ActionConfig)actionContext.getActionConfig()).getComponentName());
            action.setInitialContext(actionContext);
            action.perform();
        }
    }
}
