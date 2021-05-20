package ru.intertrust.cm.core.business.impl.access;

import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.ClientBuilderWrapper;
import org.keycloak.admin.client.JacksonProvider;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import reactor.util.StringUtils;
import ru.intertrust.cm.core.business.api.access.CredentialInfo;
import ru.intertrust.cm.core.business.api.access.IdpConfig;
import ru.intertrust.cm.core.business.api.access.IdpAdminService;
import ru.intertrust.cm.core.business.api.access.UserInfo;
import ru.intertrust.cm.core.model.FatalException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.List;

public class KeycloakIdpAdminServiceImpl implements IdpAdminService {

    @Value("${keycloak.realm.name:}")
    private String realmName;

    @Value("${keycloak.url:}")
    private String url;

    @Value("${keycloak.client.id:}")
    private String clientId;

    @Value("${keycloak.admin.client.id:}")
    private String adminClientId;

    @Value("${keycloak.admin.secret:}")
    private String adminSecret;

    @Value("${keycloak.disable.trust.manager:false}")
    private boolean disableTrustManager;

    @Value("${keycloak.truststore:}")
    private String truststore;

    @Value("${keycloak.truststore.password:}")
    private String truststorePassword;

    @Value("${idp.authentication:false}")
    private boolean idpAuthentication;

    private Keycloak keycloak;
    private KeycloakConfig config;

    @PostConstruct
    public void init() {

        config = KeycloakConfig.getBuilder()
                .setServerUrl(url)
                .setRealm(realmName)
                .setAdminClientId(adminClientId)
                .setAdminSecret(adminSecret)
                .setClientId(clientId)
                .setEnableSsl(StringUtils.startsWithIgnoreCase(url, "https"))
                .setDisableTrustManager(disableTrustManager)
                .setTruststore(truststore)
                .setTruststorePassword(truststorePassword)
                .setIdpAuthentication(idpAuthentication)
                .createKeycloakConfig();


        if (url != null && !url.isEmpty()) {

            keycloak = KeycloakBuilder.builder().
                    serverUrl(config.getServerUrl()).
                    grantType(OAuth2Constants.CLIENT_CREDENTIALS).
                    realm(config.getRealm()).
                    clientId(config.getAdminClientId()).
                    clientSecret(config.getAdminSecret()).
                    resteasyClient(createRestClient()).
                    build();
        }
    }

    @PreDestroy
    public void preDestroy() {
        keycloak.close();
    }

    private ResteasyClient createRestClient() {
        ClientBuilder clientBuilder;
        if (config.isSslEnabled()) {
            SSLContext sslContext = createSslContext(config.getTruststore(), config.getTruststorePassword());
            clientBuilder = ClientBuilderWrapper.create(sslContext, disableTrustManager);
        } else {
            clientBuilder = new ResteasyClientBuilder().connectionPoolSize(10);
        }
        clientBuilder.register(JacksonProvider.class, 100);
        return (ResteasyClient) clientBuilder.build();
    }

    private SSLContext createSslContext(String trustStore, String trustStorePassword){
        try {

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream(trustStore),
                    trustStorePassword.toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(ks);

            SSLContext sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch(Exception ex) {
            throw new FatalException("Error create ssl context", ex);
        }
    }

    @Override
    public String createUser(UserInfo userInfo) {
        UserRepresentation userRepresentation = newUserRepresentation(userInfo);
        try (Response response = keycloak.realm(config.getRealm()).users().create(userRepresentation)) {
            if (response.getStatus() != 201) {
                String reason = getRawMessage(response);
                throw new FatalException("Error create user = " + userInfo
                        + ". Error code = " + response.getStatus() + ". Reason = " + reason
                );
            }
        }

        UserInfo createUserInfo = findUserByUserName(userInfo.getUsername());
        return createUserInfo.getUnid();
    }

    private String getRawMessage(Response response) {
        ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
        String message = errorMessage.getErrorMessage();
        if (message == null) {
            message = errorMessage.getError();
        }
        return message;
    }

    @Override
    public String updateUser(UserInfo userInfo) {
        UserRepresentation userRepresentation = newUserRepresentation(userInfo);
        userRepresentation.setId(userInfo.getUnid());

        keycloak.realm(config.getRealm()).users().get(userInfo.getUnid()).update(userRepresentation);
        return userInfo.getUnid();
    }

    private UserRepresentation newUserRepresentation(UserInfo userInfo) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(userInfo.getUsername());
        userRepresentation.setEmail(userInfo.getEmail());
        userRepresentation.setFirstName(userInfo.getFirstName());
        userRepresentation.setLastName(userInfo.getLastName());
        userRepresentation.setEnabled(userInfo.isEnable());
        userRepresentation.setAttributes(userInfo.getAttributes());
        userRepresentation.setRequiredActions(userInfo.getRequiredActions());

        List<CredentialRepresentation> credentials = userInfo.getCredentialInfoList().stream()
                .map(this::newCredentialRepresentation)
                .collect(Collectors.toList());
        userRepresentation.setCredentials(credentials);

        return userRepresentation;
    }

