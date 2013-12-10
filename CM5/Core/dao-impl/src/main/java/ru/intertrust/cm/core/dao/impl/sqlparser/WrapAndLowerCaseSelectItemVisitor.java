package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;

import static ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper.wrap;

/**
* Реализация SelectItemVisitor для транформации sql-запросов: приведение к нижнему регистру и заключение в кавычки
* имен таблиц и колонок
* User: vmatsukevich
* Date: 12/10/13
* Time: 10:34 AM
*/
class WrapAndLowerCaseSelectItemVisitor implements SelectItemVisitor {

    @Override
    public void visit(AllColumns allColumns) {
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        if (allTableColumns.getTable() != null && allTableColumns.getTable().getName() != null) {
            allTableColumns.getTable().setName(wrap(allTableColumns.getTable().getName().toLowerCase()));
        }
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        if (selectExpressionItem.getExpression() != null) {
            selectExpressionItem.getExpression().accept(new WrapAndLowerCaseExpressionVisitor());
        }
    }
}
