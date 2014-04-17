package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.impl.CollectionsDaoImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.REFERENCE_TYPE_POSTFIX;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getFilterParameterPrefix;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.generateReferenceTypeParameter;

/**
 * Заполняет ссылочные параметры фильтров в SQL запросе. Добавляет тип ссылочного поля в SQL запрос. Например, выполняет
 * следующую замену: t.id = {0} -> t.id = 1 and t.id_type = 2.
 * @author atsvetkov
 */
public class ReferenceFilterValuesProcessingVisitor extends BaseExpressionVisitor {

    private Map<String, String> replaceExpressions = new HashMap<>();

    protected List<? extends Filter> filterValues;

    public ReferenceFilterValuesProcessingVisitor(List<? extends Filter> filterValues) {
        this.filterValues = filterValues;
    }

    public Map<String, String> getReplaceExpressions() {
        return replaceExpressions;
    }

    @Override
    public void visit(Function function) {

    }

    @Override
    public void visit(SignedExpression signedExpression) {

    }

    @Override
    public void visit(EqualsTo equalsTo) {
        visitBinaryExpression(equalsTo);        
        processReferenceFilterValues(equalsTo, true);
    }

    @Override
    public void visit(InExpression inExpression) {

    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        visitBinaryExpression(notEqualsTo);
        processReferenceFilterValues(notEqualsTo, false);
    }

    @Override
    public void visit(Column column) {
    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {

    }

    @Override
    protected void visitSubSelect(SubSelect subSelect) {
    }


    private void processReferenceFilterValues(BinaryExpression binaryExpression, boolean isEquals) {
        if (filterValues == null) {
            return;
        }

        if (!(binaryExpression.getLeftExpression() instanceof Column)) {
            return;
        }

        Column column = (Column) binaryExpression.getLeftExpression();

        String rightExpression = binaryExpression.getRightExpression().toString();
        if (!rightExpression.startsWith(CollectionsDaoImpl.PARAM_NAME_PREFIX)) {
            return;
        }

        for (Filter filterValue : filterValues) {
            String parameterPrefix = getFilterParameterPrefix(filterValue.getFilter());
            if (!rightExpression.startsWith(parameterPrefix)) {
                continue;
            }

            for (Integer criterionKey : filterValue.getCriterionKeys()) {
                Value criterionValue = filterValue.getCriterion(criterionKey);
                if (criterionValue instanceof ReferenceValue && rightExpression.equals(parameterPrefix + criterionKey)) {
                    addReplaceExpression(binaryExpression, column, (ReferenceValue) criterionValue, isEquals);
                }
            }
        }
    }

    private void addReplaceExpression(BinaryExpression binaryExpression, Column column, ReferenceValue referenceValue, boolean isEquals) {
        BinaryExpression equalsToForReferenceType =
                createComparisonExpressionForReferenceType(column, referenceValue, isEquals);

        // замена старого параметризованного фильтра по Reference полю (например, t.id = {0}) на рабочий
        // фильтр {например, t.id = 1 and t.id_type = 2 }
        StringBuffer typeExpression = new StringBuffer(binaryExpression.toString());
        if (isEquals) {
            typeExpression.append(" and ");
        } else {
            typeExpression.append(" or ");
        }

        typeExpression.append(binaryExpression.getLeftExpression() + REFERENCE_TYPE_POSTFIX);

        if (isEquals) {
            typeExpression.append(" = ");
        } else {
            typeExpression.append(" <> ");
        }

        typeExpression.append(binaryExpression.getRightExpression() + REFERENCE_TYPE_POSTFIX);

        replaceExpressions.put(binaryExpression.toString(), typeExpression.toString());
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
        String typeColumnName = generateReferenceTypeParameter(column.getColumnName());
        Column typeColumn = new Column(column.getTable(), typeColumnName);

        comparisonExpressionForReferenceType.setLeftExpression(typeColumn);

        long refTypeId = ((RdbmsId) referenceValue.get()).getTypeId();

        comparisonExpressionForReferenceType.setRightExpression(new LongValue(refTypeId + ""));

        return comparisonExpressionForReferenceType;
    }
    
}
