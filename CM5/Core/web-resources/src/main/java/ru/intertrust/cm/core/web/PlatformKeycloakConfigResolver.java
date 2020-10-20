package ru.intertrust.cm.core.web;

import java.util.Collections;
import org.keycloak.adapters.HttpClientBuilder;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.authentication.ClientCredentialsProviderUtils;
import org.keycloak.adapters.rotation.HardcodedPublicKeyLocator;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.common.enums.SslRequired;
import org.keycloak.common.util.PemUtils;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.intertrust.cm.core.business.api.access.IdpConfig;
import ru.intertrust.cm.core.business.api.access.IdpService;
import ru.intertrust.cm.core.tools.SpringClient;

public class PlatformKeycloakConfigResolver extends SpringClient implements KeycloakConfigResolver {

    @Autowired
    IdpService idpService;

    @Override
    public KeycloakDeployment resolve(HttpFacade.Request facade) {

        IdpConfig config = idpService.getConfig();

        KeycloakDeployment result = new KeycloakDeployment();
        result.setRealm(config.getRealm());
        result.setResourceName(config.getClientId());
        result.setPublicClient(true);
        AdapterConfig adapterConfig = new AdapterConfig();
        adapterConfig.setAuthServerUrl(config.getServerUrl());
        result.setAuthServerBaseUrl(adapterConfig);
        result.setSslRequired(SslRequired.NONE);
        HardcodedPublicKeyLocator pkLocator = new HardcodedPublicKeyLocator(PemUtils.decodePublicKey(config.getRealmPublicKey()));
        result.setPublicKeyLocator(pkLocator);
        result.setClientAuthenticator(ClientCredentialsProviderUtils.bootstrapClientAuthenticator(result));
        result.setClient(new HttpClientBuilder().build(adapterConfig));
        result.setCors(true);

        return result;
    }
}
