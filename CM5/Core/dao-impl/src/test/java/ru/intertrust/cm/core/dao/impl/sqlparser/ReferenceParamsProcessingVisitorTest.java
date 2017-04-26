package ru.intertrust.cm.core.dao.impl.sqlparser;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.jsqlparser.statement.select.Select;

import org.junit.Test;

import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.impl.CollectionsDaoImpl;

public class ReferenceParamsProcessingVisitorTest {
    @Test
    public void testIgnoreWhenNoFilterValues() {
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(new ArrayList<Value<?>>(), columnToConfigMap("parent"));
        String query = "select id from document where parent = " + parameterStub(0);
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        assertEquals(emptyMap(), visitor.getReplaceExpressions());
        assertEquals(emptyMap(), visitor.getJdbcParameters());
    }

    @Test
    public void testIgnoreWhenNoReferenceConfigs() {
        Value<?> value = new ReferenceValue(new RdbmsId(1, 1));
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(singletonList(value), new HashMap<String, FieldConfig>());
        String query = "select id from document where parent = " + parameterStub(0);
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        assertEquals(emptyMap(), visitor.getReplaceExpressions());
        assertEquals(emptyMap(), visitor.getJdbcParameters());
    }

    @Test
    public void testEqualsId() {
        Value<?> value = new ReferenceValue(new RdbmsId(1, 1));
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(singletonList(value), columnToConfigMap("parent"));
        String query = "select id from document where parent = " + parameterStub(0);
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("PARAM0", 1L);
        expectedParameters.put("PARAM0_type", 1L);
        assertEquals(singletonMap("parent = " + parameterStub(0), "parent = :PARAM0 AND parent_type = :PARAM0_type"),
                visitor.getReplaceExpressions());
        assertEquals(expectedParameters, visitor.getJdbcParameters());
    }

    @Test
    public void testInTwoIdsWithDifferentTypes() {
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(singletonList(new ListValue(
                asList(new Value[] {
                        new ReferenceValue(new RdbmsId(1, 1)),
                        new ReferenceValue(new RdbmsId(2, 2))
                }))), columnToConfigMap("parent"));
        String query = "select id from document where parent in (" + parameterStub(0) + ")";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("PARAM0_0", singletonList(1L));
        expectedParameters.put("PARAM0_0_type", 1L);
        expectedParameters.put("PARAM0_1", singletonList(2L));
        expectedParameters.put("PARAM0_1_type", 2L);
        assertEquals(expectedParameters, visitor.getJdbcParameters());
        assertEquals(singletonMap("parent IN (" + parameterStub(0) + ")", "(" + "(parent IN (:PARAM0_0) AND parent_type = :PARAM0_0_type)"
                + " OR (parent IN (:PARAM0_1) AND parent_type = :PARAM0_1_type)" + ")"),
                visitor.getReplaceExpressions());
    }

    @Test
    public void testNotInTwoIdsWithDifferentTypes() {
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(singletonList(new ListValue(
                asList(new Value[] {
                        new ReferenceValue(new RdbmsId(1, 1)),
                        new ReferenceValue(new RdbmsId(2, 2))
                }))), columnToConfigMap("parent"));
        String query = "select id from document where parent not in (" + parameterStub(0) + ")";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("PARAM0_0", singletonList(1L));
        expectedParameters.put("PARAM0_0_type", 1L);
        expectedParameters.put("PARAM0_1", singletonList(2L));
        expectedParameters.put("PARAM0_1_type", 2L);
        assertEquals(expectedParameters, visitor.getJdbcParameters());
        assertEquals(singletonMap("parent NOT IN (" + parameterStub(0) + ")", "(" + "(parent NOT IN (:PARAM0_0) OR parent_type <> :PARAM0_0_type)"
                + " AND (parent NOT IN (:PARAM0_1) OR parent_type <> :PARAM0_1_type)" + ")"),
                visitor.getReplaceExpressions());
    }

    @Test
    public void testInTwoIdsWithSameType() {
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(singletonList(new ListValue(
                asList(new Value[] {
                        new ReferenceValue(new RdbmsId(1, 1)),
                        new ReferenceValue(new RdbmsId(1, 2))
                }))), columnToConfigMap("parent"));
        String query = "select id from document where parent in (" + parameterStub(0) + ")";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("PARAM0_0", asList(1L, 2L));
        expectedParameters.put("PARAM0_0_type", 1L);
        assertEquals(expectedParameters, visitor.getJdbcParameters());
        assertEquals(singletonMap("parent IN (" + parameterStub(0) + ")", "(parent IN (:PARAM0_0)"
                + " AND parent_type = :PARAM0_0_type)"),
                visitor.getReplaceExpressions());
    }

    @Test
    public void testInThreeIdsTwoOfThemWithSameType() {
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(singletonList(new ListValue(
                asList(new Value[] {
                        new ReferenceValue(new RdbmsId(1, 1)),
                        new ReferenceValue(new RdbmsId(1, 2)),
                        new ReferenceValue(new RdbmsId(2, 2))
                }))), columnToConfigMap("parent"));
        String query = "select id from document where parent in (" + parameterStub(0) + ")";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("PARAM0_0", asList(1L, 2L));
        expectedParameters.put("PARAM0_0_type", 1L);
        expectedParameters.put("PARAM0_1", asList(2L));
        expectedParameters.put("PARAM0_1_type", 2L);
        assertEquals(expectedParameters, visitor.getJdbcParameters());
        assertEquals(singletonMap("parent IN (" + parameterStub(0) + ")", "(" + "(parent IN (:PARAM0_0) AND parent_type = :PARAM0_0_type)"
                + " OR (parent IN (:PARAM0_1) AND parent_type = :PARAM0_1_type)" + ")"),
                visitor.getReplaceExpressions());
    }

    private String parameterStub(int index) {
        return CollectionsDaoImpl.START_PARAM_SIGN + CollectionsDaoImpl.PARAM_NAME_PREFIX + index + CollectionsDaoImpl.END_PARAM_SIGN;
    }

    private Map<String, FieldConfig> columnToConfigMap(String... fields) {
        HashMap<String, FieldConfig> map = new HashMap<String, FieldConfig>();
        for (String field : fields) {
            ReferenceFieldConfig config = new ReferenceFieldConfig();
            config.setName(field);
            config.setType("document");
            map.put(field, config);
        }
        return map;
    }
}
