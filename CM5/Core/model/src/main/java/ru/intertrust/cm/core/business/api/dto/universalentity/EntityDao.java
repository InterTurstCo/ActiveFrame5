package ru.intertrust.cm.core.business.api.dto.universalentity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import ru.intertrust.cm.core.business.api.dto.Id;

@ThreadSafe
public interface EntityDao {

    @Nonnull
    <T extends Entity> T create (@Nonnull Class<T> clazz);

    @Nonnull
    Entity find (@Nonnull Id id);

    void delete (@Nonnull Id id);

    @Nonnull
    <E extends ListElement> E createListElement (@Nonnull List<E> list);

}