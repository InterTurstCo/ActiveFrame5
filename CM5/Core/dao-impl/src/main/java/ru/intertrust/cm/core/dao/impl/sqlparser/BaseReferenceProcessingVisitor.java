package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.HashMap;
import java.util.Map;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.WithinGroupExpression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.schema.Column;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
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

        } else {
            Expression expressionForReference = createExpressionForReference(column, paramName, isEquals);
            Expression leftExpression = new Parenthesis(finalExpression);
            Expression rightExpression = new Parenthesis(expressionForReference);

            if (isEquals) {
                finalExpression = new OrExpression(leftExpression, rightExpression);
            } else {
                finalExpression = new AndExpression(leftExpression, rightExpression);

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

    @Override
    public void visit(WithinGroupExpression wgexpr) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(UserVariable var) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(NumericBind bind) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(KeepExpression aexpr) {
        // TODO Auto-generated method stub

    }

}
