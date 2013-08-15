package ru.intertrust.cm.core.dao.impl;

import java.io.StringReader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import ru.intertrust.cm.core.dao.exception.CollectionQueryException;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Парсер SQL запросов. Парсит только Select запросы. При передачи ему другого типа запроса выбрасывает исключение
 * CollectionQueryException.
 * @author atsvetkov
 */
public class SqlQueryParser {

    private Select selectStatement;

    public SqlQueryParser(String sqlSelectQuery) {
        CCJSqlParserManager pm = new CCJSqlParserManager();
        try {

            Statement statement = pm.parse(new StringReader(sqlSelectQuery));
            if (!statement.getClass().equals(Select.class)) {
                throw new CollectionQueryException("Collection sql query could be of type SELECT only");
            }
            this.selectStatement = (Select) statement;
        } catch (JSQLParserException e) {
            throw new FatalException("Sql query is not valid: " + sqlSelectQuery);
        }

    }

    public SelectBody getSelectBody() {
        return selectStatement.getSelectBody();
    }

}
