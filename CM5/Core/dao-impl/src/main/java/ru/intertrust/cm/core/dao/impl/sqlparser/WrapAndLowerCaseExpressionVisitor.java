package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SubSelect;

import static ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper.wrap;

/**
 * Реализация ExpressionVisitor для транформации sql-запросов: приведение к нижнему регистру и заключение в кавычки
 * имен таблиц и колонок
 * User: vmatsukevich
 * Date: 12/9/13
 * Time: 4:23 PM
 */
public class WrapAndLowerCaseExpressionVisitor extends BaseExpressionVisitor implements ExpressionVisitor {

    @Override
    public void visit(Function function) {
        if (function.getParameters() != null) {
            function.getParameters().accept(new WrapAndLowerCaseItemListVisitor());
        }
    }

    @Override
    public void visit(InExpression inExpression) {
        if (inExpression.getLeftExpression() != null) {
            inExpression.getLeftExpression().accept(this);
        }

        if (inExpression.getLeftItemsList() != null) {
            inExpression.getLeftItemsList().accept(new WrapAndLowerCaseItemListVisitor());
        }

        if (inExpression.getRightItemsList() != null) {
            inExpression.getRightItemsList().accept(new WrapAndLowerCaseItemListVisitor());
        }
    }

    @Override
    public void visit(Column column) {
        if (column.getColumnName() != null) {
            column.setColumnName(wrap(column.getColumnName().toLowerCase()));
        }
    }

    @Override
    protected void visitSubSelect(SubSelect subSelect) {
        if (subSelect.getPivot() != null) {
            subSelect.getPivot().accept(new WrapAndLowerCasePivotVisitor());
        }

        if (subSelect.getSelectBody() != null) {
            subSelect.getSelectBody().accept(new WrapAndLowerCaseSelectVisitor());
        }
    }

}
