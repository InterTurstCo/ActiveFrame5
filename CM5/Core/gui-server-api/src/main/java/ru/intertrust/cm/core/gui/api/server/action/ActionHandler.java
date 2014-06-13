package ru.intertrust.cm.core.gui.api.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;

import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 13:14
 */
public abstract class ActionHandler<E extends ActionContext, T extends ActionData> implements ComponentHandler {
    public static final String TOGGLE_EDIT_ATTR = "toggle-edit";
    public static final String TOGGLE_EDIT_KEY = "toggleEdit";

    public enum Status {SUCCESSFUL, SKIPPED}

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected GuiService guiService;

    public T executeAction(Dto context) {
        return executeAction((E) context);
    }

    public abstract T executeAction(E context);

    public abstract E getActionContext();

    public HandlerStatusData getCheckStatusData() {
        return null;
    }

    public Status getHandlerStatus(String conditionExpression, HandlerStatusData condition) {
        return Status.SUCCESSFUL;
    }

    protected <X> X evaluateExpression(final String conditionExpression, final HandlerStatusData condition) {
        final EvaluationContext evaluationContext = new StandardEvaluationContext(condition);
        final Expression expression = new SpelExpressionParser().parseExpression(conditionExpression);
        return (X) expression.getValue(evaluationContext);
    }

    public interface HandlerStatusData {
        void initialize(Map<String, Object> params);
    }
}
