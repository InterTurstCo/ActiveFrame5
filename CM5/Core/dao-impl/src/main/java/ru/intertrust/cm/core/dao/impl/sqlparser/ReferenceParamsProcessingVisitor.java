package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.impl.CollectionsDaoImpl;

/**
 * Заполняет ссылочные параметры в SQL запросе. Добавляет тип ссылочного поля в SQL запрос. Например, выполняет
 * следующую замену: t.id = {0} -> t.id = 1 and t.id_type = 2.
 * @author atsvetkov
 */
public class ReferenceParamsProcessingVisitor extends BaseParamProcessingVisitor {

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


    private void processReferenceParameters(BinaryExpression equalsTo, boolean isEquals) {
        if (params == null) {
            return;
        }
        if (equalsTo.getLeftExpression() instanceof Column) {
            Column column = (Column) equalsTo.getLeftExpression();

            FieldConfig fieldConfig = columnToConfigMap.get(column.getColumnName().toLowerCase());

            if (fieldConfig instanceof ReferenceFieldConfig) {

                BinaryExpression modifiedEqualsToForReferenceId = null;
                if (isEquals) {
                    modifiedEqualsToForReferenceId = new EqualsTo();
                } else {
                    modifiedEqualsToForReferenceId = new NotEqualsTo();
                }

                modifiedEqualsToForReferenceId.setLeftExpression(equalsTo.getLeftExpression());
                modifiedEqualsToForReferenceId.setRightExpression(equalsTo.getRightExpression());

                String rightExpression = modifiedEqualsToForReferenceId.getRightExpression().toString();

                ReferenceValue referenceValue = null;
                if (rightExpression.indexOf(CollectionsDaoImpl.PARAM_NAME_PREFIX) > 0) {

                    Integer paramIndex = findParameterIndex(rightExpression);

                    if (params.get(paramIndex) instanceof ReferenceValue) {
                        referenceValue = (ReferenceValue) params.get(paramIndex);
                    } else if (params.get(paramIndex) instanceof StringValue) {
                        String strValue = ((StringValue) params.get(paramIndex)).get();
                        referenceValue = new ReferenceValue(new RdbmsId(strValue));
                    }

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

                    replaceExpressions.put(equalsTo.toString(), newReferenceExpression.toString());
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

    private BinaryExpression createComparisonExpressionForReferenceType(Column column,
            ReferenceValue referenceValue, boolean isEquals) {
        BinaryExpression comparisonExpressionForReferenceType = null;
        if (isEquals) {
            comparisonExpressionForReferenceType = new EqualsTo();
        } else {
            comparisonExpressionForReferenceType = new NotEqualsTo();
        }
        String typeColumnName = column.getColumnName() + DomainObjectDao.REFERENCE_TYPE_POSTFIX;
        Column typeColumn = new Column(column.getTable(), typeColumnName);

        comparisonExpressionForReferenceType.setLeftExpression(typeColumn);

        long refTypeId = ((RdbmsId) referenceValue.get()).getTypeId();

        comparisonExpressionForReferenceType.setRightExpression(new LongValue(refTypeId + ""));

        return comparisonExpressionForReferenceType;
    }
    
}
