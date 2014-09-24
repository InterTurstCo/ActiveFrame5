package ru.intertrust.cm.core.dao.impl.sqlparser;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getFilterParameterPrefix;

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
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

/**
 * Заполняет ссылочные параметры фильтров в SQL запросе. Добавляет тип ссылочного поля в SQL запрос. Например, выполняет
 * следующую замену: t.id = {0} -> t.id = 1 and t.id_type = 2.
 * @author atsvetkov
 */
public class ReferenceFilterValuesProcessingVisitor extends BaseReferenceProcessingVisitor {

    private Map<String, String> replaceExpressions = new HashMap<>();

    protected List<? extends Filter> filterValues;

    private Map<String, FieldConfig> columnToConfigMap;
    
    public ReferenceFilterValuesProcessingVisitor(List<? extends Filter> filterValues, Map<String, FieldConfig> columnToConfigMap) {
        this.filterValues = filterValues;
        this.columnToConfigMap = columnToConfigMap;
    }

    public Map<String, String> getReplaceExpressions() {
        return replaceExpressions;
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        visitBinaryExpression(equalsTo);        
        processReferenceParametersInEqualsExpression(equalsTo, true);
    }


    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        visitBinaryExpression(notEqualsTo);
        processReferenceParametersInEqualsExpression(notEqualsTo, false);
    }

    
    @Override
    public void visit(InExpression inExpression) {
        inExpression.getLeftExpression().accept(this);
        inExpression.getRightItemsList().accept(this);
        boolean isInExpression = !inExpression.isNot();
        processReferenceParameterInsideInExpression(inExpression, isInExpression);
    }
    
    private void processReferenceParameterInsideInExpression(InExpression inExpression, boolean isEquals) {
        if (filterValues == null) {
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

                for (Filter filterValue : filterValues) {
                    String parameterPrefix = getFilterParameterPrefix(filterValue.getFilter());
                    if (inExpressionStr.indexOf(parameterPrefix) <= 0) {
                        continue;
                    }

                    for (Integer criterionKey : filterValue.getCriterionKeys()) {
                        Value criterionValue = filterValue.getCriterion(criterionKey);
                        if (inExpressionStr.indexOf(parameterPrefix + criterionKey) > 0) {
                            Expression finalExpression = null;
                            if (criterionValue instanceof ReferenceValue) {
                                finalExpression = createExpressionForReference(column, (ReferenceValue) criterionValue, isEquals);
                            } else if (criterionValue instanceof ListValue) {
                                ListValue listValue = (ListValue) criterionValue;
                                int index = 0;
                                for (Value value : listValue.getValues()) {
                                    ReferenceValue refValue = getReferenceValue(value);
                                    if (refValue == null) {
                                        continue;
                                    }

                                    finalExpression = updateFinalExpression(finalExpression, column, index, refValue, isEquals);
                                    index++;
                                }                                
                            }
                            
                            if (finalExpression != null) {
                                finalExpression = new Parenthesis(finalExpression);
                                replaceExpressions.put(inExpression.toString(), finalExpression.toString());
                                return;
                            }

                        }
                    }
                }

            }
        }
    }

    private ReferenceValue getReferenceValue(Value value) {
        ReferenceValue refValue = null;
        if (value instanceof ReferenceValue) {
            refValue = (ReferenceValue) value;

        } else if (value instanceof StringValue) {
            // ссылочные параметры могут передаваться в строковом виде.
            String strParamValue = ((StringValue) value).get();
            refValue = new ReferenceValue(new RdbmsId(strParamValue));
        }
        return refValue;
    }
    
    private void processReferenceParametersInEqualsExpression(BinaryExpression equalsTo, boolean isEquals) {
        if (filterValues == null) {
            return;
        }
        if (equalsTo.getLeftExpression() instanceof Column) {
            Column column = (Column) equalsTo.getLeftExpression();

            FieldConfig fieldConfig = columnToConfigMap.get(DaoUtils.unwrap(column.getColumnName().toLowerCase()));

            if (fieldConfig instanceof ReferenceFieldConfig) {
                
                String rightExpression = equalsTo.getRightExpression().toString();

                ReferenceValue referenceValue = null;
                
                for (Filter filterValue : filterValues) {
                    String parameterPrefix = getFilterParameterPrefix(filterValue.getFilter());
                    if (!rightExpression.startsWith(parameterPrefix)) {
                        continue;
                    }

                    for (Integer criterionKey : filterValue.getCriterionKeys()) {
                        Value criterionValue = filterValue.getCriterion(criterionKey);
                        if (criterionValue instanceof ReferenceValue && rightExpression.equals(parameterPrefix + criterionKey)) {
                            referenceValue = (ReferenceValue) criterionValue;
                            BinaryExpression newReferenceExpression = createFilledReferenceExpression(column, referenceValue, equalsTo, isEquals);
                            replaceExpressions.put(equalsTo.toString(), newReferenceExpression.toString());
                        }
                    }
                }
                
            }
        }
    }

}
