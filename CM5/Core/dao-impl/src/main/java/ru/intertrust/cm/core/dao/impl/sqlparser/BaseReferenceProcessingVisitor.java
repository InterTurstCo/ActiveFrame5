package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
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

    protected BinaryExpression createComparisonExpressionForReferenceType(Column column,
            ReferenceValue referenceValue, boolean isEquals) {
        BinaryExpression comparisonExpressionForReferenceType = null;
        if (isEquals) {
            comparisonExpressionForReferenceType = new EqualsTo();
        } else {
            comparisonExpressionForReferenceType = new NotEqualsTo();
        }
        Column typeColumn = createReferenceTypeColumn(column);

        comparisonExpressionForReferenceType.setLeftExpression(typeColumn);

        long refTypeId = ((RdbmsId) referenceValue.get()).getTypeId();

        comparisonExpressionForReferenceType.setRightExpression(new LongValue(refTypeId + ""));

        return comparisonExpressionForReferenceType;
    }

    protected Column createReferenceTypeColumn(Column column) {
        String typeColumnName = DaoUtils.unwrap(column.getColumnName()) + DomainObjectDao.REFERENCE_TYPE_POSTFIX;
        Column typeColumn = new Column(column.getTable(), typeColumnName);
        return typeColumn;
    }
    
    protected BinaryExpression createFilledReferenceExpression(Column column, ReferenceValue referenceValue, BinaryExpression equalsTo, boolean isEquals) {
        BinaryExpression modifiedEqualsToForReferenceId = null;
        if (isEquals) {
            modifiedEqualsToForReferenceId = new EqualsTo();
        } else {
            modifiedEqualsToForReferenceId = new NotEqualsTo();
        }

        modifiedEqualsToForReferenceId.setLeftExpression(equalsTo.getLeftExpression());
        modifiedEqualsToForReferenceId.setRightExpression(equalsTo.getRightExpression());

        BinaryExpression equalsToForReferenceType =
                createComparisonExpressionForReferenceType(column, referenceValue, isEquals);

        long refId = ((RdbmsId) referenceValue.get()).getId();
        modifiedEqualsToForReferenceId.setRightExpression(new LongValue(refId + ""));
        // замена старого параметризованного фильтра по Reference полю (например, t.id = {0}) на рабочий
        // фильтр {например, t.id = 1 and t.id_type = 2 }
        BinaryExpression newReferenceExpression = null;
        if (isEquals) {
            newReferenceExpression =
                    new AndExpression(modifiedEqualsToForReferenceId, equalsToForReferenceType);

        } else {
            newReferenceExpression =
                    new OrExpression(modifiedEqualsToForReferenceId, equalsToForReferenceType);
        }
        return newReferenceExpression;
    }

    protected Expression updateFinalExpression(Expression finalExpression, Column column, int index, ReferenceValue refValue, boolean isEquals) {
        if (index == 0) {
            finalExpression = createExpressionForReference(column, refValue, isEquals);

        } else {
            Expression expressionForReference = createExpressionForReference(column, refValue, isEquals);
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

    protected Expression createExpressionForReference(Column column, ReferenceValue refValue, boolean isEquals) {
        BinaryExpression referenceIdEqualsExpression = createReferenceEqualsExpr(column, refValue, isEquals);
        BinaryExpression referenceTypeEqualsExpression = createReferenceTypeEqualsExpr(column, refValue, isEquals);
        if (isEquals) {
            return new AndExpression(referenceIdEqualsExpression, referenceTypeEqualsExpression);
        } else {
            return new OrExpression(referenceIdEqualsExpression, referenceTypeEqualsExpression);
        }
    }

    protected BinaryExpression createReferenceTypeEqualsExpr(Column column, ReferenceValue refValue, boolean isEquals) {
        Column typeColumn = createReferenceTypeColumn(column);

        BinaryExpression referenceTypeEqualsExpression = null;
        if (isEquals) {
            referenceTypeEqualsExpression = new EqualsTo();
        } else {
            referenceTypeEqualsExpression = new NotEqualsTo();
        }
        referenceTypeEqualsExpression.setLeftExpression(typeColumn);
        referenceTypeEqualsExpression.setRightExpression(new LongValue(((RdbmsId) refValue.get()).getTypeId()));
        return referenceTypeEqualsExpression;
    }

    protected BinaryExpression createReferenceEqualsExpr(Column column, ReferenceValue refValue, boolean isEquals) {
        BinaryExpression referenceIdEqualsExpression = null;
        if (isEquals) {
            referenceIdEqualsExpression = new EqualsTo();
        } else {
            referenceIdEqualsExpression = new NotEqualsTo();
        }
        referenceIdEqualsExpression.setLeftExpression(column);
        referenceIdEqualsExpression.setRightExpression(new LongValue(((RdbmsId) refValue.get()).getId()));
        return referenceIdEqualsExpression;
    }

}
