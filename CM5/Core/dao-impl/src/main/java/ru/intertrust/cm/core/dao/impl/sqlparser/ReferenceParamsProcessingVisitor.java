package ru.intertrust.cm.core.dao.impl.sqlparser;

import static java.util.Collections.singletonList;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import ru.intertrust.cm.core.business.api.QueryModifierPrompt;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.impl.CollectionsDaoImpl;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

/**
 * Заполняет ссылочные параметры в SQL запросе. Добавляет тип ссылочного поля в
 * SQL запрос. Например, выполняет следующую замену: t.id = {0} -> t.id = 1 and
 * t.id_type = 2.
 * @author atsvetkov
 */

public class ReferenceParamsProcessingVisitor extends BaseParamProcessingVisitor {
    private Map<String, String> replaceExpressions = new HashMap<>();

    private QueryModifierPrompt prompt;
    private boolean namedParameters;

    public ReferenceParamsProcessingVisitor(QueryModifierPrompt prompt, boolean namedParameters) {
        this.prompt = prompt;
        this.namedParameters = namedParameters;
    }

    public Map<String, String> getReplaceExpressions() {
        return replaceExpressions;
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        visitBinaryExpression(equalsTo);
        processReferenceParameters(equalsTo, true);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        visitBinaryExpression(notEqualsTo);
        processReferenceParameters(notEqualsTo, false);
    }

    @Override
    public void visit(InExpression inExpression) {
        inExpression.getLeftExpression().accept(this);
        inExpression.getRightItemsList().accept(this);
        boolean isInExpression = !inExpression.isNot();
        processReferenceParameterInsideInExpression(inExpression, isInExpression);
    }

    private void processReferenceParameters(BinaryExpression equalsTo, boolean isEquals) {
        if (prompt == null) {
            return;
        }
        if (equalsTo.getLeftExpression() instanceof Column) {
            Column column = (Column) equalsTo.getLeftExpression();

            String rightExpression = equalsTo.getRightExpression().toString();

            String paramName = getParamName(rightExpression);

            if (paramName != null && prompt.getIdParamsPrompt().containsKey(paramName)) {

                Expression newReferenceExpression = createFilledReferenceExpression(column, paramName, equalsTo, isEquals);
                replaceExpressions.put(equalsTo.toString(), newReferenceExpression.toString());
            }

        }
    }

    private String getParamName(String expression) {
        String paramName = null;
        if (namedParameters) {
            String namedParamRegexp = CollectionsDaoImpl.PARAM_NAME_PREFIX + "([\\w]+)([\\d]+)";
            Matcher matcher = Pattern.compile(namedParamRegexp).matcher(expression);
            if (matcher.find()) {
                paramName = matcher.group(1) + "_" + matcher.group(2);
            }
        } else {
            String unnamedParamRegexp = CollectionsDaoImpl.START_PARAM_SIGN + CollectionsDaoImpl.PARAM_NAME_PREFIX + "([\\d]+)"
                    + CollectionsDaoImpl.END_PARAM_SIGN;
            Matcher matcher = Pattern.compile(unnamedParamRegexp).matcher(expression);
            if (matcher.find()) {
                paramName = CollectionsDaoImpl.JDBC_PARAM_PREFIX + matcher.group(1);
            }
        }
        return paramName;
    }

    private void processReferenceParameterInsideInExpression(InExpression inExpression, boolean isEquals) {
        if (prompt == null) {
            return;
        }
        if (inExpression.getRightItemsList() instanceof SubSelect) {
            return;
        }
        if (inExpression.getLeftExpression() instanceof Column) {
            Column column = (Column) inExpression.getLeftExpression();

            String paramName = getParamName(inExpression.getRightItemsList().toString());

            if (paramName != null) {

                Expression finalExpression = null;

                if (prompt.getIdParamsPrompt().containsKey(paramName)) {
                    finalExpression = processListValue(isEquals, column, paramName, prompt.getIdParamsPrompt().get(paramName));
                }
                if (finalExpression != null) {
                    if (!(finalExpression instanceof Parenthesis)) {
                        finalExpression = new Parenthesis(finalExpression);
                    }
                    replaceExpressions.put(inExpression.toString(), finalExpression.toString());
                }

            }
        }
    }

    protected Expression processListValue(boolean isIn, Column column, String baseParamName, Integer numberOfUniqueTypes) {

        Expression finalExpression = null;
        for (int i = 0; i < numberOfUniqueTypes; i++) {
            String paramName =
                    new StringBuilder().append(baseParamName).append("_")
                            .append(i).toString();

            finalExpression = finalExpression == null ? inExpressionForSingleType(column, paramName, !isIn) :
                    connect(finalExpression, inExpressionForSingleType(column, paramName, !isIn), !isIn);
        }
        return finalExpression;
    }

