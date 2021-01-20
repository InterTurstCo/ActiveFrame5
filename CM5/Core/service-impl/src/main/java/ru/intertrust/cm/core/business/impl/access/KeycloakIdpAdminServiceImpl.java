package ru.intertrust.cm.core.business.impl.access;

import java.util.Collections;
import java.util.stream.Collectors;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.ClientBuilderWrapper;
import org.keycloak.admin.client.JacksonProvider;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import ru.intertrust.cm.core.business.api.access.CredentialInfo;
import ru.intertrust.cm.core.business.api.access.IdpConfig;
import ru.intertrust.cm.core.business.api.access.IdpAdminService;
import ru.intertrust.cm.core.business.api.access.UserInfo;
import ru.intertrust.cm.core.model.FatalException;

import javax.annotation.PostConstruct;
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

        config = new KeycloakConfig();
        config.setServerUrl(url);
        config.setRealm(realmName);
        config.setClientId(clientId);
        config.setAdminClientId(adminClientId);
        config.setAdminSecret(adminSecret);
        config.setDisableTrustManager(disableTrustManager);
        config.setTruststore(truststore);
        config.setTruststorePassword(truststorePassword);
        config.setIdpAuthentication(idpAuthentication);

        if (url != null && !url.isEmpty()) {

            keycloak = KeycloakBuilder.builder().
                    serverUrl(config.getServerUrl()).
                    realm(config.getRealm()).
                    clientId(config.getAdminClientId()).
                    clientSecret(config.getAdminSecret()).
                    grantType(OAuth2Constants.CLIENT_CREDENTIALS).
                    resteasyClient(createRestClient(
                            createSslContext(config.getTruststore(), config.getTruststorePassword()),
                            config.isDisableTrustManager())).
                    build();
        }
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
        }catch(Exception ex){
            throw new FatalException("Error create ssl context", ex);
        }
    }

    private ResteasyClient createRestClient(SSLContext sslContext, boolean disableTrustManager) {
        ClientBuilder clientBuilder = ClientBuilderWrapper.create(sslContext, disableTrustManager);
        clientBuilder.register(JacksonProvider.class, 100);
        return (ResteasyClient)clientBuilder.build();
    }

    @Override
    public String createUser(UserInfo userInfo) {
        UserRepresentation userRepresentation = newUserRepresentation(userInfo);

        Response response = keycloak.realm(config.getRealm()).users().create(userRepresentation);
        if (response.getStatus() != 201){
            throw new FatalException("Error create user");
        }

        UserInfo createUserInfo = findUserByUserName(userInfo.getUsername());
        return createUserInfo.getUnid();
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
        keycloak.realm(config.getRealm()).users().delete(unid);
    }

    @Override
    public UserInfo getUserByUnid(String unid) {
        UserRepresentation userRepresentation = keycloak.realm(config.getRealm()).users().get(unid).toRepresentation();

        return getUserInfo(userRepresentation);
    }

    @Override
    public UserInfo findUserByUserName(String userName) {

        init();

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
        init();

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
}