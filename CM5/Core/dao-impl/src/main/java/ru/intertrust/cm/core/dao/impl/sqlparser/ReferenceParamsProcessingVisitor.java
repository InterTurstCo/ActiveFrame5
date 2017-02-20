package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.impl.CollectionsDaoImpl;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

/**
 * Заполняет ссылочные параметры в SQL запросе. Добавляет тип ссылочного поля в
 * SQL запрос. Например, выполняет следующую замену: t.id = {0} -> t.id = 1 and
 * t.id_type = 2.
 * @author atsvetkov
 */

public class ReferenceParamsProcessingVisitor extends BaseReferenceProcessingVisitor {
    private Map<String, String> replaceExpressions = new HashMap<>();

    protected List<? extends Value> params;

    private Map<String, FieldConfig> columnToConfigMap;

    public ReferenceParamsProcessingVisitor(List<? extends Value> params, Map<String, FieldConfig> columnToConfigMap) {
        this.params = params;
        this.columnToConfigMap = columnToConfigMap;
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
        if (params == null || params.isEmpty()) {
            return;
        }
        if (equalsTo.getLeftExpression() instanceof Column) {
            Column column = (Column) equalsTo.getLeftExpression();

            FieldConfig fieldConfig = columnToConfigMap.get(DaoUtils.unwrap(column.getColumnName().toLowerCase()));

            if (fieldConfig instanceof ReferenceFieldConfig) {

                String rightExpression = equalsTo.getRightExpression().toString();

                ReferenceValue referenceValue = null;
                if (rightExpression.indexOf(CollectionsDaoImpl.PARAM_NAME_PREFIX) > 0) {

                    Integer paramIndex = findParameterIndex(rightExpression);

                    if (params.get(paramIndex) instanceof ReferenceValue) {
                        referenceValue = (ReferenceValue) params.get(paramIndex);
                    } else if (params.get(paramIndex) instanceof StringValue) {
                        String strValue = ((StringValue) params.get(paramIndex)).get();
                        referenceValue = new ReferenceValue(new RdbmsId(strValue));
                    }
                    String paramName = CollectionsDaoImpl.JDBC_PARAM_PREFIX + paramIndex;
                    addParameters(paramName, referenceValue);
                    Expression newReferenceExpression = createFilledReferenceExpression(column, paramName, equalsTo, isEquals);
                    replaceExpressions.put(equalsTo.toString(), newReferenceExpression.toString());
                }
            }
        }
    }

    private void processReferenceParameterInsideInExpression(InExpression inExpression, boolean isEquals) {
        if (params == null) {
            return;
        }
        if (inExpression.getRightItemsList() instanceof SubSelect) {
            return;
        }
        if (inExpression.getLeftExpression() instanceof Column) {
            Column column = (Column) inExpression.getLeftExpression();

            FieldConfig fieldConfig = columnToConfigMap.get(DaoUtils.unwrap(column.getColumnName().toLowerCase()));

            if (fieldConfig instanceof ReferenceFieldConfig) {

                String inExpressionStr = inExpression.toString();

                ListValue listValue = null;
                if (inExpressionStr.indexOf(CollectionsDaoImpl.PARAM_NAME_PREFIX) > 0) {

                    Integer paramIndex = findParameterIndex(inExpressionStr);

                    Expression finalExpression = null;

                    if (params.get(paramIndex) instanceof ListValue) {
                        finalExpression = processListValue(isEquals, column, CollectionsDaoImpl.JDBC_PARAM_PREFIX, paramIndex, params.get(paramIndex));
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
    }

    private Integer findParameterIndex(String rightExpression) {
        int startParamNumberIndex =
                rightExpression.indexOf(CollectionsDaoImpl.PARAM_NAME_PREFIX)
                        + CollectionsDaoImpl.PARAM_NAME_PREFIX.length();
        int endParamNumberIndex =
                rightExpression.indexOf(CollectionsDaoImpl.END_PARAM_SIGN, startParamNumberIndex);
        String paramName = rightExpression.substring(startParamNumberIndex, endParamNumberIndex);
        Integer paramIndex = Integer.parseInt(paramName);
        return paramIndex;
    }

}
