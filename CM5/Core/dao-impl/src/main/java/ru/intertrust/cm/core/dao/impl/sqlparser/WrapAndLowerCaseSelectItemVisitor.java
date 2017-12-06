package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.statement.select.*;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

/**
* Реализация SelectItemVisitor для транформации sql-запросов: приведение к нижнему регистру и заключение в кавычки
* имен таблиц и колонок
* User: vmatsukevich
* Date: 12/10/13
* Time: 10:34 AM
*/
class WrapAndLowerCaseSelectItemVisitor implements SelectItemVisitor {

    private FromItem fromItem;

    public WrapAndLowerCaseSelectItemVisitor() {
    }

    public WrapAndLowerCaseSelectItemVisitor(FromItem fromItem) {
        this.fromItem = fromItem;
    }

    @Override
    public void visit(AllColumns allColumns) {
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        if (allTableColumns.getTable() != null && allTableColumns.getTable().getName() != null) {
            if(fromItem != null && fromItem.getAlias() != null &&
                    allTableColumns.getTable().getName().equalsIgnoreCase(fromItem.getAlias().getName())) {
                // Don't wrap alias
                allTableColumns.getTable().setName(Case.toLower(allTableColumns.getTable().getName()));
            } else {
                allTableColumns.getTable().setName(DaoUtils.wrap(Case.toLower(allTableColumns.getTable().getName())));
            }
        }
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        if (selectExpressionItem.getExpression() != null) {
            selectExpressionItem.getExpression().accept(new WrapAndLowerCaseExpressionVisitor());
            if (selectExpressionItem.getAlias() != null && selectExpressionItem.getAlias().getName() != null) {
                selectExpressionItem.setAlias(new Alias(DaoUtils.wrap(Case.toLower(selectExpressionItem.getAlias().getName())), false));
            }
        }
    }
}
