package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;

/**
 * Реализация SelectVisitor для транформации sql-запросов: приведение к нижнему регистру и заключение в кавычки
 * имен таблиц и колонок
 * User: vmatsukevich
 * Date: 12/10/13
 * Time: 10:13 AM
 */
public class WrapAndLowerCaseSelectVisitor implements SelectVisitor {

    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getSelectItems() != null) {
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                selectItem.accept(new WrapAndLowerCaseSelectItemVisitor(plainSelect.getFromItem()));
            }
        }

        if (plainSelect.getFromItem() != null) {
            plainSelect.getFromItem().accept(new WrapAndLowerCaseFromItemVisitor());
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                new WrapAndLowerCaseJoinVisitor().visit(join);
            }
        }

        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(new WrapAndLowerCaseExpressionVisitor());
        }

        if (plainSelect.getDistinct() != null && plainSelect.getDistinct().getOnSelectItems() != null) {
            for (SelectItem selectItem : plainSelect.getDistinct().getOnSelectItems()) {
                selectItem.accept(new WrapAndLowerCaseSelectItemVisitor(plainSelect.getFromItem()));
            }
        }

        if (plainSelect.getGroupByColumnReferences() != null) {
            for (Expression expression : plainSelect.getGroupByColumnReferences()) {
                expression.accept(new WrapAndLowerCaseExpressionVisitor());
            }
        }

        if (plainSelect.getOrderByElements() != null) {
            for (OrderByElement orderByElement : plainSelect.getOrderByElements()) {
                if (orderByElement.getExpression() != null) {
                    orderByElement.getExpression().accept(new WrapAndLowerCaseExpressionVisitor());
                }
            }
        }

        if (plainSelect.getHaving() != null) {
            plainSelect.getHaving().accept(new WrapAndLowerCaseExpressionVisitor());
        }

        if (plainSelect.getInto() != null) {

        }
    }

    @Override
    public void visit(SetOperationList setOperationList) {
        if (setOperationList.getPlainSelects() != null) {
            for (PlainSelect plainSelect : setOperationList.getPlainSelects()) {
                plainSelect.accept(this);
            }
        }

        if (setOperationList.getOrderByElements() != null) {
            for (OrderByElement orderByElement : setOperationList.getOrderByElements()) {
                orderByElement.getExpression().accept(new WrapAndLowerCaseExpressionVisitor());
            }
        }
    }

    @Override
    public void visit(WithItem withItem) {
        if (withItem.getSelectBody() != null) {
            withItem.getSelectBody().accept(this);
        }

        if (withItem.getWithItemList() != null) {
            for (SelectItem selectItem : withItem.getWithItemList()) {
                selectItem.accept(new WrapAndLowerCaseSelectItemVisitor());
            }
        }
    }

}
