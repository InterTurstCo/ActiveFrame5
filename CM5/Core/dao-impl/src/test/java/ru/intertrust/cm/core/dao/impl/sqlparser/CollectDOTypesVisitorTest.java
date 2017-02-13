package ru.intertrust.cm.core.dao.impl.sqlparser;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ru.intertrust.cm.core.dao.impl.sqlparser.FakeConfigurationExplorer.TypeConfigBuilder;

public class CollectDOTypesVisitorTest {

    private FakeConfigurationExplorer configurationExplorer = new FakeConfigurationExplorer();

    private CollectDOTypesVisitor visitor = new CollectDOTypesVisitor(configurationExplorer);

    @Test
    public void testIgnoreNotDoTables() throws Exception {
        Set<String> types = visitor.getDOTypes("select * from a natural join b");
        Set<String> expected = new HashSet<>();
        assertEquals(expected, types);
    }

    @Test
    public void testFindDOTypesSelectItemSubQuery() throws Exception {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("x")).addLongField("n"));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("t")).linkedTo("x", "x"));
        Set<String> types = visitor.getDOTypes("select (select array_agg(abc) from t where t.x = x.id) from x where x.n = 0");
        Set<String> expected = new HashSet<>(Arrays.asList(new String[] {"t", "x" }));
        assertEquals(expected, types);
    }

    @Test
    public void testFindDOTypesWithSubQuery() throws Exception {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("x")).addLongField("n"));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("t")).linkedTo("x", "x"));
        Set<String> types = visitor.getDOTypes("with temp as (select a from x) select * from t natural join temp");
        Set<String> expected = new HashSet<>(Arrays.asList(new String[] {"t", "x" }));
        assertEquals(expected, types);
    }

    @Test
    public void testAddChildTypesToo() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("x")));
        configurationExplorer.createTypeConfig(new TypeConfigBuilder("xchild").parent("x"));
        configurationExplorer.createTypeConfig(new TypeConfigBuilder("xgrandchild").parent("xchild"));
        Set<String> types = visitor.getDOTypes("select * from xchild");
        Set<String> expected = new HashSet<>(Arrays.asList(new String[] {"xchild", "xgrandchild" }));
        assertEquals(expected, types);
    }

    @Test
    public void testQuotedTableNames() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("x")));
        Set<String> types = visitor.getDOTypes("select * from \"x\"");
        Set<String> expected = new HashSet<>(Arrays.asList(new String[] {"x" }));
        assertEquals(expected, types);
    }

    @Test
    public void testMixedCaseType() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("MixedCase")));
        Set<String> types = visitor.getDOTypes("select * from mixedCase");
        Set<String> expected = new HashSet<>(Arrays.asList(new String[] {"mixedcase" }));
        assertEquals(expected, types);
    }

    @Test
    public void testAliasedTable() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("x")));
        Set<String> types = visitor.getDOTypes("select * from (select alias.n from x alias) t");
        Set<String> expected = new HashSet<>(Arrays.asList(new String[] {"x" }));
        assertEquals(expected, types);
    }

    @Test
    public void testInSubquery() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("x")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("y")));
        Set<String> types = visitor.getDOTypes("select * from x where x.id in (select y.id from y where y.n = 0)");
        Set<String> expected = new HashSet<>(Arrays.asList(new String[] {"x", "y" }));
        assertEquals(expected, types);
    }

    @Test
    public void testCaseSubquery() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("x")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("y")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("z")));
        Set<String> types = visitor.getDOTypes("select case when x.n = 1 then (select t from y) else (select t from z) end from x");
        Set<String> expected = new HashSet<>(Arrays.asList(new String[] {"x", "y", "z" }));
        assertEquals(expected, types);
    }

    @Test
    public void testDOsWithNotDoTables() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("x")));
        Set<String> types = visitor.getDOTypes("select * from x naturnal join y");
        Set<String> expected = new HashSet<>(Arrays.asList(new String[] {"x" }));
        assertEquals(expected, types);
    }

}
