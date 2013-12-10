package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;

/**
 * Реализация PivotVisitor для транформации sql-запросов: приведение к нижнему регистру и заключение в кавычки
 * имен таблиц и колонок
 * User: vmatsukevich
 * Date: 12/10/13
 * Time: 10:45 AM
 */
public class WrapAndLowerCasePivotVisitor implements PivotVisitor {

    @Override
    public void visit(Pivot pivot) {
        visitPivot(pivot);
    }

    @Override
    public void visit(PivotXml pivotXml) {
        visitPivot(pivotXml);

        if (pivotXml.getInSelect() != null) {
            pivotXml.getInSelect().accept(new WrapAndLowerCaseSelectVisitor());
        }
    }

    private void visitPivot(Pivot pivot) {
        if (pivot.getForColumns() != null) {
            for (Column column : pivot.getForColumns()) {
                column.accept(new WrapAndLowerCaseExpressionVisitor());
            }
        }

        if (pivot.getFunctionItems() != null) {
            for (FunctionItem functionItem : pivot.getFunctionItems()) {
                if (functionItem.getFunction() != null) {
                    functionItem.getFunction().accept(new WrapAndLowerCaseExpressionVisitor());
                }
            }
        }

        if (pivot.getSingleInItems() != null) {
            for (SelectExpressionItem selectExpressionItem: pivot.getSingleInItems()) {
                selectExpressionItem.accept(new WrapAndLowerCaseSelectItemVisitor());
            }
        }

        if (pivot.getMultiInItems() != null) {
            for (ExpressionListItem expressionListItem : pivot.getMultiInItems()) {
                if (expressionListItem.getExpressionList() != null) {
                    expressionListItem.getExpressionList().accept(new WrapAndLowerCaseItemListVisitor());
                }
            }
        }
    }
}
