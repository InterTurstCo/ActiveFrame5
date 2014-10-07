package ru.intertrust.cm.core.gui.api.server.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.el.DomainObjectPropertyAccessor;
import ru.intertrust.cm.core.gui.api.server.el.DomainObjectTypeComparator;
import ru.intertrust.cm.core.gui.api.server.el.ReferenceValuePropertyAccessor;
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
    @Autowired
    protected CrudService crudService;
    @Autowired
    protected CurrentUserAccessor currentUserAccessor;

    public T executeAction(Dto context) {
        final ActionConfig config = context == null ? null : (ActionConfig) ((E) context).getActionConfig();
        final T result = executeAction((E) context);
        if (result != null && config != null && config.getAfterConfig() != null) {
            final DomainObject dobj = ((E) context).getRootObjectId() != null
                    ? crudService.find(((E) context).getRootObjectId())
                    : null;
            final String successPattern = (config.getAfterConfig().getMessageConfig() == null)
                    ? null
                    : config.getAfterConfig().getMessageConfig().getText();
            result.setOnSuccessMessage(parseMessage(successPattern, dobj));
        }
        return result;
    }

    public abstract T executeAction(E context);

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

    private String parseMessage(final String pattern, final DomainObject dobj) {
        if (pattern != null && dobj != null) {
            final StandardEvaluationContext evaluationContext = new StandardEvaluationContext(dobj);
            final List<PropertyAccessor> accessors = new ArrayList<>();
            accessors.add(new DomainObjectPropertyAccessor(currentUserAccessor.getCurrentUserId()));
            accessors.add(new ReferenceValuePropertyAccessor(crudService));
            evaluationContext.setPropertyAccessors(accessors);
            evaluationContext.setTypeComparator(new DomainObjectTypeComparator());
            final ExpressionParser expressionParser = new SpelExpressionParser();
            final String result = expressionParser.parseExpression(pattern, new ParserContextImpl())
                    .getValue(evaluationContext, String.class);
            return result;
        } else {
            return pattern;
        }
    }

    public interface HandlerStatusData {

        void initialize(Map<String, Object> params);

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
    }

    private static class ParserContextImpl implements ParserContext {

        @Override
        public boolean isTemplate() {
            return true;
        }

        @Override
        public String getExpressionPrefix() {
            return "{";
        }

        @Override
        public String getExpressionSuffix() {
            return "}";
        }
    }
}
