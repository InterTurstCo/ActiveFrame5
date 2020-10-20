package ru.intertrust.cm.core.business.impl.access;

import ru.intertrust.cm.core.business.api.access.IdpConfig;

public class KeycloakConfig implements IdpConfig {
    private String serverUrl;
    private String realm;
    private String realmPublicKey;
    private String adminLogin;
    private String adminPassword;
    private String clientId;
    private boolean disableTrustManager;
    private String truststore;
    private String truststorePassword;
    private boolean idpAuthentication;

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    @Override
    public String getRealmPublicKey() {
        return realmPublicKey;
    }

    public void setRealmPublicKey(String realmPublicKey) {
        this.realmPublicKey = realmPublicKey;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getAdminLogin() {
        return adminLogin;
    }

    public void setAdminLogin(String adminLogin) {
        this.adminLogin = adminLogin;
    }

    @Override
    public String getAdminPassword() {
        return adminPassword;
    }

    @Override
    public boolean isDisableTrustManager() {
        return disableTrustManager;
    }

    @Override
    public String getTruststore() {
        return truststore;
    }

    @Override
    public String getTruststorePassword() {
        return truststorePassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public void setDisableTrustManager(boolean disableTrustManager) {
        this.disableTrustManager = disableTrustManager;
    }

    public void setTruststore(String truststore) {
        this.truststore = truststore;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    @Override
    public boolean isIdpAuthentication() {
        return idpAuthentication;
    }

    public void setIdpAuthentication(boolean idpAuthentication) {
        this.idpAuthentication = idpAuthentication;
    }
}
