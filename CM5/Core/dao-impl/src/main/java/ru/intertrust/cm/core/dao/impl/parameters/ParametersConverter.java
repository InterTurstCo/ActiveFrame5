package ru.intertrust.cm.core.dao.impl.parameters;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.intertrust.cm.core.business.api.QueryModifierPrompt;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.impl.CollectionsDaoImpl;
import ru.intertrust.cm.core.dao.impl.sqlparser.ReferenceFilterUtility;

public class ParametersConverter {

    public Pair<Map<String, Object>, QueryModifierPrompt> convertReferenceValuesInFilters(List<? extends Filter> filters) {
        Map<String, Object> referenceParameters = new HashMap<>();
        QueryModifierPrompt prompt = new QueryModifierPrompt();
        for (Filter filterValue : filters) {
            for (Integer criterionKey : filterValue.getCriterionKeys()) {
                Value<?> value = null;
                if (filterValue.getIsSingleParameterMap().get(criterionKey)) {
                    value = filterValue.getCriterion(criterionKey);
                } else {
                    value = convertToListValue(filterValue.getMultiCriterion(criterionKey));
                }
                processValue(referenceParameters, prompt, filterValue.getFilter() + "_" + criterionKey, value);
            }
        }
        return new Pair<>(referenceParameters, prompt);
    }

    private ListValue convertToListValue(List<?> multiCriterion) {
        ArrayList<Value<?>> values = new ArrayList<>();
        for (Object o : multiCriterion) {
            values.add((Value<?>) o);
        }
        return ListValue.createListValue(values);
    }

    public Pair<Map<String, Object>, QueryModifierPrompt> convertReferenceValues(List<? extends Value<?>> values) {
        Map<String, Object> referenceParameters = new HashMap<>();
        QueryModifierPrompt prompt = new QueryModifierPrompt();
        for (int i = 0; i < values.size(); i++) {
            Value<?> value = values.get(i);
            processValue(referenceParameters, prompt, CollectionsDaoImpl.JDBC_PARAM_PREFIX + i, value);
        }
        return new Pair<>(referenceParameters, prompt);
    }

    private void processValue(Map<String, Object> referenceParameters, QueryModifierPrompt prompt, String baseParamName, Value<?> value) {
        ReferenceValue singleReferenceValue = getSingleReferenceValue(value);
        if (singleReferenceValue != null) {
            RdbmsId id = (RdbmsId) singleReferenceValue.get();
            referenceParameters.put(baseParamName, id.getId());
            referenceParameters.put(baseParamName + DomainObjectDao.REFERENCE_TYPE_POSTFIX, (long) id.getTypeId());
            referenceParameters.put(baseParamName + "_0", singletonList(id.getId()));
            referenceParameters.put(baseParamName + "_0" + DomainObjectDao.REFERENCE_TYPE_POSTFIX, (long) id.getTypeId());
            prompt.appendIdParamsPrompt(baseParamName, 1);
        } else if (value instanceof ListValue && containsOnlyReferenceValues((ListValue) value)) {
            ListValue listValue = (ListValue) value;
            HashMap<Long, List<Long>> idsByType = new HashMap<Long, List<Long>>();
            for (Value<?> v : listValue.getUnmodifiableValuesList()) {
                ReferenceValue refValue = ReferenceFilterUtility.getReferenceValue(v);
                if (refValue == null || refValue.get() == null) {
                    continue;
                }
                RdbmsId id = (RdbmsId) refValue.get();
                long type = id.getTypeId();
                if (!idsByType.containsKey(type)) {
                    idsByType.put(type, new ArrayList<Long>());
                }
                idsByType.get(type).add(id.getId());
            }
            int index = 0;
            for (Map.Entry<Long, List<Long>> e : idsByType.entrySet()) {
                String paramName = baseParamName + "_" + index;
                referenceParameters.put(paramName + DomainObjectDao.REFERENCE_TYPE_POSTFIX, e.getKey());
                referenceParameters.put(paramName, e.getValue());
                index++;
            }
            prompt.appendIdParamsPrompt(baseParamName, idsByType.size());
        }
    }

    private ReferenceValue getSingleReferenceValue(Value<?> value) {
        if (value instanceof ReferenceValue) {
            return (ReferenceValue) value;
        } else if (value instanceof ListValue) {
            ListValue listValue = (ListValue) value;
            List<Value<?>> values = listValue.getUnmodifiableValuesList();
            if (values.size() == 1 && values.get(0) instanceof ReferenceValue) {
                return (ReferenceValue) values.get(0);
            }
        }
        return null;
    }

    private boolean containsOnlyReferenceValues(ListValue value) {
        for (Value<?> v : value.getUnmodifiableValuesList()) {
            if (!(v instanceof ReferenceValue)) {
                return false;
            }
        }
        return true;
    }

}
