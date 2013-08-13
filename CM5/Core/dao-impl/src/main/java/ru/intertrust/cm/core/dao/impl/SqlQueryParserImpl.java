package ru.intertrust.cm.core.dao.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import ru.intertrust.cm.core.dao.api.SqlQueryParser;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Реализация SQL Парсера Select запросов.
 * @author atsvetkov
 *
 */
public class SqlQueryParserImpl implements SqlQueryParser {

    private Select statement; 
    
    public SqlQueryParserImpl(String sqlSelectQuery) {
        CCJSqlParserManager pm = new CCJSqlParserManager();
        try {
            statement = (Select)pm.parse(new StringReader(sqlSelectQuery));
        } catch (JSQLParserException e) {
            throw new FatalException("Sql query is not valid: " + sqlSelectQuery);
        }
                
    }

    private boolean isValidStatement() {
        return statement != null && statement.getSelectBody() != null;
    }

    @Override
    public FromItem getFromItem() {
        if (isValidStatement()) {
            return  ((PlainSelect) statement.getSelectBody()).getFromItem();
        }
        return null;
    }

    @Override
    public List getSelectItems() {
        if (isValidStatement()) {
            return ((PlainSelect) statement.getSelectBody()).getSelectItems();
        }
        return new ArrayList();
    }

    @Override
    public Expression getWhereExpression() {
        if (isValidStatement()) {
            return ((PlainSelect) statement.getSelectBody()).getWhere();
        }
        return null;
    }
    
    

}
