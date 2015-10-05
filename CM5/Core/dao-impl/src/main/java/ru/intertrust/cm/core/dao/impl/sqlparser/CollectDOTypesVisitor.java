package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Возвращает множество таблиц (типов ДО) в SQL запросе. Построен по принципу визитора.
 * @author atsvetkov
 *
 */
public class CollectDOTypesVisitor implements SelectVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor {

    private Set<String> doTypes = new HashSet<>();

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
        select.getSelectBody().accept(this);
        addChildDOTypes();
        return doTypes;
    }

    private void addChildDOTypes() {
        Set<String> allChildren = new HashSet<>();
        for (String doType : doTypes) {
            Collection<DomainObjectTypeConfig> childTypeConfigs = configurationExplorer.findChildDomainObjectTypes(doType, true);
            if (childTypeConfigs != null) {
                for (DomainObjectTypeConfig childConfig : childTypeConfigs) {
                    String name = childConfig.getName();
                    if (name.startsWith("\"")) {
                        name = name.substring(1, name.length() - 1);
                    }
                    allChildren.add(name.toLowerCase());
                }
            }
        }
        doTypes.addAll(allChildren);
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

    public void visit(PlainSelect plainSelect) {
        plainSelect.getFromItem().accept(this);

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                join.getRightItem().accept(this);
                if (join.getOnExpression() != null) {
                    join.getOnExpression().accept(this);
                }
            }
        }

        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(this);
        }

    }

    @Override
    public void visit(SignedExpression signedExpression) {

    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        for (ExpressionList expressionList : multiExprList.getExprList()) {
            visit(expressionList);
        }
    }

    public void visit(Table tableName) {
        String tableWholeName = tableName.getName();
        if (tableWholeName.startsWith("\"")) {
            tableWholeName = tableWholeName.substring(1, tableWholeName.length() - 1);
        }
        if (configurationExplorer.getDomainObjectTypeConfig(tableWholeName) == null) {
            return;
        }
        doTypes.add(tableWholeName.toLowerCase());
    }

    public void visit(SubSelect subSelect) {
        subSelect.getSelectBody().accept(this);
    }

    public void visit(Addition addition) {
        visitBinaryExpression(addition);
    }

    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression);
    }

    public void visit(Between between) {
        between.getLeftExpression().accept(this);
        between.getBetweenExpressionStart().accept(this);
        between.getBetweenExpressionEnd().accept(this);
    }

    public void visit(Column tableColumn) {
    }

    public void visit(Division division) {
        visitBinaryExpression(division);
    }

    public void visit(DoubleValue doubleValue) {
    }

    public void visit(EqualsTo equalsTo) {
        visitBinaryExpression(equalsTo);
    }

    public void visit(Function function) {
    }

    public void visit(GreaterThan greaterThan) {
        visitBinaryExpression(greaterThan);
    }

    public void visit(GreaterThanEquals greaterThanEquals) {
        visitBinaryExpression(greaterThanEquals);
    }

    public void visit(InExpression inExpression) {
        inExpression.getLeftExpression().accept(this);
        inExpression.getRightItemsList().accept(this);
    }

    public void visit(IsNullExpression isNullExpression) {
    }

    public void visit(JdbcParameter jdbcParameter) {
    }

    public void visit(LikeExpression likeExpression) {
        visitBinaryExpression(likeExpression);
    }

    public void visit(ExistsExpression existsExpression) {
        existsExpression.getRightExpression().accept(this);
    }

    public void visit(LongValue longValue) {
    }

    public void visit(MinorThan minorThan) {
        visitBinaryExpression(minorThan);
    }

    public void visit(MinorThanEquals minorThanEquals) {
        visitBinaryExpression(minorThanEquals);
    }

    public void visit(Multiplication multiplication) {
        visitBinaryExpression(multiplication);
    }

    public void visit(NotEqualsTo notEqualsTo) {
        visitBinaryExpression(notEqualsTo);
    }

    public void visit(NullValue nullValue) {
    }

    public void visit(OrExpression orExpression) {
        visitBinaryExpression(orExpression);
    }

    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    public void visit(StringValue stringValue) {
    }

    public void visit(Subtraction subtraction) {
        visitBinaryExpression(subtraction);
    }

    public void visitBinaryExpression(BinaryExpression binaryExpression) {
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);
    }

    public void visit(ExpressionList expressionList) {
        for (Expression expression : expressionList.getExpressions()) {
            expression.accept(this);
        }
    }

    public void visit(DateValue dateValue) {
    }

    public void visit(TimestampValue timestampValue) {
    }

    public void visit(TimeValue timeValue) {
    }

    public void visit(CaseExpression caseExpression) {
    }

    public void visit(WhenClause whenClause) {
    }

    public void visit(AllComparisonExpression allComparisonExpression) {
        allComparisonExpression.getSubSelect().getSelectBody().accept(this);
    }

    public void visit(AnyComparisonExpression anyComparisonExpression) {
        anyComparisonExpression.getSubSelect().getSelectBody().accept(this);
    }

    public void visit(SubJoin subjoin) {
        subjoin.getLeft().accept(this);
        subjoin.getJoin().getRightItem().accept(this);
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
    }

    @Override
    public void visit(Concat concat) {

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

    @Override
    public void visit(CastExpression castExpression) {
    }

    @Override
    public void visit(Modulo modulo) {
    }

    @Override
    public void visit(AnalyticExpression analyticExpression) {
    }

    @Override
    public void visit(ExtractExpression extractExpression) {
    }

    @Override
    public void visit(IntervalExpression intervalExpression) {
    }

    @Override
    public void visit(OracleHierarchicalExpression oracleHierarchicalExpression) {
    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        lateralSubSelect.accept(this);
    }

    @Override
    public void visit(ValuesList valuesList) {
        if (valuesList.getMultiExpressionList() != null) {
            visit(valuesList.getMultiExpressionList());
        }
    }

    @Override
    public void visit(SetOperationList setOperationList) {
        if (setOperationList.getPlainSelects() != null) {
            for (PlainSelect plainSelect : setOperationList.getPlainSelects()) {
                visit(plainSelect);
            }
        }
    }

    @Override
    public void visit(WithItem withItem) {
    }
}
