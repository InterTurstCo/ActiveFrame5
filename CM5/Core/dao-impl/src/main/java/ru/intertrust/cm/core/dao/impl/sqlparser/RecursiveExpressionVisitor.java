package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.WithinGroupExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.ValuesList;

public class RecursiveExpressionVisitor extends ExpressionVisitorAdapter implements ExpressionVisitor, ItemsListVisitor, FromItemVisitor {

    private SelectVisitor selectVisitor;

    @Override
    public void visit(ValuesList valuesList) {
        if (valuesList.getMultiExpressionList() != null) {
            this.visit(valuesList.getMultiExpressionList());
        }
    }

    @Override
    public void visit(SubJoin subjoin) {
        subjoin.getLeft().accept(this);
        subjoin.getJoin().getRightItem().accept(this);
    }

    @Override
    public void visit(SubSelect subSelect) {
        subSelect.getSelectBody().accept(selectVisitor);
    }

    @Override
    public void visit(Table tableName) {

    }

    public RecursiveExpressionVisitor(SelectVisitor selectVisitor) {
        this.selectVisitor = selectVisitor;
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        lateralSubSelect.accept(this);
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        for (ExpressionList expressionList : multiExprList.getExprList()) {
            visit(expressionList);
        }
    }

    @Override
    public void visit(ExpressionList expressionList) {
        for (Expression expression : expressionList.getExpressions()) {
            expression.accept(this);
        }
    }

    @Override
    public void visit(Addition addition) {
        visitBinaryExpression(addition);
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        allComparisonExpression.getSubSelect().getSelectBody().accept(selectVisitor);
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        if (aexpr.getExpression() != null) {
            aexpr.getExpression().accept(this);
        }
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression);
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        anyComparisonExpression.getSubSelect().getSelectBody().accept(selectVisitor);

    }

    @Override
    public void visit(Between between) {
        between.getLeftExpression().accept(this);
        between.getBetweenExpressionStart().accept(this);
        between.getBetweenExpressionEnd().accept(this);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        visitBinaryExpression(bitwiseAnd);
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        visitBinaryExpression(bitwiseOr);
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        visitBinaryExpression(bitwiseXor);
    }

    @Override
    public void visit(CastExpression cast) {
        if (cast.getLeftExpression() != null) {
            cast.getLeftExpression().accept(this);
        }
    }

    @Override
    public void visit(Concat concat) {
        visitBinaryExpression(concat);
    }

    @Override
    public void visit(Division division) {
        visitBinaryExpression(division);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        visitBinaryExpression(equalsTo);
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        existsExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        if (eexpr.getExpression() != null) {
            eexpr.getExpression().accept(this);
        }
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        visitBinaryExpression(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        visitBinaryExpression(greaterThanEquals);
    }

    @Override
    public void visit(InExpression inExpression) {
        if (inExpression.getLeftItemsList() != null) {
            inExpression.getLeftItemsList().accept(this);
        } else if (inExpression.getLeftExpression() != null) {
            inExpression.getLeftExpression().accept(this);
        }
        inExpression.getRightItemsList().accept(this);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        visitBinaryExpression(likeExpression);
    }

    @Override
    public void visit(Matches matches) {
        visitBinaryExpression(matches);
    }

    @Override
    public void visit(MinorThan minorThan) {
        visitBinaryExpression(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        visitBinaryExpression(minorThanEquals);
    }

    @Override
    public void visit(Multiplication multiplication) {
        visitBinaryExpression(multiplication);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        visitBinaryExpression(notEqualsTo);
    }

    @Override
    public void visit(OrExpression orExpression) {
        visitBinaryExpression(orExpression);
    }

    @Override
    public void visit(Subtraction subtraction) {
        visitBinaryExpression(subtraction);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression binaryExpression) {
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(WhenClause whenClause) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(Modulo modulo) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(WithinGroupExpression wgexpr) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(KeepExpression aexpr) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(TableFunction tableFunction) {
        // TODO Auto-generated method stub

    }

}