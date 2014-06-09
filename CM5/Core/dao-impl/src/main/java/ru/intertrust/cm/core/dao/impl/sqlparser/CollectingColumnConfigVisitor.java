package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.HashMap;
import java.util.Map;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;

/**
 * Визитор для поиска конфигурации полей в Where части SQL запроса и для заполнения фильтров для ссылочных
 * параметров.
 * @author atsvetkov
 */

public class CollectingColumnConfigVisitor extends BaseParamProcessingVisitor implements ExpressionVisitor, FromItemVisitor, SelectVisitor {

    protected ConfigurationExplorer configurationExplorer;
    protected PlainSelect plainSelect;

    protected String plainSelectQuery;

    protected Map<String, FieldConfig> whereColumnToConfigMapping = new HashMap<>();

    public Map<String, FieldConfig> getWhereColumnToConfigMapping() {
        return whereColumnToConfigMapping;
    }

    public String getModifiedQuery() {
        return plainSelectQuery;
    }

    public CollectingColumnConfigVisitor(ConfigurationExplorer configurationExplorer, PlainSelect plainSelect) {
        this.configurationExplorer = configurationExplorer;
        this.plainSelect = plainSelect;
        this.plainSelectQuery = plainSelect.toString();
    }

    @Override
    protected void visitSubSelect(SubSelect subSelect) {
        subSelect.getSelectBody().accept(this);
    }

    @Override
    public void visit(Column column) {
        collectWhereColumnConfigurations(column);
    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {

    }

    private void collectWhereColumnConfigurations(Column column) {
        FieldConfig fieldConfig =
                configurationExplorer.getFieldConfig(SqlQueryModifier.getDOTypeName(plainSelect, column, false),
                        column.getColumnName());
        whereColumnToConfigMapping.put(column.getColumnName().toLowerCase(), fieldConfig);
    }
    
}
