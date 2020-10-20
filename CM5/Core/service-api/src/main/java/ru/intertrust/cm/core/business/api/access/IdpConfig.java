package ru.intertrust.cm.core.business.api.access;

public interface IdpConfig {

    String getServerUrl();

    String getRealm();

    String getRealmPublicKey();

    String getClientId();

    String getAdminLogin();

    String getAdminPassword();
}
