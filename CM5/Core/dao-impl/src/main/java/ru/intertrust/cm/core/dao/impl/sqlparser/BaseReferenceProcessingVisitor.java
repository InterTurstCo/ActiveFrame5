package ru.intertrust.cm.core.dao.impl.sqlparser;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

/**
 * 
 * @author atsvetkov
 * 
 */
public class BaseReferenceProcessingVisitor extends BaseParamProcessingVisitor {

    Map<String, Object> jdbcParameters = new HashMap<>();

    public Map<String, Object> getJdbcParameters() {
        return jdbcParameters;
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

    protected String createParamName(Column column, String paramSuffix) {
        return DaoUtils.unwrap(column.getColumnName()) + paramSuffix;
    }

    protected Column createReferenceTypeColumn(Column column) {
        String typeColumnName = DaoUtils.unwrap(column.getColumnName()) + DomainObjectDao.REFERENCE_TYPE_POSTFIX;
        Column typeColumn = new Column(column.getTable(), typeColumnName);
        return typeColumn;
    }

    protected String createReferenceTypeColumn(String paramName) {
        return paramName + DomainObjectDao.REFERENCE_TYPE_POSTFIX;
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

    protected Expression updateFinalExpression(Expression finalExpression, Column column, int index, String paramName, boolean isEquals) {
        if (index == 0) {
            finalExpression = createExpressionForReference(column, paramName, isEquals);
            if (!isEquals) {
                finalExpression = new Parenthesis(finalExpression);
            }

        } else {
            Expression expressionForReference = createExpressionForReference(column, paramName, isEquals);
            Expression leftExpression = finalExpression;
            Expression rightExpression = expressionForReference;

            if (isEquals) {
                finalExpression = new OrExpression(leftExpression, rightExpression);
            } else {
                finalExpression = new AndExpression(leftExpression, new Parenthesis(rightExpression));

            }

        }
        return finalExpression;
    }

    protected Expression createExpressionForReference(Column column, String paramName, boolean isEquals) {
        BinaryExpression referenceIdEqualsExpression = createReferenceEqualsExpr(column, paramName, isEquals);
        BinaryExpression referenceTypeEqualsExpression = createReferenceTypeEqualsExpr(column, paramName, isEquals);
        if (isEquals) {
            return new AndExpression(referenceIdEqualsExpression, referenceTypeEqualsExpression);
        } else {
            return new OrExpression(referenceIdEqualsExpression, referenceTypeEqualsExpression);
        }
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

    protected BinaryExpression createReferenceEqualsExpr(Column column, String paramName, boolean isEquals) {
        BinaryExpression referenceIdEqualsExpression = null;
        if (isEquals) {
            referenceIdEqualsExpression = new EqualsTo();
        } else {
            referenceIdEqualsExpression = new NotEqualsTo();
        }
        referenceIdEqualsExpression.setLeftExpression(column);

        JdbcNamedParameter referenceParameter = new JdbcNamedParameter();
        String referenceParamName = paramName;
        referenceParameter.setName(referenceParamName);

        referenceIdEqualsExpression.setRightExpression(referenceParameter);
        return referenceIdEqualsExpression;
    }

    protected void addParameters(String paramName, ReferenceValue referenceValue) {
        String referenceParamName = paramName;
        String referenceTypeParamName = paramName + DomainObjectDao.REFERENCE_TYPE_POSTFIX;

        if (referenceValue == null || referenceValue.get() == null) {
            throw new IllegalArgumentException("Reference value passed as a parameter is null or empty, param name: " + paramName);
        }
        long refTypeId = ((RdbmsId) referenceValue.get()).getTypeId();

        jdbcParameters.put(referenceParamName, ((RdbmsId) referenceValue.get()).getId());
        jdbcParameters.put(referenceTypeParamName, refTypeId);
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

    protected Expression processListValue(boolean isIn, Column column, String baseParamName, Integer criterionKey, Value<?> criterionValue) {
        ListValue listValue = (ListValue) criterionValue;
        HashMap<Long, List<Long>> idsByType = new HashMap<Long, List<Long>>();
        for (Value<?> value : listValue.getValues()) {
            ReferenceValue refValue = ReferenceFilterUtility.getReferenceValue(value);
            if (refValue == null || refValue.get() == null) {
                continue;
            }
            RdbmsId id = (RdbmsId) refValue.get();
            long type = id.getTypeId();
            if (!idsByType.containsKey(type)) {
                idsByType.put(type, new ArrayList<Long>());
            }
            idsByType.get(type).add(id.getId());
        }

        Expression finalExpression = null;
        int index = 0;
        for (Map.Entry<Long, List<Long>> e : idsByType.entrySet()) {
            String paramName =
                    new StringBuilder().append(baseParamName).append(criterionKey).append("_")
                            .append(index).toString();

            addParameters(paramName, e.getKey(), e.getValue());
            finalExpression = finalExpression == null ? inExpressionForSingleType(column, paramName, !isIn) :
                    connect(finalExpression, inExpressionForSingleType(column, paramName, !isIn), !isIn);
            index++;
        }
        return finalExpression;
    }

    protected void addParameters(String paramName, Long type, List<Long> ids) {
        String referenceParamName = paramName;
        String referenceTypeParamName = paramName + DomainObjectDao.REFERENCE_TYPE_POSTFIX;
        jdbcParameters.put(referenceParamName, ids);
        jdbcParameters.put(referenceTypeParamName, type);
    }

}
