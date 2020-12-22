package ru.intertrust.cm.test.acess;

import java.util.List;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.access.IdpConfig;
import ru.intertrust.cm.core.business.api.access.IdpAdminService;
import ru.intertrust.cm.core.business.api.access.UserInfo;
import ru.intertrust.cm.core.gui.impl.server.cmd.model.ErrorPlatformWebServiceResult;
import ru.intertrust.cm.core.gui.impl.server.cmd.model.PlatformWebService;
import ru.intertrust.cm.core.gui.impl.server.cmd.model.PlatformWebServiceResult;
import ru.intertrust.cm.core.gui.impl.server.cmd.model.SuccessPlatformWebServiceResult;
import ru.intertrust.cm.core.model.FatalException;

public class TestKeycloakAdmin implements PlatformWebService {

    private static Logger logger = LoggerFactory.getLogger(TestKeycloakAdmin.class);

    @Autowired
    private IdpAdminService idpAdminService;

    @Override
    public PlatformWebServiceResult execute(List<FileItem> bytes, Map<String, String[]> data) {
        try {
            logger.info("Start test KeycloakAdmin");

            // Поиск
            // Поиск несуществующего
            UserInfo findUserInfoTest = idpAdminService.findUserByUserName("xxx");
            if (findUserInfoTest != null){
                throw new FatalException("Incorrect find not exists user");
            }

            // Создание
            UserInfo userInfo = new UserInfo();
            userInfo.setUsername("user_" + System.currentTimeMillis());
            userInfo.setEnable(true);
            userInfo.setLastName("last_name_" + System.currentTimeMillis());
            userInfo.setFirstName("first_name_" + System.currentTimeMillis());
            userInfo.setEmail("" + System.currentTimeMillis() + "@cm.ru");
            String newUserUnid = idpAdminService.createUser(userInfo);
            if (newUserUnid == null){
                throw new FatalException("Error create user");
            }

            // Получение по ID
            UserInfo getUserInfo = idpAdminService.getUserByUnid(newUserUnid);
            if (getUserInfo == null ||
                    !userInfo.getEmail().equals(getUserInfo.getEmail()) ||
                    !userInfo.getFirstName().equals(getUserInfo.getFirstName()) ||
                    !userInfo.getLastName().equals(getUserInfo.getLastName()) ||
                    !userInfo.getUsername().equals(getUserInfo.getUsername()) ||
                    userInfo.isEnable() != getUserInfo.isEnable()){
                throw new FatalException("Get user incorrect");
            }

            // Поиск по username
            UserInfo findUserInfo = idpAdminService.findUserByUserName(userInfo.getUsername());
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
            String updateUnid = idpAdminService.updateUser(findUserInfo);
            if (!updateUnid.equals(newUserUnid)){
                throw new FatalException("Incorrect update return value");
            }

            UserInfo updateUserInfo = idpAdminService.getUserByUnid(updateUnid);
            if (updateUserInfo == null ||
                    !updateUserInfo.getEmail().equals(findUserInfo.getEmail()) ||
                    !updateUserInfo.getFirstName().equals(findUserInfo.getFirstName()) ||
                    !updateUserInfo.getLastName().equals(findUserInfo.getLastName()) ||
                    updateUserInfo.isEnable() != findUserInfo.isEnable()){
                throw new FatalException("Update user incorrect");
            }

            // Получение конфигурации
            IdpConfig config = idpAdminService.getConfig();
            if (config == null ||
                    config.getAdminClientId() == null ||
                    config.getAdminSecret() == null ||
                    config.getClientId() == null ||
                    config.getRealm() == null ||
                    config.getServerUrl() == null){
                throw new FatalException("Config is incorrect");
            }

            // Включение пользователя
            idpAdminService.enableUser(newUserUnid);
            UserInfo enableUserInfo = idpAdminService.getUserByUnid(newUserUnid);
            if (!enableUserInfo.isEnable()){
                throw new FatalException("Incorrect enable user");
            }

            // Выключение пользователя
            idpAdminService.disableUser(newUserUnid);
            UserInfo disabledUserInfo = idpAdminService.getUserByUnid(newUserUnid);
            if (disabledUserInfo.isEnable()){
                throw new FatalException("Incorrect disable user");
            }

            // Удаление пользователя
            idpAdminService.deleteUser(newUserUnid);

            // Проверка что не найден
            UserInfo deleteUserInfo = idpAdminService.findUserByUserName(updateUserInfo.getUsername());
            if (deleteUserInfo != null){
                throw new FatalException("Incorrect delete user");
            }

            // Проверка что не найден 2
            try {
                deleteUserInfo = idpAdminService.getUserByUnid(newUserUnid);
                throw new FatalException("Incorrect get deleted user");
            }catch(NotFoundException correctException){
                logger.info("Correct error");
            }

            logger.info("Finish test KeycloakAdmin");
            return new SuccessPlatformWebServiceResult();
        }catch (Exception ex){
            logger.error("Error test KeycloakAdmin", ex);
            return new ErrorPlatformWebServiceResult(ex.toString());
        }
    }
}
