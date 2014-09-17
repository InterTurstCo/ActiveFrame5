package ru.intertrust.cm.core.gui.impl.server.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationException;

/**
 *
 *
 * @author Sergey.Okolot
 *         Created on 16.09.2014 12:49.
 */
public class VisibilityExpressionParser {
    private static final String DELIMITER = " \t\r\n\f";
    private static final String CURRENT_USER = "current_user";

    private final String expression;
    private final Id currentUserId;
    private Map<String, String> expressionPairs = new HashMap<>();

    public VisibilityExpressionParser(final String expression, final Id currentUserId) {
        this.expression = expression;
        this.currentUserId = currentUserId;
    }

    public void parse() {
        final StringTokenizer tokenizer = new StringTokenizer(expression, "='" + DELIMITER, true);
        String name = null;
        boolean isCondition = false;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if ("'".equals(token)) {
                token = getStringToken(tokenizer);
            }
            if (DELIMITER.contains(token)) {
                continue;
            }
            if (CURRENT_USER.equals(token.toLowerCase())) {
                token = currentUserId.toStringRepresentation();
            }
            if ("=".equals(token)) {
                isCondition = true;
            } else if (!isCondition) {
                name = token;
            } else {
                expressionPairs.put(name, token);
                isCondition = false;
            }
        }
        System.out.println("--------------------> " + expressionPairs);
    }

    private String getStringToken(StringTokenizer tokenizer) {
        final StringBuilder result = new StringBuilder("'");
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            result.append(token);
            if ("'".equals(token)) {
                return result.toString();
            }
        }
        throw new ConfigurationException("Ошибка в выражении '" + expression + "'. Не закрыта строка " + result.toString());
    }

    public static void main(String[] args) {
        final VisibilityExpressionParser parser = new VisibilityExpressionParser(
                "status =  null or status.name= 'active ' and ' blabla ' = ' blabla' and created_by=current_user",
                new RdbmsId("5050000000000004"));
        parser.parse();

        parser.evaluateExpressions2();
    }

    private void evaluateExpressions2() {
        final DomainObject dObj = new GenericDomainObject();
        dObj.setId(new RdbmsId("5050000000000004"));
        dObj.setString("created_by", "5050000000000004");
        final StandardEvaluationContext context = new StandardEvaluationContext(dObj);

        final List<PropertyAccessor> accessors = new ArrayList<>();
        accessors.add(new DomainObjectPropertyAccessor(currentUserId));
        context.setPropertyAccessors(accessors);
        context.setTypeComparator(new DomainObjectTypeComparator());
        final ExpressionParser expressionParser = new SpelExpressionParser();
        Object result = expressionParser.parseExpression(
                "created_by == null"
        ).getValue(context);

        System.out.println("--------------------> " + result);
    }

    private void evaluateExpressions1() {
        final StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("name", "name");
        context.setVariable("context", Boolean.TRUE);
        context.setVariable("val", 10);
        final ExpressionParser expressionParser = new SpelExpressionParser();
        Object result = expressionParser.parseExpression(
                "#name=='name' and #context==true and #val > (9 - 1)/2"
        ).getValue(context);

        System.out.println("--------------------> " + result);
    }

    private static class EvaluateContext {
        private DomainObject domainObject;

    }
}
