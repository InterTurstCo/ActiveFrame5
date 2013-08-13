package ru.intertrust.cm.core.dao.api;

import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.FromItem;

/**
 * Парсер SQL (Select) запросов. 
 * @author atsvetkov
 *
 */
public interface SqlQueryParser {
    
    /**
     * Возвращает название таблицы после ключевого слова From.
     * @return
     */
    FromItem getFromItem();
    
    /**
     * Возвращает список возвращаемых полей в запросе. Список полей между ключевыми полями Select и From.
     * @return
     */
    List getSelectItems();
    
    /**
     * Возвращает значение Where фильтра.
     * @return
     */
    Expression getWhereExpression();
}
