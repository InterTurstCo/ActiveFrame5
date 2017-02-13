package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;

/**
 * Возвращает множество таблиц (типов ДО) в SQL запросе.
 * @author atsvetkov
 * @author oerentsov
 * 
 */
public class CollectDOTypesVisitor {

    private class CaseStatementSupportingTablesNamesFinder extends TablesNamesFinder {
        @Override
        public void visit(CaseExpression caseExpression) {
            safeAcceptThis(caseExpression.getSwitchExpression());
            safeAcceptThis(caseExpression.getElseExpression());
            for (Expression when : caseExpression.getWhenClauses()) {
                when.accept(this);
            }
        }

        @Override
        public void visit(WhenClause whenClause) {
            safeAcceptThis(whenClause.getWhenExpression());
            safeAcceptThis(whenClause.getThenExpression());
        }

        private void safeAcceptThis(Expression expression) {
            if (expression != null) {
                expression.accept(this);
            }
        }
    }

    private CaseStatementSupportingTablesNamesFinder tableNamesFinder = new CaseStatementSupportingTablesNamesFinder();

    private ConfigurationExplorer configurationExplorer;

    public CollectDOTypesVisitor(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Возвращает множество типов ДО в запросе.
     * @param selectBody
     * @return
     */
    public Set<String> getDOTypes(Select select) {
        HashSet<String> doTypes = new HashSet<String>();
        for (String tableName : tableNamesFinder.getTableList(select)) {
            tableName = stripQuotes(tableName);
            if (configurationExplorer.getConfig(DomainObjectTypeConfig.class, tableName) != null) {
                doTypes.add(tableName.toLowerCase());
            }
        }
        doTypes.addAll(getChildDOTypes(doTypes));
        return doTypes;
    }

    private Set<String> getChildDOTypes(Set<String> types) {
        Set<String> allChildren = new HashSet<>();
        for (String doType : types) {
            Collection<DomainObjectTypeConfig> childTypeConfigs = configurationExplorer.findChildDomainObjectTypes(doType, true);
            if (childTypeConfigs != null) {
                for (DomainObjectTypeConfig childConfig : childTypeConfigs) {
                    String name = childConfig.getName();
                    name = stripQuotes(name);
                    allChildren.add(name.toLowerCase());
                }
            }
        }
        return allChildren;
    }

    /**
     * Создан для удобства использования, когда нет распаршенного SQL запроса.
     * @param collectionQuery
     * @return
     */
    public Set<String> getDOTypes(String collectionQuery) {
        SqlQueryParser parser = new SqlQueryParser(collectionQuery);
        Select select = parser.getSelectStatement();
        return getDOTypes(select);
    }

    private String stripQuotes(String tableWholeName) {
        if (tableWholeName.startsWith("\"")) {
            tableWholeName = tableWholeName.substring(1, tableWholeName.length() - 1);
        }
        return tableWholeName;
    }

}
