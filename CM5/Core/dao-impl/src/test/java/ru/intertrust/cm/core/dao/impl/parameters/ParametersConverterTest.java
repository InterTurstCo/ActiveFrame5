package ru.intertrust.cm.core.dao.impl.parameters;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import ru.intertrust.cm.core.business.api.QueryModifierPrompt;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;

public class ParametersConverterTest {

    private ParametersConverter converter = new ParametersConverter();

    private Filter filter(String name, Value<?>... values) {
        Filter filter = new Filter();
        filter.setFilter(name);
        int index = 0;
        for (Value<?> v : values) {
            filter.addCriterion(index, v);
            index++;
        }
        return filter;
    }

    private Filter multiCriterionReferenceFilter(String name, List<Id> ids) {
        Filter filter = new Filter();
        filter.setFilter(name);
        filter.addMultiReferenceCriterion(0, ids);
        return filter;
    }

    private ListValue idsList(Id... ids) {
        ArrayList<Value<?>> values = new ArrayList<Value<?>>();
        for (Id id : ids) {
            values.add(new ReferenceValue(id));
        }
        return ListValue.createListValue(values);
    }

    @Test
    public void testFiltersListParameter() {
        HashMap<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("parent_0_0", singletonList(1L));
        expectedParams.put("parent_0_0_type", 1L);
        expectedParams.put("parent_0", 1L);
        expectedParams.put("parent_0_type", 1L);
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(expectedParams,
                (new QueryModifierPrompt()).appendIdParamsPrompt("parent_0", 1));
        assertEquals(expected, converter.convertReferenceValuesInFilters(singletonList(filter("parent", idsList(new RdbmsId(1, 1))))));
    }

    @Test
    public void testFiltersListParameterDifferentTypes() {
        HashMap<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("parent_0_0", asList(1L));
        expectedParams.put("parent_0_0_type", 1L);
        expectedParams.put("parent_0_1", asList(2L));
        expectedParams.put("parent_0_1_type", 2L);
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(expectedParams,
                (new QueryModifierPrompt()).appendIdParamsPrompt("parent_0", 2));
        assertEquals(expected, converter.convertReferenceValuesInFilters(singletonList(filter("parent", idsList(new RdbmsId(1, 1), new RdbmsId(2, 2))))));
    }

    @Test
    public void testFiltersListParameterMixedTypes() {
        HashMap<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("parent_0_0", asList(1L, 2L));
        expectedParams.put("parent_0_0_type", 1L);
        expectedParams.put("parent_0_1", asList(2L));
        expectedParams.put("parent_0_1_type", 2L);
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(expectedParams,
                (new QueryModifierPrompt()).appendIdParamsPrompt("parent_0", 2));
        assertEquals(expected,
                converter.convertReferenceValuesInFilters(singletonList(filter("parent", idsList(new RdbmsId(1, 1), new RdbmsId(1, 2), new RdbmsId(2, 2))))));
    }

    @Test
    public void testFiltersListParameterSameType() {
        HashMap<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("parent_0_0", asList(1L, 2L));
        expectedParams.put("parent_0_0_type", 1L);
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(expectedParams,
                (new QueryModifierPrompt()).appendIdParamsPrompt("parent_0", 1));
        assertEquals(expected, converter.convertReferenceValuesInFilters(singletonList(filter("parent", idsList(new RdbmsId(1, 1), new RdbmsId(1, 2))))));
    }

    @Test
    public void testFiltersNoReferenceParameters() {
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(new HashMap<String, Object>(), new QueryModifierPrompt());
        Filter filter = new Filter();
        filter.setFilter("parent");
        filter.addStringCriterion(0, "");
        assertEquals(expected, converter.convertReferenceValuesInFilters(singletonList(filter)));
    }

    @Test
    public void testFiltersReferenceParameter() {
        HashMap<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("parent_0", 1L);
        expectedParams.put("parent_0_type", 1L);
        expectedParams.put("parent_0_0", asList(1L));
        expectedParams.put("parent_0_0_type", 1L);
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(expectedParams,
                (new QueryModifierPrompt()).appendIdParamsPrompt("parent_0", 1));
        assertEquals(expected, converter.convertReferenceValuesInFilters(singletonList(filter("parent", new ReferenceValue(new RdbmsId(1, 1))))));
    }

    @Test
    public void testFiltersWithMultipleCriterionsReferenceParameter() {
        HashMap<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("parent_0", 1L);
        expectedParams.put("parent_0_type", 1L);
        expectedParams.put("parent_1", 1L);
        expectedParams.put("parent_1_type", 1L);
        expectedParams.put("parent_0_0", asList(1L));
        expectedParams.put("parent_0_0_type", 1L);
        expectedParams.put("parent_1_0", asList(1L));
        expectedParams.put("parent_1_0_type", 1L);
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(expectedParams,
                (new QueryModifierPrompt()).appendIdParamsPrompt("parent_0", 1).appendIdParamsPrompt("parent_1", 1));
        assertEquals(expected, converter.convertReferenceValuesInFilters(singletonList(filter(
                "parent",
                new ReferenceValue(new RdbmsId(1, 1)),
                new ReferenceValue(new RdbmsId(1, 1))
                ))));
    }

