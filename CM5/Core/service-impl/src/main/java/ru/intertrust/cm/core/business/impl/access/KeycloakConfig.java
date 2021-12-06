package ru.intertrust.cm.core.business.impl.access;

import ru.intertrust.cm.core.business.api.access.IdpConfig;

public class KeycloakConfig implements IdpConfig {
    private final String serverUrl;
    private final String realm;
    private final String adminClientId;
    private final String adminSecret;
    private final String clientId;
    private final boolean disableTrustManager;
    private final String truststore;
    private final String truststorePassword;
    private final boolean idpAuthentication;
    private final boolean sslEnabled;

    public KeycloakConfig(String serverUrl, String realm, String adminClientId,
                          String adminSecret, String clientId, boolean disableTrustManager,
                          String truststore, String truststorePassword, boolean idpAuthentication, boolean sslEnabled) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.adminClientId = adminClientId;
        this.adminSecret = adminSecret;
        this.clientId = clientId;
        this.disableTrustManager = disableTrustManager;
        this.truststore = truststore;
        this.truststorePassword = truststorePassword;
        this.idpAuthentication = idpAuthentication;
        this.sslEnabled = sslEnabled;
    }

    public static KeycloakConfigBuilder getBuilder() {
        return new KeycloakConfigBuilder();
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public String getClientId() {
        return clientId;
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

    @Override
    public boolean isIdpAuthentication() {
        return idpAuthentication;
    }

    @Override
    public String getAdminClientId() {
        return adminClientId;
    }

    @Override
    public String getAdminSecret() {
        return adminSecret;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    static class KeycloakConfigBuilder {
        private String serverUrl;
        private String realm;
        private String adminClientId;
        private String adminSecret;
        private String clientId;
        private boolean disableTrustManager;
        private String truststore;
        private String truststorePassword;
        private boolean idpAuthentication;
        private boolean enableSsl;

        public KeycloakConfigBuilder setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public KeycloakConfigBuilder setRealm(String realm) {
            this.realm = realm;
            return this;
        }

        public KeycloakConfigBuilder setAdminClientId(String adminClientId) {
            this.adminClientId = adminClientId;
            return this;
        }

        public KeycloakConfigBuilder setAdminSecret(String adminSecret) {
            this.adminSecret = adminSecret;
            return this;
        }

        public KeycloakConfigBuilder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public KeycloakConfigBuilder setDisableTrustManager(boolean disableTrustManager) {
            this.disableTrustManager = disableTrustManager;
            return this;
        }

        public KeycloakConfigBuilder setTruststore(String truststore) {
            this.truststore = truststore;
            return this;
        }

        public KeycloakConfigBuilder setTruststorePassword(String truststorePassword) {
            this.truststorePassword = truststorePassword;
            return this;
        }

        public KeycloakConfigBuilder setIdpAuthentication(boolean idpAuthentication) {
            this.idpAuthentication = idpAuthentication;
            return this;
        }

        public KeycloakConfigBuilder setEnableSsl(boolean enableSsl) {
            this.enableSsl = enableSsl;
            return this;
        }

        public KeycloakConfig createKeycloakConfig() {
            return new KeycloakConfig(serverUrl, realm, adminClientId, adminSecret,
                    clientId, disableTrustManager, truststore, truststorePassword, idpAuthentication, enableSsl);
        }
    }
}
