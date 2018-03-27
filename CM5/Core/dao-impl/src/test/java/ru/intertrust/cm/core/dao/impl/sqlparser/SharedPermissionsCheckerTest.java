package ru.intertrust.cm.core.dao.impl.sqlparser;

import static net.sf.jsqlparser.parser.CCJSqlParserUtil.parseExpression;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SubSelect;

import org.junit.Test;

import ru.intertrust.cm.core.dao.impl.sqlparser.FakeConfigurationExplorer.TypeConfigBuilder;

public class SharedPermissionsCheckerTest {

    private FakeConfigurationExplorer configurationExplorer = new FakeConfigurationExplorer();
    private SharedPermissionsChecker checker = new SharedPermissionsChecker(configurationExplorer);

    private Table createTable(String name, String alias) {
        Table table = new Table(name);
        if (alias != null) {
            table.setAlias(new Alias(alias));
        }
        return table;
    }

    private Expression equalsExpression(String left, String right) throws JSQLParserException {
        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(parseExpression(left));
        equalsTo.setRightExpression(parseExpression(right));
        return equalsTo;
    }

    private FromItemAccessor createPlainSelectAccessor(String tableName, String alias, Expression whereExpression) {
        PlainSelect plainSelect = new PlainSelect();
        plainSelect.setFromItem(createTable(tableName, alias));
        plainSelect.setWhere(whereExpression);
        return new FromItemAccessor(plainSelect);
    }

    private FromItemAccessor createJoinAccessor(String tableName, String alias, Expression expression, boolean isNaturalJoin) {
        Join join = new Join();
        join.setRightItem(createTable(tableName, alias));
        join.setOnExpression(expression);
        join.setNatural(isNaturalJoin);
        return new FromItemAccessor(join);
    }

