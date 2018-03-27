package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.List;

import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.WindowElement;
import net.sf.jsqlparser.expression.WindowRange;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.First;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;
import net.sf.jsqlparser.statement.select.PivotVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.Skip;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public class BasicVisitor extends ExpressionVisitorAdapter implements ExpressionVisitor, FromItemVisitor, ItemsListVisitor, OrderByVisitor, PivotVisitor,
        SelectItemVisitor, SelectVisitor, StatementVisitor {

    public BasicVisitor() {
        setSelectVisitor(this);
    }

    @Override
    public void visit(Table tableName) {
        if (tableName.getPivot() != null) {
            tableName.getPivot().accept(this);
        }
    }

    @Override
    public void visit(SubJoin subjoin) {
        if (subjoin.getJoin() != null) {
            visit(subjoin.getJoin());
        }
        if (subjoin.getLeft() != null) {
            subjoin.getLeft().accept(this);
        }
        if (subjoin.getPivot() != null) {
            subjoin.getPivot().accept(this);
        }
    }

    protected void visit(Join join) {
        if (join.getOnExpression() != null) {
            join.getOnExpression().accept(this);
        }
        if (join.getRightItem() != null) {
            join.getRightItem().accept(this);
        }
        if (join.getUsingColumns() != null) {
            for (Column column : join.getUsingColumns()) {
                column.accept(this);
            }
        }
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        if (lateralSubSelect.getPivot() != null) {
            lateralSubSelect.getPivot().accept(this);
        }
        if (lateralSubSelect.getSubSelect() != null) {
            this.visit(lateralSubSelect.getSubSelect());
        }
    }

    @Override
    public void visit(ValuesList valuesList) {
        if (valuesList.getMultiExpressionList() != null) {
            valuesList.getMultiExpressionList().accept(this);
        }
        if (valuesList.getPivot() != null) {
            valuesList.getMultiExpressionList().accept(this);
        }
    }

    @Override
    public void visit(TableFunction tableFunction) {
        if (tableFunction.getFunction() != null) {
            tableFunction.getFunction().accept(this);
        }
        if (tableFunction.getPivot() != null) {
            tableFunction.getPivot().accept(this);
        }
    }

    @Override
    public void visit(OrderByElement orderBy) {
        orderBy.getExpression().accept(this);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        Distinct distinct = plainSelect.getDistinct();
        if (distinct != null) {
            List<SelectItem> selectItems = distinct.getOnSelectItems();
            if (selectItems != null) {
                visitSelectItems(selectItems);
            }
        }

        First first = plainSelect.getFirst();
        if (first != null) {
            if (first.getJdbcParameter() != null) {
                first.getJdbcParameter().accept(this);
            }
        }

        if (plainSelect.getForUpdateTable() != null) {
            plainSelect.getForUpdateTable().accept(this);
        }

        if (plainSelect.getFromItem() != null) {
            plainSelect.getFromItem().accept(this);
        }

        if (plainSelect.getGroupByColumnReferences() != null) {
            for (Expression expression : plainSelect.getGroupByColumnReferences()) {
                expression.accept(this);
            }
        }

        if (plainSelect.getHaving() != null) {
            plainSelect.getHaving().accept(this);
        }

        if (plainSelect.getIntoTables() != null) {
            for (Table table : plainSelect.getIntoTables()) {
                table.accept(this);
            }
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                visit(join);
            }
        }

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(this);
        }

        if (plainSelect.getOracleHint() != null) {
            plainSelect.getOracleHint().accept(this);
        }

        if (plainSelect.getOrderByElements() != null) {
            visitOrderByElements(plainSelect.getOrderByElements());
        }

        if (plainSelect.getSelectItems() != null) {
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                selectItem.accept(this);
            }
        }

        Skip skip = plainSelect.getSkip();
        if (skip != null) {
            if (skip.getJdbcParameter() != null) {
                skip.getJdbcParameter().accept(this);
            }
        }

        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(this);
        }
    }

    protected void visitSelectItems(List<SelectItem> selectItems) {
        for (SelectItem selectItem : selectItems) {
            selectItem.accept(this);
        }
    }

    protected void visitOrderByElements(List<OrderByElement> orderByElements) {
        for (OrderByElement orderByElement : orderByElements) {
            orderByElement.accept(this);
        }
    }

    @Override
    public void visit(SetOperationList setOpList) {
        if (setOpList.getOrderByElements() != null) {
            visitOrderByElements(setOpList.getOrderByElements());
        }

        if (setOpList.getSelects() != null) {
            for (SelectBody selectBody : setOpList.getSelects()) {
                selectBody.accept(this);
            }
        }
    }

    @Override
    public void visit(WithItem withItem) {
        if (withItem.getWithItemList() != null) {
            visitSelectItems(withItem.getWithItemList());
        }

        if (withItem.getSelectBody() != null) {
            withItem.getSelectBody().accept(this);
        }
    }

    @Override
    public void visit(Select select) {
        if (select.getWithItemsList() != null) {
            for (WithItem withItem : select.getWithItemsList()) {
                withItem.accept(this);
            }
        }
        if (select.getSelectBody() != null) {
            select.getSelectBody().accept(this);
        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        if (subSelect.getWithItemsList() != null) {
            for (WithItem item : subSelect.getWithItemsList()) {
                item.accept(this);
            }
        }

        if (subSelect.getSelectBody() != null) {
            subSelect.getSelectBody().accept(this);
        }

        if (subSelect.getPivot() != null) {
            subSelect.getPivot().accept(this);
        }
    }

    @Override
    public void visit(AnalyticExpression expr) {
        if (expr.getExpression() != null) {
            expr.getExpression().accept(this);
        }

        if (expr.getPartitionExpressionList() != null) {
            expr.getPartitionExpressionList().accept(this);
        }

        if (expr.getDefaultValue() != null) {
            expr.getDefaultValue().accept(this);
        }

        if (expr.getOffset() != null) {
            expr.getOffset().accept(this);
        }

        if (expr.getKeep() != null) {
            expr.getKeep().accept(this);
        }

        if (expr.getOrderByElements() != null) {
            visitOrderByElements(expr.getOrderByElements());
        }

        WindowElement windowElement = expr.getWindowElement();
        if (windowElement != null) {
            WindowRange windowRange = windowElement.getRange();
            if (windowRange != null) {
                if (windowRange.getStart() != null) {
                    windowRange.getStart().getExpression().accept(this);
                }

                if (windowRange.getEnd() != null) {
                    windowRange.getEnd().getExpression().accept(this);
                }
            }
            if (windowElement.getOffset() != null) {
                windowElement.getOffset().getExpression().accept(this);
            }
        }
    }

    @Override
    public void visit(CaseExpression expr) {
        if (expr.getSwitchExpression() != null) {
            expr.getSwitchExpression().accept(this);
        }
        if (expr.getWhenClauses() != null) {
            for (Expression x : expr.getWhenClauses()) {
                x.accept(this);
            }
        }
        if (expr.getElseExpression() != null) {
            expr.getElseExpression().accept(this);
        }
    }

    @Override
    public void visit(Delete delete) {

    }

    @Override
    public void visit(Update update) {

    }

    @Override
    public void visit(Insert insert) {

    }

    @Override
    public void visit(Replace replace) {

    }

    @Override
    public void visit(Drop drop) {

    }

    @Override
    public void visit(Truncate truncate) {

    }

    @Override
    public void visit(CreateIndex createIndex) {

    }

    @Override
    public void visit(CreateTable createTable) {

    }

    @Override
    public void visit(CreateView createView) {

    }

    @Override
    public void visit(Alter alter) {

    }

    @Override
    public void visit(Statements stmts) {

    }

    @Override
    public void visit(Execute execute) {

    }

    @Override
    public void visit(SetStatement set) {

    }

    @Override
    public void visit(Merge merge) {

    }
}
