package ru.intertrust.cm.core.gui.model.session;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Объект обертка с информацией о таймауте клиентской сессии.<br>
 * <br>
 * Created by Myskin Sergey on 27.07.2020.
 */
public class SessionTimeoutResponseDto implements Dto {

    /**
     * Таймаут клиентской сессии в минутах
     */
    private Integer sessionTimeout;

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

}
