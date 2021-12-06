package ru.intertrust.cm.core.business.api.access;

public class CredentialInfoFactory {

    public static CredentialInfo newPasswordInfo(String password, boolean temporary) {
        return new PasswordInfo(password, temporary);
    }

    public static CredentialInfo newPasswordInfo(String password) {
        return newPasswordInfo(password, true);
    }

}
