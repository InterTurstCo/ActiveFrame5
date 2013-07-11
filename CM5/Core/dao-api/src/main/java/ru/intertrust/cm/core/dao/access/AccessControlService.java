package ru.intertrust.cm.core.dao.access;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.exception.AccessException;

/**
 * Интерфейс службы контроля доступа (СКД)
 * 
 * @author apirozhkov
 */
public interface AccessControlService {

    /**
     * Формирует маркер доступа к объекту для заданного пользователя.
     * <p>Реализация метода, исходя из параметров метода, внутреннего состоаяния и соображений эффективности,
     * вольна выбрать один из двух вариантов:
     * <ul>
     * <li>сформировать простой маркер доступа. В этом случае она должна произвести проверку прав пользователя
     * на запрошенный тип доступа к запрошенному объекту. В случае отсутствия прав выбрасывается исключение
     * {@link AccessException};
     * <li>сформировать отложенный маркер доступа, возложив таким образом ответственность за проверку прав
     * на базовый сервис.
     * </ul>
     * 
     * @param userId Идентификатор пользователя
     * @param objectId Идентификатор доменного объекта
     * @param type Тип доступа
     * @return Сформированный маркер доступа
     * @throws AccessException если пользователь не имеет права на выполнение запрошенной операции
     */
    AccessToken createAccessToken(int userId, Id objectId, AccessType type)
            throws AccessException;

    AccessToken createAccessToken(int userId, Id[] objectIds, AccessType type, boolean requireAll)
            throws AccessException;

    AccessToken createAccessToken(int userId, Id objectId, AccessType[] types, boolean requireAll)
            throws AccessException;

    /**
     * Формирует универсальный маркер доступа, позволяющий выполнять любые операции от имени системы.
     * 
     * @param processId Идентификатор процесса
     * @return Универсальный маркер доступа
     * @throws AccessException если доступ не может быть предоставлен
     */
    AccessToken createAccessToken(String processId) throws AccessException;

    /**
     * Проверяет соответствие маркера доступа указанному типу доступа к указанному объекту. В случае несоответствия
     * метод выбрасывает исключение {@link AccessException}.
     * <p>Этот метод обязаны использовать базовые сервисы перед проведением защищаемых операций с объектами.
     * Кроме того, если маркер доступа является отложенным, обязанностью базового сервиса является самостоятельное
     * выполнение надлежащих проверок, предпочтительно, путём внедрения их в запросы, выполняющие саму операцию.
     * Если базовый сервис не поддерживает самостоятельную проверку прав, то при получении отложенного маркера доступа
     * он должен выбросить исключение {@link ru.intertrust.cm.core.dao.exception.DeferredAccessTokenNotSupportedException}
     * <p>Реализация службы контроля доступа должна принимать в качестве допустимых маркеров доступа только те,
     * которые были созданы ранее ей самой.
     * 
     * @param token Маркер доступа
     * @param objectId Идентификатор объекта доступа
     * @param type Тип доступа
     * @throws AccessException если маркер доступа не соответствует запрошенному доступу
     */
    void verifyAccessToken(AccessToken token, Id objectId, AccessType type) throws AccessException;
}
