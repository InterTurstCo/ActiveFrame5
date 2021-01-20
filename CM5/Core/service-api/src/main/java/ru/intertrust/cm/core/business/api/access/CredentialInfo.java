package ru.intertrust.cm.core.business.api.access;

import javax.annotation.Nonnull;

public interface CredentialInfo {

    boolean isTemporary();

    String getType();

    @Nonnull
    String getValue();

}
