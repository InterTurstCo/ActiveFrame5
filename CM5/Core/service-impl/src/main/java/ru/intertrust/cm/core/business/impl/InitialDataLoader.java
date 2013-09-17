package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;

/**
 * Класс, предназначенный для загрузки конфигурации доменных объектов
 * @author vmatsukevich
 *         Date: 5/6/13
 *         Time: 9:36 AM
 */
public class InitialDataLoader {

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    @Autowired
    private AuthenticationService authenticationService;

    public InitialDataLoader() {
    }

    /**
     * Устанавливает {@link #authenticationService}
     * @param authenticationService сервис для работы с конфигурацией доменных объектов
     */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Загружает конфигурацию доменных объектов, валидирует и создает соответствующие сущности в базе.
     * Добавляет запись администратора (admin/admin) в таблицу authentication_info.
     * @throws Exception
     */
    public void load() throws Exception {
        if (!authenticationService.existsAuthenticationInfo(ADMIN_LOGIN)) {
            insertAdminAuthenticationInfo();
        }
    }

    private void insertAdminAuthenticationInfo() {
        AuthenticationInfoAndRole admin = new AuthenticationInfoAndRole();
        admin.setUserUid(ADMIN_LOGIN);
        admin.setPassword(ADMIN_PASSWORD);
        admin.setRole("admin");
        authenticationService.insertAuthenticationInfoAndRole(admin);
    }

}
