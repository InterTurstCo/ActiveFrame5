package ru.intertrust.cm.core.gui.api.server.action;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 13:14
 */
public abstract class ActionHandler<E extends ActionContext, T extends ActionData> implements ComponentHandler {
    private static final String TOGGLE_EDIT_ATTR = "toggleEdit";
    private static final String TOGGLE_EDIT_KEY = "toggle-edit";

    public enum Status {APPLY, SKIP}

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected GuiService guiService;

    public T executeAction(Dto context) {
        return executeAction((E) context);
    }

    public abstract T executeAction(E context);

    /**
     * @deprecated
     * Нужно использовать {@link ActionHandler#getActionContext(ActionConfig)}
     */
    @Deprecated
    public <E extends ActionContext> E getActionContext() {
        return (E) new ActionContext();
    }

    public <E extends ActionContext> E getActionContext(ActionConfig actionConfig) {
        return null;
    }

    public HandlerStatusData getCheckStatusData() {
        return new DefaultHandlerStatusData();
    }

    public Status getHandlerStatus(String conditionExpression, HandlerStatusData condition) {
        if (condition != null) {
            conditionExpression = conditionExpression.replaceAll(TOGGLE_EDIT_KEY, TOGGLE_EDIT_ATTR);
            final boolean result = evaluateExpression(conditionExpression, condition);
            return result ? Status.APPLY : Status.SKIP;
        } else {
            return Status.APPLY;
        }
    }

    private boolean evaluateExpression(final String conditionExpression, final HandlerStatusData condition) {
        final EvaluationContext evaluationContext = new StandardEvaluationContext(condition);
        final Expression expression = new SpelExpressionParser().parseExpression(conditionExpression);
        return (boolean) expression.getValue(evaluationContext);
    }

    public interface HandlerStatusData {

        void initialize(Map<String, Object> params);

        boolean isNewDomainObject();

        Object getParameter(String key);
    }

    public static class DefaultHandlerStatusData implements HandlerStatusData {
        @Override
        public void initialize(Map<String, Object> params) {
        }

        @Override
        public Object getParameter(String key) {
            return null;
        }

        @Override
        public boolean isNewDomainObject() {
            return false;
        }
    }
}