    @Test
    public void testMulticriterionFilterReferenceParameter() {
        HashMap<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("parent_0_0", asList(1L));
        expectedParams.put("parent_0_0_type", 1L);
        expectedParams.put("parent_0_1", asList(1L));
        expectedParams.put("parent_0_1_type", 2L);
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(expectedParams,
                (new QueryModifierPrompt()).appendIdParamsPrompt("parent_0", 2));
        assertEquals(expected, converter.convertReferenceValuesInFilters(singletonList(multiCriterionReferenceFilter(
                "parent",
                asList(
                        (Id) new RdbmsId(1, 1),
                        (Id) new RdbmsId(2, 1)
                )
                ))));
    }

    @Test
    public void testListParameter() {
        HashMap<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("PARAM0_0", singletonList(1L));
        expectedParams.put("PARAM0_0_type", 1L);
        expectedParams.put("PARAM0", 1L);
        expectedParams.put("PARAM0_type", 1L);
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(expectedParams,
                (new QueryModifierPrompt()).appendIdParamsPrompt("PARAM0", 1));
        List<? extends Value<?>> values = asList((Value<?>) idsList(new RdbmsId(1, 1)));
        assertEquals(expected, converter.convertReferenceValues(values));
    }

    @Test
    public void testListParameterDifferentTypes() {
        HashMap<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("PARAM0_0", asList(1L));
        expectedParams.put("PARAM0_0_type", 1L);
        expectedParams.put("PARAM0_1", asList(2L));
        expectedParams.put("PARAM0_1_type", 2L);
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(expectedParams,
                (new QueryModifierPrompt()).appendIdParamsPrompt("PARAM0", 2));
        List<? extends Value<?>> values = asList((Value<?>) idsList(new RdbmsId(1, 1), new RdbmsId(2, 2)));
        assertEquals(expected, converter.convertReferenceValues(values));
    }

    @Test
    public void testListParameterMixedTypes() {
        HashMap<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("PARAM0_0", asList(1L, 2L));
        expectedParams.put("PARAM0_0_type", 1L);
        expectedParams.put("PARAM0_1", asList(2L));
        expectedParams.put("PARAM0_1_type", 2L);
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(expectedParams,
                (new QueryModifierPrompt()).appendIdParamsPrompt("PARAM0", 2));
        List<? extends Value<?>> values = asList((Value<?>) idsList(new RdbmsId(1, 1), new RdbmsId(2, 2), new RdbmsId(1, 2)));
        assertEquals(expected, converter.convertReferenceValues(values));
    }

    @Test
    public void testListParameterSameType() {
        HashMap<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("PARAM0_0", asList(1L, 2L));
        expectedParams.put("PARAM0_0_type", 1L);
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(expectedParams,
                (new QueryModifierPrompt()).appendIdParamsPrompt("PARAM0", 1));
        List<? extends Value<?>> values = asList((Value<?>) idsList(new RdbmsId(1, 1), new RdbmsId(1, 2)));
        assertEquals(expected, converter.convertReferenceValues(values));
    }

    @Test
    public void testListParameterNotReferenceInside() {
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(new HashMap<String, Object>(),
                (new QueryModifierPrompt()));
        List<? extends Value<?>> values = asList((Value<?>) ListValue.createListValue(asList(new StringValue(""))));
        assertEquals(expected, converter.convertReferenceValues(values));
    }

    @Test
    public void testNoReferenceParameters() {
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(new HashMap<String, Object>(), new QueryModifierPrompt());
        List<? extends Value<?>> values = asList((Value<?>)
                new StringValue(""));
        assertEquals(expected, converter.convertReferenceValues(values));
    }

    @Test
    public void testReferenceParameter() {
        HashMap<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("PARAM0", 1L);
        expectedParams.put("PARAM0_type", 1L);
        expectedParams.put("PARAM0_0", asList(1L));
        expectedParams.put("PARAM0_0_type", 1L);
        Pair<HashMap<String, Object>, QueryModifierPrompt> expected = new Pair<>(expectedParams,
                (new QueryModifierPrompt()).appendIdParamsPrompt("PARAM0", 1));
        List<? extends Value<?>> values = asList((Value<?>) new ReferenceValue(new RdbmsId(1, 1)));
        assertEquals(expected, converter.convertReferenceValues(values));
    }
}
