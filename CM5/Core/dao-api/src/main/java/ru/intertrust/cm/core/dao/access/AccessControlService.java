package ru.intertrust.cm.core.dao.access;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.model.AccessException;

/**
 * Интерфейс службы контроля доступа (СКД).
 * <p>Служба контроля доступа является обязательным звеном для выполнения любой операции, требующей специальных
 * разрешений в системе. Каждый базовый сервис, осуществляющий низкоуровневые операции с объектами, требует передачи
 * ему маркера доступа, который клиент сервиса может получить предварительно через службу контроля доступа.
 * При получении маркера доступа указывается субъект, объект и тип доступа, и служба контроля доступа осуществляет
 * проверку прав субъекта на запрошенный доступ. В некоторых случаях могут возвращаться отложенные маркеры доступа,
 * которые означают, что проверка прав будет произведена непосредственно при выполнении доступа.
 * <p>Базовые сервисы, в свою очередь, обращаются к службе контроля доступа для проверки соответствия маркера доступа
 * той операции, которую они должны выполнить. В случае получения отложенного маркера доступа базовый сервис
 * самостоятельно обращается к БД для проверки прав доступа.
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
     * @throws AccessException если пользователь не имеет права на доступ к запрошенному объекту
     */
    AccessToken createAccessToken(String login, Id objectId, AccessType type)
            throws AccessException;

    /**
     * Формирует маркер доступа на чтение коллекции для заданного пользователя. 
     * Список идентификаторов доменных объектов не передается, так как он не известен на этом этапе.
     * Реализация метода должна возвращать отложенный маркер доступа с типом доступа - чтение.
     * 
     * @param userId Идентификатор пользователя
     * @return Сформированный маркер доступа
     * @throws AccessException
     */
    AccessToken createCollectionAccessToken(String login) throws AccessException;

    /**
     * Формирует маркер доступа к группе объектов для заданного пользователя.
     * Сформированный маркер не может быть отложенным, т.е. проверка доступа производится непосредственно
     * при вызове метода.
     * <p>Поведение метода в случае отсутствия доступа к некоторым из запрошенных объектов регулируется
     * параметром requireAll. При использовании значения true метод выбрасывает исключение {@link AccessException},
     * если доступ пользователя хотя бы к одному объекту из запрошенных запрещён. В случае false метод возвращает
     * маркер доступа, позволяющий операции только с теми объектами, доступ к которым пользователю разрешён.
     * 
     * @param userId Идентификатор пользователя
     * @param objectIds Массив идентификаторов объектов
     * @param type Тип доступа
     * @param requireAll true если требуется маркер доступа только ко всем объектам сразу
     * @return Сформированный маркер доступа
     * @throws AccessException если пользователь не имеет права на доступ к некоторым или всем запрошенным объектам,
     *          в зависимости от параметра requireAll
     */
    AccessToken createAccessToken(String login, Id[] objectIds, AccessType type, boolean requireAll)
            throws AccessException;

    /**
     * Формирует маркер доступа на несколько типов доступа к одному объекту для заданного пользователя.
     * Сформированный маркер не может быть отложенным, т.е. проверка доступа производится непосредственно
     * при вызове метода.
     * <p>Поведение метода в случае отсутствия у пользователя разрешений на все запрошенные типы доступа к объекту
     * регулируется параметром requireAll. При передаче значения true в этот параметр метод выбрасывает исключение
     * {@link AccessException}, если хотя бы один тип доступа из запрошенных запрещён. В случае false метод возвращает
     * маркер доступа, позволяющий лишь разрешённые пользователю операции с объектом.
     * 
     * @param userId Идентификатор пользователя
     * @param objectId Идентификатор объекта
     * @param types Массив типов доступа
     * @param requireAll true если требуется маркер только со всеми запрошенными типами доступа
     * @return Сформированный маркер доступа
     * @throws AccessException если пользователь не имеет права на несколько или все типы доступа из запрошенных,
     *          в зависимости от параметра requireAll
     */
    AccessToken createAccessToken(String login, Id objectId, AccessType[] types, boolean requireAll)
            throws AccessException;

    /**
     * Формирует маркер доступа пользователя, позволяющий выполнять административные операции в системе.
     * Требует включения пользователя в группу "Администраторы".
     * 
     * @param userId Идентификатор пользователя
     * @return Сформированный маркер доступа
     * @throws AccessException если пользователь не включён в группу "Администраторы"
     */
    AccessToken createAdminAccessToken(String login) throws AccessException;

    /**
     * Формирует универсальный маркер доступа, позволяющий выполнять любые операции от имени системы.
     * 
     * @param processId Идентификатор процесса
     * @return Универсальный маркер доступа
     * @throws AccessException если доступ не может быть предоставлен
     */
    AccessToken createSystemAccessToken(String processId) throws AccessException;

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
    
    /**
     * Проверяет, что переданный маркер является универсальным маркером доступа. Если маркер нулевой (null) или не
     * является универсальным, выбрасывает исключение {@link AccessException}.
     * @param accessToken Маркер доступа
     * @throws AccessException если маркер нулевой или не является универсальным маркером доступа
     */
    public void verifySystemAccessToken(AccessToken accessToken) throws AccessException;
    
    /**
     * Формирует маркер доступа на создание доменного объекта данного типа для переданного пользователя.
     * @param login Идентификатор пользователя
     * @param domainObject доменный объект
     * @return
     * @throws AccessException
     */
    AccessToken createDomainObjectCreateToken(String login, DomainObject domainObject) throws AccessException;
}
