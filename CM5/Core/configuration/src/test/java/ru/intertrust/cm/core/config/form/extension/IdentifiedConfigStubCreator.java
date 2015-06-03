package ru.intertrust.cm.core.config.form.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.05.2015
 *         Time: 13:01
 */
public class IdentifiedConfigStubCreator {

    public static List<IdentifiedConfigStub> createIdentifiedConfigs(int size, List<Integer> skippedPositions) {
        return createIdentifiedConfigs(size, "", skippedPositions);

    }

    public static List<IdentifiedConfigStub> createIdentifiedConfigs(int size) {
        return createIdentifiedConfigs(size, Collections.<Integer>emptyList());
    }

    public static List<IdentifiedConfigStub> createIdentifiedConfigs(int size, String contentPrefix) {
        return createIdentifiedConfigs(size, contentPrefix, Collections.<Integer>emptyList());
    }

    public static List<IdentifiedConfigStub> createIdentifiedConfigs(int size, String contentPrefix, List<Integer> skippedPositions) {
        List<IdentifiedConfigStub> result = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            if (!skippedPositions.contains(i)) {
                result.add(new IdentifiedConfigStub(String.valueOf(i), contentPrefix));
            }
        }
        return result;
    }
}
