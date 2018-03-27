package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

/**
 * Реализация StatementVisitor для транформации sql-запросов: приведение к
 * нижнему регистру и заключение в кавычки имен таблиц и колонок User:
 * vmatsukevich Date: 12/9/13 Time: 4:23 PM
 */
public class WrapAndLowerCaseStatementVisitor extends BasicVisitor implements StatementVisitor {

    @Override
    public void visit(Table table) {
        if (table.getName() != null) {
            table.setName(DaoUtils.wrap(Case.toLower(table.getName())));
        }
        super.visit(table);
    }

    @Override
    public void visit(Column column) {
        if (column.getColumnName() != null) {
            column.setColumnName(DaoUtils.wrap(Case.toLower(column.getColumnName())));
        }
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        if (selectExpressionItem.getExpression() != null) {
            selectExpressionItem.getExpression().accept(this);
            if (selectExpressionItem.getAlias() != null && selectExpressionItem.getAlias().getName() != null) {
                selectExpressionItem.setAlias(new Alias(DaoUtils.wrap(Case.toLower(selectExpressionItem.getAlias().getName())), false));
            }
        }
    }
}
