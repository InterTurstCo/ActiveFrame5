package ru.intertrust.cm.core.dao.impl.sqlparser;

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
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

/**
 * Реализация StatementVisitor для транформации sql-запросов: приведение к
 * нижнему регистру и заключение в кавычки имен таблиц и колонок User:
 * vmatsukevich Date: 12/9/13 Time: 4:23 PM
 */
public class WrapAndLowerCaseStatementVisitor implements StatementVisitor {

    @Override
    public void visit(Select select) {
        if (select.getWithItemsList() != null) {
            for (WithItem withItem : select.getWithItemsList()) {
                withItem.getSelectBody().accept(new WrapAndLowerCaseSelectVisitor());
            }
        }

        select.getSelectBody().accept(new WrapAndLowerCaseSelectVisitor());
    }

    @Override
    public void visit(Delete delete) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(Update update) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(Insert insert) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(Replace replace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(Drop drop) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(Truncate truncate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(CreateIndex createIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(CreateTable createTable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(CreateView createView) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(Alter alter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(Statements stmts) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(Execute execute) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(SetStatement set) {
        // TODO Auto-generated method stub

    }
}
