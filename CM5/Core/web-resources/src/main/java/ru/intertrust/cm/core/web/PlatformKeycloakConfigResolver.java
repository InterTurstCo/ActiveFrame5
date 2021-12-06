package ru.intertrust.cm.core.web;

import org.keycloak.adapters.HttpClientBuilder;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.authentication.ClientCredentialsProviderUtils;
import org.keycloak.adapters.rotation.JWKPublicKeyLocator;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.common.enums.SslRequired;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.access.IdpConfig;
import ru.intertrust.cm.core.business.api.access.IdpAdminService;
import ru.intertrust.cm.core.tools.SpringClient;

public class PlatformKeycloakConfigResolver extends SpringClient implements KeycloakConfigResolver {

    @Autowired
    private IdpAdminService idpAdminService;

    @Override
    public KeycloakDeployment resolve(HttpFacade.Request facade) {

        IdpConfig config = idpAdminService.getConfig();

        KeycloakDeployment result = new KeycloakDeployment();
        result.setRealm(config.getRealm());
        result.setResourceName(config.getClientId());
        result.setPublicClient(true);
        AdapterConfig adapterConfig = new AdapterConfig();
        adapterConfig.setAuthServerUrl(config.getServerUrl());

        // Выключение проверки сертификатов
        adapterConfig.setDisableTrustManager(config.isDisableTrustManager());

        // Настройка хранилища сертификатов keycloak сервера
        adapterConfig.setTruststore(config.getTruststore());
        adapterConfig.setTruststorePassword(config.getTruststorePassword());

        result.setAuthServerBaseUrl(adapterConfig);
        result.setSslRequired(SslRequired.NONE);
        JWKPublicKeyLocator pkLocator = new JWKPublicKeyLocator();
        result.setPublicKeyLocator(pkLocator);
        result.setPublicKeyCacheTtl(600);

        result.setClientAuthenticator(ClientCredentialsProviderUtils.bootstrapClientAuthenticator(result));
        result.setClient(new HttpClientBuilder().build(adapterConfig));
        result.setCors(true);


        return result;
    }
}