    @Test
    public void testSimpleBundle() throws JSQLParserException {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("root")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("branch", false).parent("root")));
        FromItemAccessor first = createPlainSelectAccessor("root", null, null);
        FromItemAccessor second = createJoinAccessor("branch", null, equalsExpression("branch.id", "root.id"), false);
        assertTrue(checker.check(first, second));
    }

    @Test
    public void testWithAlias() throws JSQLParserException {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("root")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("branch", false).parent("root")));
        FromItemAccessor first = createPlainSelectAccessor("root", "r", null);
        FromItemAccessor second = createJoinAccessor("branch", "b", equalsExpression("b.id", "r.id"), false);
        assertTrue(checker.check(first, second));
    }

    @Test
    public void testOneOfAndsIsEnough() throws JSQLParserException {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("root")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("branch", false).parent("root")));
        FromItemAccessor first = createPlainSelectAccessor("root", "r", null);
        FromItemAccessor second = createJoinAccessor("branch", "b", andExpression(parseExpression("someOtherCondition()"), equalsExpression("b.id", "r.id")),
                false);
        assertTrue(checker.check(first, second));
    }

    @Test
    public void testOneOfOrsIsNotEnough() throws JSQLParserException {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("root")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("branch").parent("root")));
        FromItemAccessor first = createPlainSelectAccessor("root", "r", null);
        FromItemAccessor second = createJoinAccessor("branch", "b",
                new OrExpression(parseExpression("someOtherCondition()"), equalsExpression("b.id", "r.id")),
                false);
        assertFalse(checker.check(first, second));
    }

    @Test
    public void testIgnoreNonTableFromItems() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("root")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("branch").parent("root")));
        PlainSelect plainSelect = new PlainSelect();
        plainSelect.setFromItem(new SubSelect());
        FromItemAccessor first = new FromItemAccessor(plainSelect);
        Join join = new Join();
        join.setRightItem(new SubSelect());
        FromItemAccessor second = new FromItemAccessor(join);
        assertFalse(checker.check(first, second));
    }

    @Test
    public void testNotAType() throws JSQLParserException {
        FromItemAccessor first = createPlainSelectAccessor("root", "rm", null);
        FromItemAccessor second = createJoinAccessor("branch", "b", equalsExpression("r.id", "rm.root_id"), false);
        assertFalse(checker.check(first, second));
    }

    @Test
    public void testIgnoreJoinsWithoutOn() throws JSQLParserException {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("root")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("branch").parent("root")));
        FromItemAccessor first = createPlainSelectAccessor("root", "r", null);
        FromItemAccessor second = createJoinAccessor("branch", "b", null, false);
        assertFalse(checker.check(first, second));
    }

    @Test
    public void testAutoSuccessForFullJoinIfTablesRelated() throws JSQLParserException {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("root")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("branch", false).parent("root")));
        FromItemAccessor first = createPlainSelectAccessor("root", "r", null);
        FromItemAccessor second = createJoinAccessor("branch", "b", null, true);
        assertTrue(checker.check(first, second));
    }

    @Test
    public void testLinkedTypes() throws JSQLParserException {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("root")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("root_multiple").linkedTo("root", "root_id")));
        FromItemAccessor first = createPlainSelectAccessor("root", "r", null);
        FromItemAccessor second = createJoinAccessor("root_multiple", "rm", equalsExpression("r.id", "rm.root_id"), false);
        assertTrue(checker.check(first, second));
    }

    @Test
    public void testCaseInsensitive() throws JSQLParserException {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("Root")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("Root_Multiple").linkedTo("Root", "Root_id")));
        FromItemAccessor first = createPlainSelectAccessor("Root", "r", null);
        FromItemAccessor second = createJoinAccessor("Root_multiple", "rm", equalsExpression("r.ID", "rm.root_id"), false);
        assertTrue(checker.check(first, second));
    }

    @Test
    public void testLinkedToAbstractType() throws JSQLParserException {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("Abstract")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("Root").parent("Abstract")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("Abstract_Multiple").linkedTo("Abstract", "Abstract_id")));
        FromItemAccessor first = createPlainSelectAccessor("Abstract", "a", null);
        FromItemAccessor second = createJoinAccessor("Abstract_Multiple", "am", equalsExpression("a.ID", "am.abstract_id"), false);
        assertTrue(checker.check(first, second));
    }

    @Test
    public void testJoinThroughParentType() throws JSQLParserException {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("Abstract")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("Root").parent("Abstract")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("Root_Multiple").linkedTo("Root", "Root_id")));
        FromItemAccessor first = createPlainSelectAccessor("Abstract", "a", null);
        FromItemAccessor second = createJoinAccessor("Root_Multiple", "rm", equalsExpression("a.ID", "rm.root_id"), false);
        assertTrue(checker.check(first, second));
    }

    @Test
    public void testBorrowedAccessMatrix() throws JSQLParserException {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("Document")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("Resolution").linkedTo("Document", "Root")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("ResolutionAttr").linkedTo("Resolution", "Owner")));
        FromItemAccessor first = createPlainSelectAccessor("Resolution", "r", null);
        FromItemAccessor second = createJoinAccessor("ResolutionAttr", "ra", equalsExpression("ra.owner", "r.id"), false);
        assertTrue(checker.check(first, second));
    }

    @Test
    public void testMatrixlessTypes() throws JSQLParserException {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("A", false)));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("B", false)));
        FromItemAccessor first = createPlainSelectAccessor("A", "a", null);
        FromItemAccessor second = createJoinAccessor("B", "b", equalsExpression("b.a", "A.id"), false);
        assertFalse(checker.check(first, second));
    }

    @Test
    public void testAliaslessColumnname() throws JSQLParserException {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("A")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("B")).linkedTo("A", "a"));
        FromItemAccessor first = createPlainSelectAccessor("A", null, null);
        FromItemAccessor second = createJoinAccessor("B", null, equalsExpression("a", "A.id"), false);
        assertTrue(checker.check(first, second));
    }

    @Test
    public void testTwoPlainSelects() throws JSQLParserException {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("A")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("B")).linkedTo("A", "a"));
        FromItemAccessor first = createPlainSelectAccessor("A", null, null);
        FromItemAccessor second = createPlainSelectAccessor("B", null, equalsExpression("a", "A.id"));
        assertTrue(checker.check(first, second));
    }

    private Expression andExpression(Expression left, Expression right) {
        return new AndExpression(left, right);
    }

}
