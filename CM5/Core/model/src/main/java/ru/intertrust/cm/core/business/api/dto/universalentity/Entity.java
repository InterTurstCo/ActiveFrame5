package ru.intertrust.cm.core.business.api.dto.universalentity;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nonnull;

import ru.intertrust.cm.core.business.api.dto.Id;

public interface Entity extends Serializable {

    boolean isNew ();

    @Nonnull
    Id getId ();

    @Nonnull
    Date getCreatedDate ();

    @Nonnull
    Date getModifiedDate ();

    void save ();

}