package ru.intertrust.cm.core.business.api.access;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@Immutable
class PasswordInfo implements CredentialInfo {

    private static final String TYPE = "password";

    private final String password;
    private final boolean temporary;

    PasswordInfo(@Nonnull String password, boolean temporary) {
        this.password = Objects.requireNonNull(password);
        this.temporary = temporary;
    }

    @Nonnull
    @Override
    public String getValue() {
        return password;
    }

    @Override
    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
