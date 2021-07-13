package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.TreeMap;
import javax.annotation.concurrent.NotThreadSafe;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Class helper. Collects compound fields data to ensure order
 */
@NotThreadSafe
public class CompoundFieldCollector {

    // Map < Field Name, TreeMap< Index of field in a config list, List of values >
    private Map<String, TreeMap<Integer, List<String>>> compoundFields;
    // Map < Field Name, Delimiter >
    private Map<String, String> fieldDelimiters;
    // Map < Field Name, Boost >
    private Map<String, Double> fieldBoosts;

    public void addDelimiter(String fieldName, String delimiter) {
        if (fieldDelimiters == null) {
            fieldDelimiters = new HashMap<>();
        }
        fieldDelimiters.putIfAbsent(fieldName, delimiter);
    }

    public void add(String fieldName, int orderIndex, String value) {
        if (compoundFields == null) {
            compoundFields = new HashMap<>();
        }
        compoundFields
                .computeIfAbsent(fieldName, s -> new TreeMap<>())
                .computeIfAbsent(orderIndex, s -> new ArrayList<>())
                .add(value);
    }

    public List<Triple<String, String, Double>> collect() {
        Objects.requireNonNull(fieldDelimiters, "Unable to collect compound fields. " +
                "At least one field delimiter must be set");
        Objects.requireNonNull(compoundFields, "Unable to collect compound fields. " +
                "At least one field value must be added");

        List<Triple<String, String, Double>> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : fieldDelimiters.entrySet()) {
            String field = entry.getKey();
            TreeMap<Integer, List<String>> fieldValues = compoundFields.get(field);
            StringJoiner calculatedValue = new StringJoiner(entry.getValue());
            for (Map.Entry<Integer, List<String>> valueEntry : fieldValues.entrySet()) {
                valueEntry.getValue().forEach(calculatedValue::add);;
            }

            result.add(ImmutableTriple.of(field, calculatedValue.toString(), fieldBoosts.get(field)));
        }

        return result;
    }

    public void addBoost(String fieldName, Double boostValue) {
        if (fieldBoosts == null) {
            fieldBoosts = new HashMap<>();
        }
        fieldBoosts.putIfAbsent(fieldName, boostValue);
    }

    public boolean isEmpty() {
        return compoundFields == null || compoundFields.isEmpty();
    }
}
