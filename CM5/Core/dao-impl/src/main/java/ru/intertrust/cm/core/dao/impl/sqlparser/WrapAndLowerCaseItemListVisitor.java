package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
* Реализация ItemsListVisitor для транформации sql-запросов: приведение к нижнему регистру и заключение в кавычки
* имен таблиц и колонок
* User: vmatsukevich
* Date: 12/10/13
* Time: 10:34 AM
*/
class WrapAndLowerCaseItemListVisitor implements ItemsListVisitor {

    @Override
    public void visit(SubSelect subSelect) {
        if (subSelect.getSelectBody() != null) {
            subSelect.getSelectBody().accept(new WrapAndLowerCaseSelectVisitor());
        }

        if (subSelect.getPivot() != null) {
            subSelect.getPivot().accept(new WrapAndLowerCasePivotVisitor());
        }
    }

    @Override
    public void visit(ExpressionList expressionList) {
        if (expressionList.getExpressions() != null) {
            for (Expression expression : expressionList.getExpressions()) {
                expression.accept(new WrapAndLowerCaseExpressionVisitor());
            }
        }
    }

    @Override
    public void visit(MultiExpressionList multiExpressionList) {
        if (multiExpressionList.getExprList() != null) {
            for (ExpressionList expressionList : multiExpressionList.getExprList()) {
                visit(expressionList);
            }
        }
    }
}
