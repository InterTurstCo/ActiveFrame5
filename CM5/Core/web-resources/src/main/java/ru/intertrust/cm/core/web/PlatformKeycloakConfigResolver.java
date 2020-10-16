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
import org.springframework.beans.factory.annotation.Value;
import ru.intertrust.cm.core.tools.SpringClient;

public class PlatformKeycloakConfigResolver extends SpringClient implements KeycloakConfigResolver {

    @Value("${keycloak.ralm.name}")
    private String realmName;

    @Value("${keycloak.realm.public.key}")
    private String realmPublicKey;

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Override
    public KeycloakDeployment resolve(HttpFacade.Request facade) {
        KeycloakDeployment result = new KeycloakDeployment();
        result.setRealm(realmName);
        result.setResourceName("cm-client");
        result.setPublicClient(true);
        AdapterConfig adapterConfig = new AdapterConfig();
        adapterConfig.setAuthServerUrl(keycloakUrl);
        result.setAuthServerBaseUrl(adapterConfig);
        result.setSslRequired(SslRequired.NONE);
        result.setResourceCredentials(Collections.singletonMap("secret", "234234-234234-234234"));
        HardcodedPublicKeyLocator pkLocator = new HardcodedPublicKeyLocator(PemUtils.decodePublicKey(realmPublicKey));
        result.setPublicKeyLocator(pkLocator);
        result.setClientAuthenticator(ClientCredentialsProviderUtils.bootstrapClientAuthenticator(result));
        result.setClient(new HttpClientBuilder().build(adapterConfig));
        result.setCors(true);

        return result;
    }
}