    protected Expression createFilledReferenceExpression(Column column, String paramName, BinaryExpression equalsTo, boolean isEquals) {
        BinaryExpression expressionForReferenceId = null;
        if (isEquals) {
            expressionForReferenceId = new EqualsTo();
        } else {
            expressionForReferenceId = new NotEqualsTo();
        }

        expressionForReferenceId.setLeftExpression(equalsTo.getLeftExpression());
        expressionForReferenceId.setRightExpression(equalsTo.getRightExpression());

        BinaryExpression expressionForReferenceType =
                createComparisonExpressionForReferenceType(column, paramName, isEquals);

        JdbcNamedParameter referenceParameter = new JdbcNamedParameter();
        referenceParameter.setName(paramName);

        expressionForReferenceId.setRightExpression(referenceParameter);
        // замена старого параметризованного фильтра по Reference полю
        // (например, t.id = {0}) на рабочий
        // фильтр {например, t.id = 1 and t.id_type = 2 }
        Expression newReferenceExpression = null;
        if (isEquals) {
            newReferenceExpression =
                    new AndExpression(expressionForReferenceId, expressionForReferenceType);

        } else {
            newReferenceExpression =
                    new Parenthesis(new OrExpression(expressionForReferenceId, expressionForReferenceType));
        }
        return newReferenceExpression;
    }

    protected BinaryExpression createComparisonExpressionForReferenceType(Column column, String paramName, boolean isEquals) {
        BinaryExpression comparisonExpressionForReferenceType = null;
        if (isEquals) {
            comparisonExpressionForReferenceType = new EqualsTo();
        } else {
            comparisonExpressionForReferenceType = new NotEqualsTo();
        }
        Column typeColumn = createReferenceTypeColumn(column);

        comparisonExpressionForReferenceType.setLeftExpression(typeColumn);

        JdbcNamedParameter referenceTypeParameter = new JdbcNamedParameter();
        String referenceTypeParamName = createReferenceTypeColumn(paramName);
        referenceTypeParameter.setName(referenceTypeParamName);

        comparisonExpressionForReferenceType.setRightExpression(referenceTypeParameter);

        return comparisonExpressionForReferenceType;
    }

    protected Column createReferenceTypeColumn(Column column) {
        String typeColumnName = DaoUtils.unwrap(column.getColumnName()) + DomainObjectDao.REFERENCE_TYPE_POSTFIX;
        Column typeColumn = new Column(column.getTable(), typeColumnName);
        return typeColumn;
    }

    protected String createReferenceTypeColumn(String paramName) {
        return paramName + DomainObjectDao.REFERENCE_TYPE_POSTFIX;
    }

    protected BinaryExpression createReferenceTypeEqualsExpr(Column column, String paramName, boolean isEquals) {
        Column typeColumn = createReferenceTypeColumn(column);

        BinaryExpression referenceTypeEqualsExpression = null;
        if (isEquals) {
            referenceTypeEqualsExpression = new EqualsTo();
        } else {
            referenceTypeEqualsExpression = new NotEqualsTo();
        }
        referenceTypeEqualsExpression.setLeftExpression(typeColumn);

        JdbcNamedParameter referenceTypeParameter = new JdbcNamedParameter();
        String referenceTypeParamName = createReferenceTypeColumn(paramName);
        referenceTypeParameter.setName(referenceTypeParamName);

        referenceTypeEqualsExpression.setRightExpression(referenceTypeParameter);
        return referenceTypeEqualsExpression;
    }

    protected Expression connect(Expression first, Expression second, boolean connectWithAndInsteadOfOr) {
        return connectWithAndInsteadOfOr ? new AndExpression(first, second) : new OrExpression(first, second);
    }

    protected Expression inExpressionForSingleType(Column column, String paramName, boolean isNotIn) {
        JdbcNamedParameter param = new JdbcNamedParameter();
        param.setName(paramName);
        JdbcNamedParameter typeParam = new JdbcNamedParameter();
        typeParam.setName(paramName + DomainObjectDao.REFERENCE_TYPE_POSTFIX);
        InExpression idExpression = new InExpression(column, new ExpressionList(singletonList((Expression) param)));
        idExpression.setNot(isNotIn);
        Expression typeIdExpression = createReferenceTypeEqualsExpr(column, paramName, !isNotIn);
        Expression expr = isNotIn ? new OrExpression(idExpression, typeIdExpression) : new AndExpression(idExpression, typeIdExpression);
        return new Parenthesis(expr);
    }

}
