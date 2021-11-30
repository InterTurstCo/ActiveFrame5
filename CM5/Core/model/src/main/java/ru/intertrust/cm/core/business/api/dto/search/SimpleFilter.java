package ru.intertrust.cm.core.business.api.dto.search;

import java.util.EnumSet;
import javax.annotation.Nonnull;

public final class SimpleFilter<T> extends ConditionalFilter<T> {

    public SimpleFilter(@Nonnull String field, @Nonnull Condition condition, T value) {
        super(field, condition, value);
    }

    private static final EnumSet<Condition> allowedConditions = EnumSet.of(Condition.EXISTS, Condition.EQUALS,
            Condition.MATCH, Condition.V_SUBSET, Condition.P_SUBSET,
            Condition.LT, Condition.LT_INC, Condition.GT, Condition.GT_INC,
            Condition.BINARY_IN, Condition.BINARY_INTERSECT);
    @Override
    public EnumSet<Condition> allowedConditions() {
        return allowedConditions;
    }
}
