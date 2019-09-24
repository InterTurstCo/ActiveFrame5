package ru.intertrust.cm.core.business.api;

import java.time.Duration;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.ClusteredLock;

/**
 * Cервис распределенных блокировок
 * @author larin
 *
 */
public interface ClusteredLockService {

    public interface Remote extends ClusteredLockService {
    }
    /**
     * Список захваченных блокировок определённой категории, т.е. у всех isLocked()==true
     * @param category категория блокировки
     * @return Экземпляр блокировки
     */
    Set<ClusteredLock> list(String category);

    /**
     * Установка блокировки. Метод устанавливает блокировку и возвращает управление только когда блокировка установлена.
     * В результате всегда isLocked()==true
     * @param category категория блокировки
     * @param name имя блокировки
     * @param owner владелец блокировки
     * @param autoUnlockTimeout Время жизни блокировки, по окончании которого блокировка снимается, даже если небыло вызван метод unlock
     * @return Экземпляр блокировки
     * @throws InterruptedException
     */
    ClusteredLock lock(String category, String name, String owner, Duration autoUnlockTimeout) throws InterruptedException;
    
    /**
     * Установка блокировки.
     * Если блокировка установлена то ждет waitingTime, если за это время блокировку получить не удалось возвращает isLocked()==false  
     * Если уже заблокирован, а owner текущий владелец (getOwner().equals(owner)), метод не ждёт таймаут, а сразу возвращает isLocked()==false
     * @param category категория блокировки
     * @param name имя блокировки
     * @param owner владелец блокировки
     * @param autoUnlockTimeout Время жизни блокировки, по окончании которого блокировка снимается, даже если небыло вызван метод unlock
     * @param waitingTime Время ожидания захвата блокировки, если захватить не получается за это время вернется экземпляр с isLocked()==false
     * @return Экземпляр блокировки
     * @throws InterruptedException
     */
    ClusteredLock tryLock(String category, String name, String owner, Duration autoUnlockTimeout, Duration waitingTime) throws InterruptedException;
    
    /**
     * Снятие блокировки. В случае если блокировка установлена происходит снятие блокировки. Если блокировки нет ничего не выполняется.
     * @param lock Экземпляр блокировки
     */
    void unlock(ClusteredLock lock);
    
    /**
     * Метод возвращает описание блокировки. В случае если блокировка установлена в результате isLocked()==true, иначе isLocked()==false
     * @param category категория блокировки
     * @param name имя блокировки
     * @return Экземпляр блокировки
     */
    ClusteredLock getLock(String category, String name);
}
