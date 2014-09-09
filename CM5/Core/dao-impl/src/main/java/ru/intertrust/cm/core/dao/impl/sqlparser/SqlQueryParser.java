package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import ru.intertrust.cm.core.dao.exception.CollectionQueryException;
import ru.intertrust.cm.core.model.FatalException;

import java.io.StringReader;

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
            int index = sqlSelectQuery.indexOf("value");
            if (index == 0 || sqlSelectQuery.charAt(index - 1) != '\"') {
                throw new FatalException("Sql query contains keyword 'value' that should be wrapped in parentheses. Query:\n" + sqlSelectQuery, e);
            } else {
                throw new FatalException("Sql query is not valid. Probably it contains some keyword used as column name, alias etc. " +
                        "In such cases keywords should be wrapped in parentheses. Query:\n" + sqlSelectQuery, e);
            }
        }

    }

    public SelectBody getSelectBody() {
        return selectStatement.getSelectBody();
    }

}
