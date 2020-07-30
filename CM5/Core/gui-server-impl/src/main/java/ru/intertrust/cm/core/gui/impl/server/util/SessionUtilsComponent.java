package ru.intertrust.cm.core.gui.impl.server.util;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.session.MakeServerSessionDirtyResponseDto;
import ru.intertrust.cm.core.gui.model.session.SessionTimeoutResponseDto;

/**
 * Набор утилит для работы с сессией.<br>
 * <br>
 * Created by Myskin Sergey on 27.07.2020.
 */
@ComponentName("session.utils.component")
public class SessionUtilsComponent implements ComponentHandler {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Возвращает таймаут сессии, после которого на клиенте должен происходить разлогин.<br>
     *
     * @param dto объект с параметрами (не используется, присутствует из-за требований к сигнатуре метода)
     * @return объект-обертка с информацией о таймауте для передачи на клиент
     */
    public SessionTimeoutResponseDto getSessionTimeout(Dto dto) {
        final Integer sessionTimeout = authenticationService.getSessionTimeout();

        final SessionTimeoutResponseDto sessionTimeoutResponseDto = new SessionTimeoutResponseDto();
        sessionTimeoutResponseDto.setSessionTimeout(sessionTimeout);

        return sessionTimeoutResponseDto;
    }

    /**
     * "Загрязняет" серверную сессию делая ложный запрос, который ничего не меняет.<br>
     * Это необходимо для того, чтобы на сервере никогда не наступал таймаут сессии,
     * для того, чтобы управлять временем простоя на клиенте.
     *
     * @param dto объект-обертка аргумента без каких-либо данных (только из-за требования сигнатуры)
     * @return объект-обертка ответа без каких-либо данных (только из-за требования сигнатуры)
     */
    public MakeServerSessionDirtyResponseDto makeServerSessionDirty(Dto dto) {
        return new MakeServerSessionDirtyResponseDto();
    }

}
