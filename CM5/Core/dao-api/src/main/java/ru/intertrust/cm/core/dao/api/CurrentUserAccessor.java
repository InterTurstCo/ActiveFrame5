package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Предоставляет доступ к текущему пользователю системы.
 * @author atsvetkov
 *
 */
public interface CurrentUserAccessor {

    String getCurrentUser();
    
    Id getCurrentUserId();

}
