package ru.intertrust.cm.core.business.api;

/**
 * Сервис, предназначенный для координации выполнения общих задач (например,
 * инициализации) между несколькими серверами, работающими на одной БД. Сервис
 * не занимается сопоставлением имени ресурса с именем конкретной задачи или
 * ресурса в системе и не осуществляет их блокировку - он используется только
 * для передачи информации. Контроль корректности работы в соответствии с этой
 * информацией всецело лежит на клиентском коде.<h3>
 * Пример использования</h3>
 * 
 * <pre>
 * {@code
 * //Пытаемся получить ресурс
 * if(interserverLockingService.lock("resource")){
 *      //Успешно получили в пользование ресурс, выполняем действия с ним...
 *      //...
 *      //Освобождаем ресурс
 *      interserverLockingService.unlock("resource");
 * } else{
 *      //Ресурс уже заблокировал другой сервер из кластера, ждем пока действия с ресурсом выполнит он...
 *      interserverLockingService.waitUntilNotLocked("resource");
 * }
 * 
 * </pre>
 * @author erentsov
 * 
 */
public interface InterserverLockingService {

    public interface Remote extends InterserverLockingService {
    }
    
    /**
     * Попытаться заблокировать ресурс
     * @param resourceId
     *            Имя ресурса
     * @return <code>true</code>, если удалось заблокировать <code>false</code>
     *         в противном случае
     */
    boolean lock(String resourceId);

    /**
     * @param resourceId
     * @return
     */
    boolean isLocked(String resourceId);

    /**
     * Разблокирует ресурс. Только сервер, заблокировавший ресурс, может его
     * разблокировать. Попытка разблокировать незаблокированный текущим сервером
     * ресурс вызывает {@link RuntimeException}
     * @param resourceId
     *            Имя ресурса
     */
    void unlock(String resourceId);

    /**
     * Ждать, пока заблокированный ресурс не освободится.
     * @param resourceId
     *            Имя ресурса
     */
    void waitUntilNotLocked(String resourceId);

    void waitUntilActualData(String resourceId, String stampInfo);

    /**
     * Попытка заблокировать ресурс, с учетом того, какой объект пытается
     * наложить блокировку.
     * @param resourceId
     *            идентификатор (имя) ресурса
     * @return <code>true</code>, если удалось заблокировать, или если ресурс
     *         уже заблокирован объектом, накладывающим блокировку
     *         <code>false</code> в противном случае
     */
    boolean selfSharedLock(String resourceId);

}
