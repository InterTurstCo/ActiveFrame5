package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

/**
 * Реализация FromItemVisitor для транформации sql-запросов: приведение к
 * нижнему регистру и заключение в кавычки имен таблиц и колонок User:
 * vmatsukevich Date: 12/10/13 Time: 10:36 AM
 */
public class WrapAndLowerCaseFromItemVisitor extends FromItemVisitorAdapter {

    @Override
    public void visit(Table table) {
        if (table.getName() != null) {
            table.setName(DaoUtils.wrap(table.getName().toLowerCase()));
        }

        if (table.getPivot() != null) {
            table.getPivot().accept(new WrapAndLowerCasePivotVisitor());
        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        if (subSelect.getPivot() != null) {
            subSelect.getPivot().accept(new WrapAndLowerCasePivotVisitor());
        }

        if (subSelect.getSelectBody() != null) {
            subSelect.getSelectBody().accept(new WrapAndLowerCaseSelectVisitor());
        }
    }

    @Override
    public void visit(SubJoin subJoin) {
        if (subJoin.getPivot() != null) {

        }

        if (subJoin.getJoin() != null) {
            new WrapAndLowerCaseJoinVisitor().visit(subJoin.getJoin());
        }

        if (subJoin.getLeft() != null) {
            subJoin.getLeft().accept(new WrapAndLowerCaseFromItemVisitor());
        }
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        if (lateralSubSelect.getPivot() != null) {
            lateralSubSelect.getPivot().accept(new WrapAndLowerCasePivotVisitor());
        }
        lateralSubSelect.getSubSelect().accept(new WrapAndLowerCaseFromItemVisitor());
    }

}
