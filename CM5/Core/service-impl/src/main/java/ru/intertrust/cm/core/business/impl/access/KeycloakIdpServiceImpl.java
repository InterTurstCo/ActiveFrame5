package ru.intertrust.cm.core.business.impl.access;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import ru.intertrust.cm.core.business.api.access.IdpConfig;
import ru.intertrust.cm.core.business.api.access.IdpService;
import ru.intertrust.cm.core.business.api.access.UserInfo;
import ru.intertrust.cm.core.model.FatalException;

public class KeycloakIdpServiceImpl implements IdpService {

    @Value("${keycloak.ralm.name}")
    private String realmName;

    @Value("${keycloak.realm.public.key}")
    private String realmPublicKey;

    @Value("${keycloak.url}")
    private String url;

    @Value("${keycloak.client.id}")
    private String clientId;

    @Value("${keycloak.admin.login}")
    private String adminLogin;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.disable.trust.manager:false}")
    private boolean disableTrustManager;

    @Value("${keycloak.truststore:}")
    private String truststore;

    @Value("${keycloak.truststore.password:}")
    private String truststorePassword;

    private Keycloak keycloak;
    private KeycloakConfig config;


    @PostConstruct
    public void init(){

        config = new KeycloakConfig();
        config.setRealm(realmName);
        config.setServerUrl(url);
        config.setRealmPublicKey(realmPublicKey);
        config.setClientId(clientId);
        config.setAdminLogin(adminLogin);
        config.setAdminPassword(adminPassword);
        config.setDisableTrustManager(disableTrustManager);
        config.setTruststore(truststore);
        config.setTruststorePassword(truststorePassword);

        keycloak = Keycloak.getInstance(
                config.getServerUrl(),
                "master",
                config.getAdminLogin(),
                config.getAdminPassword(),
                "admin-cli");
    }

    @Override
    public String createUser(UserInfo userInfo) {

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(userInfo.getUsername());
        userRepresentation.setEmail(userInfo.getEmail());
        userRepresentation.setFirstName(userInfo.getFirstName());
        userRepresentation.setLastName(userInfo.getLastName());
        userRepresentation.setEnabled(userInfo.isEnable());

        Response response = keycloak.realm(config.getRealm()).users().create(userRepresentation);
        if (response.getStatus() != 201){
            throw new FatalException("Error create user");
        }

        UserInfo createUserInfo = findUserByUserName(userInfo.getUsername());
        return createUserInfo.getUnid();
    }

    @Override
    public String updateUser(UserInfo userInfo) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(userInfo.getUnid());
        userRepresentation.setUsername(userInfo.getUsername());
        userRepresentation.setEmail(userInfo.getEmail());
        userRepresentation.setFirstName(userInfo.getFirstName());
        userRepresentation.setLastName(userInfo.getLastName());
        userRepresentation.setEnabled(userInfo.isEnable());

        keycloak.realm(config.getRealm()).users().get(userInfo.getUnid()).update(userRepresentation);
        return userInfo.getUnid();
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
        List<UserRepresentation> searchResult = keycloak.realm(config.getRealm()).users().search(userName);
        if (searchResult.size() == 0 ){
            return null;
        }else if(searchResult.size() == 1){
            return getUserInfo(searchResult.get(0));
        }else{
            throw new FatalException("Find more then one user with name " + userName);
        }
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
    public IdpConfig getConfig() {
        return config;
    }
}