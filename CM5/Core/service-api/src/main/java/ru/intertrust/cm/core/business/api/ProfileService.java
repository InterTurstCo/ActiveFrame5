package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.PersonProfile;
import ru.intertrust.cm.core.business.api.dto.Profile;

/**
 * Сервис профиля системы и пользователей
 * @author larin
 * 
 */
public interface ProfileService {
    public static final String LOCALE = "LOCALE";

    public interface Remote extends ProfileService {
    }

    /**
     * Получение профиля системы. Профиль содержит данные профиля без учета иерархии профилей. Предназначен для
     * редактирования системных профилей администраторами при его вызове должен создаваться AdminAccessToken
     * @param name имя профиля
     * @return
     */
    Profile getProfile(String name);

    /**
     * Получения профиля персоны. Профиль содержит данные профиля без учета иерархии профилей. Предназначен для
     * редактирования пользовательских профилей администраторами. При его вызове должен создаваться AdminAccessToken
     * @param personId
     * @return
     */
    Profile getPersonProfile(Id personId);

    /**
     * Сохранения профиля системы. Профиль содержит данные профиля без учета иерархии профилей. Предназначен для
     * редактирования системных профилей администраторами при его вызове должен создаваться AdminAccessToken
     */
    void setProfile(Profile profile);
    
    /**
     * Получение пользовательского профиля. Профиль содержит данные профиля пользователя с учетом иерархии профилей. 
     * Предназначен для работы под провами простого пользователя 
     * @return
     */
    PersonProfile getPersonProfile();

    /**
     * Получение пользовательского профиля. Профиль содержит данные профиля пользователя с учетом иерархии профилей.
     * Предназначен для работы под провами простого пользователя
     * @return
     */
    PersonProfile getPersonProfileByPersonId(Id personId);

    /**
     * Сохранение пользовательского профиля. Профиль содержит данные профиля пользователя с учетом иерархии профилей. 
     * Предназначен для работы под провами простого пользователя.
     * При сохранения профиля меняются данные только профиля пользователя. Данные системных профилей остаются не изменными. 
     * @param profile
     */
    void setPersonProfile(PersonProfile profile);

    /**
     * Получение локали из профиля текущего пользователя
     * @return
     */
    String getPersonLocale();
}
