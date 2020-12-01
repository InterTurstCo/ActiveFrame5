package ru.intertrust.cm.test.acess;

import java.util.List;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.access.IdpConfig;
import ru.intertrust.cm.core.business.api.access.IdpService;
import ru.intertrust.cm.core.business.api.access.UserInfo;
import ru.intertrust.cm.core.gui.impl.server.cmd.model.ErrorPlatformWebServiceResult;
import ru.intertrust.cm.core.gui.impl.server.cmd.model.PlatformWebService;
import ru.intertrust.cm.core.gui.impl.server.cmd.model.PlatformWebServiceResult;
import ru.intertrust.cm.core.gui.impl.server.cmd.model.SuccessPlatformWebServiceResult;
import ru.intertrust.cm.core.model.FatalException;

public class TestKeycloakAdmin implements PlatformWebService {

    private static Logger logger = LoggerFactory.getLogger(TestKeycloakAdmin.class);

    @Autowired
    private IdpService idpService;

    @Override
    public PlatformWebServiceResult execute(List<FileItem> bytes, Map<String, String[]> data) {
        try {
            logger.info("Start test KeycloakAdmin");

            // Создание
            UserInfo userInfo = new UserInfo();
            userInfo.setUsername("user_" + System.currentTimeMillis());
            userInfo.setEnable(true);
            userInfo.setLastName("last_name_" + System.currentTimeMillis());
            userInfo.setFirstName("first_name_" + System.currentTimeMillis());
            userInfo.setEmail("" + System.currentTimeMillis() + "@cm.ru");
            String newUserUnid = idpService.createUser(userInfo);
            if (newUserUnid == null){
                throw new FatalException("Error create user");
            }

            // Получение по ID
            UserInfo getUserInfo = idpService.getUserByUnid(newUserUnid);
            if (getUserInfo == null ||
                    !userInfo.getEmail().equals(getUserInfo.getEmail()) ||
                    !userInfo.getFirstName().equals(getUserInfo.getFirstName()) ||
                    !userInfo.getLastName().equals(getUserInfo.getLastName()) ||
                    !userInfo.getUsername().equals(getUserInfo.getUsername()) ||
                    userInfo.isEnable() != getUserInfo.isEnable()){
                throw new FatalException("Get user incorrect");
            }

            // Поиск по username
            UserInfo findUserInfo = idpService.findUserByUserName(userInfo.getUsername());
            if (findUserInfo == null ||
                    !userInfo.getEmail().equals(findUserInfo.getEmail()) ||
                    !userInfo.getFirstName().equals(findUserInfo.getFirstName()) ||
                    !userInfo.getLastName().equals(findUserInfo.getLastName()) ||
                    !userInfo.getUsername().equals(findUserInfo.getUsername()) ||
                    userInfo.isEnable() != getUserInfo.isEnable()){
                throw new FatalException("Find user incorrect");
            }

            // Изменение пользователя
            findUserInfo.setLastName("last_name_" + System.currentTimeMillis());
            findUserInfo.setFirstName("first_name_" + System.currentTimeMillis());
            findUserInfo.setEmail("" + System.currentTimeMillis() + "@cm.ru");
            findUserInfo.setEnable(false);
            String updateUnid = idpService.updateUser(findUserInfo);
            if (!updateUnid.equals(newUserUnid)){
                throw new FatalException("Incorrect update return value");
            }

            UserInfo updateUserInfo = idpService.getUserByUnid(updateUnid);
            if (updateUserInfo == null ||
                    !updateUserInfo.getEmail().equals(findUserInfo.getEmail()) ||
                    !updateUserInfo.getFirstName().equals(findUserInfo.getFirstName()) ||
                    !updateUserInfo.getLastName().equals(findUserInfo.getLastName()) ||
                    updateUserInfo.isEnable() != findUserInfo.isEnable()){
                throw new FatalException("Update user incorrect");
            }

            // Получение конфигурации
            IdpConfig config = idpService.getConfig();
            if (config == null ||
                    config.getAdminLogin() == null ||
                    config.getAdminPassword() == null ||
                    config.getClientId() == null ||
                    config.getRealm() == null ||
                    config.getRealmPublicKey() == null ||
                    config.getServerUrl() == null){
                throw new FatalException("Config is incorrect");
            }

            // Включение пользователя
            idpService.enableUser(newUserUnid);
            UserInfo enableUserInfo = idpService.getUserByUnid(newUserUnid);
            if (!enableUserInfo.isEnable()){
                throw new FatalException("Incorrect enable user");
            }

            // Выключение пользователя
            idpService.disableUser(newUserUnid);
            UserInfo disabledUserInfo = idpService.getUserByUnid(newUserUnid);
            if (disabledUserInfo.isEnable()){
                throw new FatalException("Incorrect disable user");
            }

            // Удаление пользователя
            idpService.deleteUser(newUserUnid);

            // Проверка что не найден
            UserInfo deleteUserInfo = idpService.findUserByUserName(updateUserInfo.getUsername());
            if (deleteUserInfo != null){
                throw new FatalException("Incorrect delete user");
            }

            // Проверка что не найден 2
            try {
                deleteUserInfo = idpService.getUserByUnid(newUserUnid);
                throw new FatalException("Incorrect get deleted user");
            }catch(NotFoundException correctException){
                System.out.println("Error OK");
            }

            logger.info("Finish test KeycloakAdmin");
            return new SuccessPlatformWebServiceResult();
        }catch (Exception ex){
            logger.error("Error test KeycloakAdmin", ex);
            return new ErrorPlatformWebServiceResult(ex.toString());
        }
    }
}
