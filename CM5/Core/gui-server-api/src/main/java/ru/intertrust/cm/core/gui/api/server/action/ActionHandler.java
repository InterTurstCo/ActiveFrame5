package ru.intertrust.cm.core.gui.api.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.AfterActionExecutionConfig;
import ru.intertrust.cm.core.config.gui.action.OnSuccessMessageConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @Autowired
    private FormatHandler formatHandler;


    public T executeAction(Dto contextDto) {
        final E context = (E) contextDto;
        final ActionConfig config = contextDto == null ? null : (ActionConfig) context.getActionConfig();
        final T result = executeAction(context);
        if (result != null) {
            final Id rootObjectId;
            if (result instanceof SimpleActionData) {
                rootObjectId = ((SimpleActionData) result).getSavedMainObjectId();
            } else {
                rootObjectId = context.getRootObjectId();
            }
            final AfterActionExecutionConfig afterConfig = config.getAfterConfig();
            if (config != null && afterConfig != null) {
                final DomainObject dobj = rootObjectId != null ? crudService.find(rootObjectId) : null;
                final OnSuccessMessageConfig messageConfig = afterConfig.getMessageConfig();
                String successPattern = (messageConfig == null) ? null : messageConfig.getText();
                if (result.getOnSuccessMessage()!=null) successPattern = result.getOnSuccessMessage(); // перезаписываем дефолтное сообщение тем, которое приехало из реализации action
                result.setOnSuccessMessage(parseMessage(successPattern, dobj));
            }
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

    private String parseMessage(final String displayPattern, final DomainObject dobj) {
        if (displayPattern != null && dobj != null) {
            final Pattern pattern = Pattern.compile("\\{[\\w.]+\\}");
            Matcher matcher = pattern.matcher(displayPattern);
            final FormattingConfig formattingConfig = new FormattingConfig();
            final String result = formatHandler.format(dobj, matcher, formattingConfig);
            return result;
        } else {
            return displayPattern;
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
}