    private CredentialRepresentation newCredentialRepresentation(CredentialInfo credentialInfo) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(credentialInfo.isTemporary());
        credential.setValue(credentialInfo.getValue());
        credential.setType(credentialInfo.getType());
        return credential;
    }

    @Override
    public void deleteUser(String unid) {
        try (Response response = keycloak.realm(config.getRealm()).users().delete(unid)) {

            if (response.getStatus() != 204) {
                String reason = getRawMessage(response);
                throw new FatalException("Error delete unid = " + unid
                        + ". Error code = " + response.getStatus() + ". Reason = " + reason
                );
            }
        }
    }

    @Override
    public UserInfo getUserByUnid(String unid) {
        UserRepresentation userRepresentation = keycloak.realm(config.getRealm()).users().get(unid).toRepresentation();

        return getUserInfo(userRepresentation);
    }

    @Override
    public UserInfo findUserByUserName(String userName) {
        List<UserRepresentation> searchResult = keycloak.realm(config.getRealm()).users().search(userName, true);
        if (searchResult.size() == 0 ) {
            return null;
        } else if (searchResult.size() == 1) {
            return getUserInfo(searchResult.get(0));
        } else {
            throw new FatalException("Find more then one user with name " + userName);
        }
    }

    @Override
    public List<UserInfo> findUsersByUserName(String userName) {
        List<UserRepresentation> searchResult = keycloak.realm(config.getRealm()).users().search(userName);
        return searchResult.stream().map(this::getUserInfo).collect(Collectors.toList());
    }

    private UserInfo getUserInfo(UserRepresentation userRepresentation) {
        UserInfo result = new UserInfo();
        result.setEmail(userRepresentation.getEmail());
        result.setEnable(userRepresentation.isEnabled());
        result.setFirstName(userRepresentation.getFirstName());
        result.setLastName(userRepresentation.getLastName());
        result.setUnid(userRepresentation.getId());
        result.setUsername(userRepresentation.getUsername());

        return result;
    }

    @Override
    public void disableUser(String unid) {
        UserInfo userInfo = getUserByUnid(unid);
        if (userInfo.isEnable()) {
            userInfo.setEnable(false);
            updateUser(userInfo);
        }
    }

    @Override
    public void enableUser(String unid) {
        UserInfo userInfo = getUserByUnid(unid);
        if (!userInfo.isEnable()) {
            userInfo.setEnable(true);
            updateUser(userInfo);
        }
    }

    @Override
    public void sendEmail(String unid, List<String> requiredActions) {
        keycloak.realm(config.getRealm()).users().get(unid).executeActionsEmail(requiredActions);
    }

    @Override
    public IdpConfig getConfig() {
        return config;
    }

    @JsonAutoDetect
    private static class ErrorMessage {

        private final String errorMessage;
        private final String error;

        @JsonCreator
        public ErrorMessage(@JsonProperty("errorMessage") String errorMessage, @JsonProperty("error") String error) {
            this.errorMessage = errorMessage;
            this.error = error;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getError() {
            return error;
        }
    }
}