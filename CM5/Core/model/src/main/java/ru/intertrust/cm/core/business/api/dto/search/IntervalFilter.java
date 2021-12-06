package ru.intertrust.cm.core.business.api.dto.search;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import ru.intertrust.cm.core.business.api.dto.Pair;

public final class IntervalFilter<T> extends ConditionalFilter<Pair<T,T>> {

    public IntervalFilter(@Nonnull String field, @Nonnull Condition c, @Nonnull Pair<T, T> value) {
        super(field, c, value);
    }

    private static final EnumSet<Condition> allowedConditions = EnumSet.of(Condition.V_SUBSET);
    @Override
    public EnumSet<Condition> allowedConditions() {
        return allowedConditions;
    }
}
