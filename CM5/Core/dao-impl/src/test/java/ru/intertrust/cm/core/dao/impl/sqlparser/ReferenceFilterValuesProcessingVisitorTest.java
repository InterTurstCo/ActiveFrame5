package ru.intertrust.cm.core.dao.impl.sqlparser;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.sf.jsqlparser.statement.select.Select;

import org.junit.Test;

import ru.intertrust.cm.core.business.api.QueryModifierPrompt;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.dao.impl.CollectionsDaoImpl;
import ru.intertrust.cm.core.dao.impl.parameters.ParametersConverter;

public class ReferenceFilterValuesProcessingVisitorTest {

    private ParametersConverter converter = new ParametersConverter();

    @Test
    public void testIgnoreWhenNoFilterValues() {
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValuesInFilters(new ArrayList<Filter>());
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(pair.getSecond(), false);
        String query = "select id from document where parent = " + parameterStub("parent", 0);
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        assertEquals(emptyMap(), visitor.getReplaceExpressions());
        assertEquals(emptyMap(), pair.getFirst());
    }

    private Filter createReferenceOrListFilter(String name, Id... ids) {
        Filter filter = new Filter();
        filter.setFilter(name);
        if (ids.length == 1) {
            filter.addReferenceCriterion(0, ids[0]);
        } else {
            ArrayList<Value<?>> list = new ArrayList<>();
            for (Id id : ids) {
                list.add(new ReferenceValue(id));
            }
            filter.addCriterion(0, ListValue.createListValue(list));
        }
        return filter;
    }

    @Test
    public void testEqualsId() {
        Filter filter = createReferenceOrListFilter("parent", new RdbmsId(1, 1));
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValuesInFilters(singletonList(filter));
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(pair.getSecond(), true);
        String query = "select id from document where parent = " + parameterStub("parent", 0);
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("parent_0", 1L);
        expectedParameters.put("parent_0_type", 1L);
        expectedParameters.put("parent_0_0", singletonList(1L));
        expectedParameters.put("parent_0_0_type", 1L);
        assertEquals(singletonMap("parent = " + parameterStub("parent", 0), "parent = :parent_0 AND parent_type = :parent_0_type"),
                visitor.getReplaceExpressions());
        assertEquals(expectedParameters, pair.getFirst());
    }

    @Test
    public void testInTwoIdsWithDifferentTypes() {
        Filter filter = createReferenceOrListFilter("parent", new RdbmsId(1, 1), new RdbmsId(2, 2));
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValuesInFilters(singletonList(filter));
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(pair.getSecond(), true);
        String query = "select id from document where parent in (" + parameterStub("parent", 0) + ")";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("parent_0_0", singletonList(1L));
        expectedParameters.put("parent_0_0_type", 1L);
        expectedParameters.put("parent_0_1", singletonList(2L));
        expectedParameters.put("parent_0_1_type", 2L);
        assertEquals(expectedParameters, pair.getFirst());
        assertEquals(singletonMap("parent IN (" + parameterStub("parent", 0) + ")", "(" + "(parent IN (:parent_0_0) AND parent_type = :parent_0_0_type)"
                + " OR (parent IN (:parent_0_1) AND parent_type = :parent_0_1_type)" + ")"),
                visitor.getReplaceExpressions());
    }

    @Test
    public void testNotInTwoIdsWithDifferentTypes() {
        Filter filter = createReferenceOrListFilter("parent", new RdbmsId(1, 1), new RdbmsId(2, 2));
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValuesInFilters(singletonList(filter));
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(pair.getSecond(), true);
        String query = "select id from document where parent not in (" + parameterStub("parent", 0) + ")";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("parent_0_0", singletonList(1L));
        expectedParameters.put("parent_0_0_type", 1L);
        expectedParameters.put("parent_0_1", singletonList(2L));
        expectedParameters.put("parent_0_1_type", 2L);
        assertEquals(expectedParameters, pair.getFirst());
        assertEquals(singletonMap("parent NOT IN (" + parameterStub("parent", 0) + ")", "((parent NOT IN (:parent_0_0) OR parent_type <> :parent_0_0_type)"
                + " AND (parent NOT IN (:parent_0_1) OR parent_type <> :parent_0_1_type))"),
                visitor.getReplaceExpressions());
    }

    @Test
    public void testInTwoIdsWithSameType() {
        Filter filter = createReferenceOrListFilter("parent", new RdbmsId(1, 1), new RdbmsId(1, 2));
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValuesInFilters(singletonList(filter));
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(pair.getSecond(), true);
        String query = "select id from document where parent in (" + parameterStub("parent", 0) + ")";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("parent_0_0", Arrays.asList(1L, 2L));
        expectedParameters.put("parent_0_0_type", 1L);
        assertEquals(expectedParameters, pair.getFirst());
        assertEquals(singletonMap("parent IN (" + parameterStub("parent", 0) + ")", "(parent IN (:parent_0_0)"
                + " AND parent_type = :parent_0_0_type)"),
                visitor.getReplaceExpressions());
    }

    @Test
    public void testInThreeIdsTwoOfThemWithSameType() {
        Filter filter = createReferenceOrListFilter("parent", new RdbmsId(1, 1), new RdbmsId(1, 2), new RdbmsId(2, 2));
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValuesInFilters(singletonList(filter));
        ReferenceParamsProcessingVisitor visitor = new ReferenceParamsProcessingVisitor(pair.getSecond(), true);
        String query = "select id from document where parent in (" + parameterStub("parent", 0) + ")";
        SqlQueryParser parser = new SqlQueryParser(query);
        Select select = parser.getSelectStatement();
        select.getSelectBody().accept(visitor);
        HashMap<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("parent_0_0", Arrays.asList(1L, 2L));
        expectedParameters.put("parent_0_0_type", 1L);
        expectedParameters.put("parent_0_1", Arrays.asList(2L));
        expectedParameters.put("parent_0_1_type", 2L);
        assertEquals(expectedParameters, pair.getFirst());
        assertEquals(singletonMap("parent IN (" + parameterStub("parent", 0) + ")", "((parent IN (:parent_0_0)"
                + " AND parent_type = :parent_0_0_type)" + " OR (parent IN (:parent_0_1) AND parent_type = :parent_0_1_type))"),
                visitor.getReplaceExpressions());
    }

    private String parameterStub(String name, int index) {
        return CollectionsDaoImpl.PARAM_NAME_PREFIX + name + index;
    }

}
