package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Join;

/**
 * Выполняет трансформацию join-выражения: приведение к нижнему регистру и заключение в кавычки
 * имен таблиц и колонок
 * User: vmatsukevich
 * Date: 12/10/13
 * Time: 11:33 AM
 */
public class WrapAndLowerCaseJoinVisitor {

    public void visit(Join join) {
        if (join.getOnExpression() != null) {
            join.getOnExpression().accept(new WrapAndLowerCaseExpressionVisitor());
        }

        if (join.getRightItem() != null) {
            join.getRightItem().accept(new WrapAndLowerCaseFromItemVisitor());
        }

        if (join.getUsingColumns() != null) {
            for (Column column : join.getUsingColumns()) {
                column.accept(new WrapAndLowerCaseExpressionVisitor());
            }
        }
    }
}
