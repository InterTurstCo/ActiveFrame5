package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class FromItemAccessor {

    private Join join;

    private PlainSelect select;

    @Override
    public String toString() {
        return getFromItem().toString();
    }

    public FromItemAccessor(Join join) {
        this.join = join;
    }

    public FromItemAccessor(PlainSelect select) {
        this.select = select;
    }

    public FromItem getFromItem() {
        if (select != null) {
            return select.getFromItem();
        } else {
            return join.getRightItem();
        }
    }

    public void setFromItem(FromItem item) {
        if (select != null) {
            select.setFromItem(item);
        } else {
            join.setRightItem(item);
        }
    }

    public Expression getCondition() {
        if (select != null) {
            return null;
        } else {
            return join.getOnExpression();
        }
    }

    public boolean isNaturalJoin() {
        if (select != null) {
            return false;
        } else {
            return join.isNatural();
        }
    }
}
