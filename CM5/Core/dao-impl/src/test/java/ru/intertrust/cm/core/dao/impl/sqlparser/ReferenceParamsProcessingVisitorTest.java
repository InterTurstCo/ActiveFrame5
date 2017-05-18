package ru.intertrust.cm.core.dao.impl.sqlparser;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.statement.select.Select;

import org.junit.Test;

import ru.intertrust.cm.core.business.api.QueryModifierPrompt;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.dao.impl.CollectionsDaoImpl;
import ru.intertrust.cm.core.dao.impl.parameters.ParametersConverter;

public class ReferenceParamsProcessingVisitorTest {

    private ParametersConverter converter = new ParametersConverter();

    @Test
    public void testEqualsId() {
        List<? extends Value<?>> values = singletonList((Value<?>) new ReferenceValue(new RdbmsId(1, 1)));
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValues(values);

        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(pair.getSecond(), false);
        String query = "select id from document where parent = " + parameterStub(0);
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("PARAM0", 1L);
        expectedParameters.put("PARAM0_type", 1L);
        expectedParameters.put("PARAM0_0", asList(1L));
        expectedParameters.put("PARAM0_0_type", 1L);
        assertEquals(singletonMap("parent = " + parameterStub(0), "parent = :PARAM0 AND parent_type = :PARAM0_type"),
                visitor.getReplaceExpressions());
        assertEquals(expectedParameters, pair.getFirst());
    }

    @Test
    public void ignoreNonReferenceParameters() {
        List<? extends Value<?>> values = asList((Value<?>) new ReferenceValue(new RdbmsId(1, 1)), (Value<?>) new StringValue("test"));
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValues(values);

        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(pair.getSecond(), false);
        String query = "select id from document where parent = " + parameterStub(0) + " and name = " + parameterStub(1);
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("PARAM0", 1L);
        expectedParameters.put("PARAM0_type", 1L);
        expectedParameters.put("PARAM0_0", asList(1L));
        expectedParameters.put("PARAM0_0_type", 1L);
        assertEquals(singletonMap("parent = " + parameterStub(0), "parent = :PARAM0 AND parent_type = :PARAM0_type"),
                visitor.getReplaceExpressions());
        assertEquals(expectedParameters, pair.getFirst());
    }

    @Test
    public void testInTwoIdsWithDifferentTypes() {
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValues(singletonList(ListValue.createListValue(asList(new Value<?>[] {
                new ReferenceValue(new RdbmsId(1, 1)),
                new ReferenceValue(new RdbmsId(2, 2))
        }))));
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(pair.getSecond(), false);
        String query = "select id from document where parent in (" + parameterStub(0) + ")";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("PARAM0_0", singletonList(1L));
        expectedParameters.put("PARAM0_0_type", 1L);
        expectedParameters.put("PARAM0_1", singletonList(2L));
        expectedParameters.put("PARAM0_1_type", 2L);
        assertEquals(expectedParameters, pair.getFirst());
        assertEquals(singletonMap("parent IN (" + parameterStub(0) + ")", "(" + "(parent IN (:PARAM0_0) AND parent_type = :PARAM0_0_type)"
                + " OR (parent IN (:PARAM0_1) AND parent_type = :PARAM0_1_type)" + ")"),
                visitor.getReplaceExpressions());
    }

    @Test
    public void testNotInTwoIdsWithDifferentTypes() {
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValues(singletonList(ListValue.createListValue(asList(new Value<?>[] {
                new ReferenceValue(new RdbmsId(1, 1)),
                new ReferenceValue(new RdbmsId(2, 2))
        }))));
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(pair.getSecond(), false);
        String query = "select id from document where parent not in (" + parameterStub(0) + ")";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("PARAM0_0", singletonList(1L));
        expectedParameters.put("PARAM0_0_type", 1L);
        expectedParameters.put("PARAM0_1", singletonList(2L));
        expectedParameters.put("PARAM0_1_type", 2L);
        assertEquals(expectedParameters, pair.getFirst());
        assertEquals(singletonMap("parent NOT IN (" + parameterStub(0) + ")", "(" + "(parent NOT IN (:PARAM0_0) OR parent_type <> :PARAM0_0_type)"
                + " AND (parent NOT IN (:PARAM0_1) OR parent_type <> :PARAM0_1_type)" + ")"),
                visitor.getReplaceExpressions());
    }

    @Test
    public void testInTwoIdsWithSameType() {
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValues(singletonList(ListValue.createListValue(asList(new Value<?>[] {
                new ReferenceValue(new RdbmsId(1, 1)),
                new ReferenceValue(new RdbmsId(1, 2))
        }))));
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(pair.getSecond(), false);
        String query = "select id from document where parent in (" + parameterStub(0) + ")";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("PARAM0_0", asList(1L, 2L));
        expectedParameters.put("PARAM0_0_type", 1L);
        assertEquals(expectedParameters, pair.getFirst());
        assertEquals(singletonMap("parent IN (" + parameterStub(0) + ")", "(parent IN (:PARAM0_0)"
                + " AND parent_type = :PARAM0_0_type)"),
                visitor.getReplaceExpressions());
    }

    @Test
    public void testInThreeIdsTwoOfThemWithSameType() {
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValues(singletonList(ListValue.createListValue(asList(new Value<?>[] {
                new ReferenceValue(new RdbmsId(1, 1)),
                new ReferenceValue(new RdbmsId(1, 2)),
                new ReferenceValue(new RdbmsId(2, 2))
        }))));
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(pair.getSecond(), false);
        String query = "select id from document where parent in (" + parameterStub(0) + ")";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("PARAM0_0", asList(1L, 2L));
        expectedParameters.put("PARAM0_0_type", 1L);
        expectedParameters.put("PARAM0_1", asList(2L));
        expectedParameters.put("PARAM0_1_type", 2L);
        assertEquals(expectedParameters, pair.getFirst());
        assertEquals(singletonMap("parent IN (" + parameterStub(0) + ")", "(" + "(parent IN (:PARAM0_0) AND parent_type = :PARAM0_0_type)"
                + " OR (parent IN (:PARAM0_1) AND parent_type = :PARAM0_1_type)" + ")"),
                visitor.getReplaceExpressions());
    }

    private String parameterStub(int index) {
        return CollectionsDaoImpl.START_PARAM_SIGN + CollectionsDaoImpl.PARAM_NAME_PREFIX + index + CollectionsDaoImpl.END_PARAM_SIGN;
    }
}
