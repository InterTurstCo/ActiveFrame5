package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

/**
 * 
 * @author atsvetkov
 *
 */
public class CollectingSelectItemConfigVisitor extends CollectingColumnConfigVisitor{

    public CollectingSelectItemConfigVisitor(ConfigurationExplorer configurationExplorer, PlainSelect plainSelect) {
        super(configurationExplorer, plainSelect);
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);

        // Add column to config mapping for column alias
        if (selectExpressionItem.getAlias() != null && selectExpressionItem.getAlias().getName() != null &&
                selectExpressionItem.getExpression() instanceof Column) {
            Column column = (Column) selectExpressionItem.getExpression();
            String aliasName = DaoUtils.unwrap(Case.toLower(selectExpressionItem.getAlias().getName()));
            if (columnToConfigMapping.get(aliasName) == null) {
                columnToConfigMapping.put(aliasName, columnToConfigMapping.get(getColumnName(column)));
            }
        }
    }

    private String getColumnName(Column column) {
        return DaoUtils.unwrap(Case.toLower(column.getColumnName()));
    }
    
    @Override
    public void visit(Addition addition) {
        visitBinaryExpression(addition);
    }

    @Override
    public void visit(Division division) {
        visitBinaryExpression(division);
    }

    @Override
    public void visit(Multiplication multiplication) {
        visitBinaryExpression(multiplication);
    }

    @Override
    public void visit(Subtraction subtraction) {
        visitBinaryExpression(subtraction);
    }

    @Override
    public void visit(AndExpression andExpression) {
        
    }

    @Override
    public void visit(OrExpression orExpression) {
    }

    @Override
    public void visit(Between between) {
    }

    @Override
    public void visit(EqualsTo equalsTo) {
    }

    @Override
    public void visit(GreaterThan greaterThan) {
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
    }


    @Override
    public void visit(IsNullExpression isNullExpression) {
    }

    @Override
    public void visit(LikeExpression likeExpression) {
    }

    @Override
    public void visit(MinorThan minorThan) {
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
    }

    @Override
    public void visit(Matches matches) {
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
    }

}
