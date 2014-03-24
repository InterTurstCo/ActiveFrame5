package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

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
    public void visit(SignedExpression signedExpression) {

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
            column.setColumnName(DaoUtils.wrap(column.getColumnName().toLowerCase()));
        }
    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {

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
