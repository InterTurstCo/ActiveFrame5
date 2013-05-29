package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.business.api.MD5Service;
import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfo;
import ru.intertrust.cm.core.dao.api.AuthenticationDAO;

/**
 * Реализация сервиса для работы с бизнес-объектом Person
 * @author atsvetkov
 * 
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    private MD5Service md5Service;

    private AuthenticationDAO authenticationDAO;

    /**
     * Добавляет пользователя в базу. Кодирует пароль, использую MD5 алгоритм. В базу сохраняется MD5 хеш значение
     * пароля.
     * @param authenticationInfo {@link AuthenticationInfo}
     */
    @Override
    public void insertAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        String enteredPassword = authenticationInfo.getPassword();
        String passwordHash = md5Service.getMD5(enteredPassword);
        authenticationInfo.setPassword(passwordHash);

        authenticationDAO.insertAuthenticationInfo(authenticationInfo);
    }

   /**
     * Проверяет сужествует ли пользователь с указанным логином.
     * @param login логин пользователя
     * @return true, если существует пользователь, false иначе
     */
    public boolean existsAuthenticationInfo(String login) {
        return authenticationDAO.existsAuthenticationInfo(login);
    }

    /**
     * Устанавливает {@see #md5Service}. Используется для кодирования паролей пользователей.
     * 
     * @param md5Service
     */
    public void setMd5Service(MD5Service md5Service) {
        this.md5Service = md5Service;
    }

    /**
     * Устанавливает {@see #authenticationDAO}.
     * @param personDAO
     */
    public void setAuthenticationDAO(AuthenticationDAO authenticationDAO) {
        this.authenticationDAO = authenticationDAO;
    }
   
}
