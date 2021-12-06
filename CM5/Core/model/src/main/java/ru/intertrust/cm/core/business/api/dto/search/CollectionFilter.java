package ru.intertrust.cm.core.business.api.dto.search;

import java.util.Collection;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public final class CollectionFilter<T> extends ConditionalFilter<Collection<T>> {

    public CollectionFilter(@Nonnull String field, @Nonnull Condition c, @Nonnull Collection<T> value) {
        super(field, c, value);
    }

    private static final EnumSet<Condition> allowedConditions = EnumSet.of(Condition.EQUALS, Condition.INTERSECT,
            Condition.V_SUBSET, Condition.P_SUBSET);
    @Override
    public EnumSet<Condition> allowedConditions() {
        return allowedConditions;
    }
}
