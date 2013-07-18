package ru.intertrust.cm.core.business.api;

import java.util.List;

import com.healthmarketscience.rmiio.RemoteInputStream;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Работа с доменными объектами Вложения. Вынесен в отдельный сервис, так как нужны функции работы с контентом Вложений.
 * @author atsvetkov
 *
 */
public interface AttachmentService {
   
    /**
     * Удаленный интерфейс для EJB
     * @author atsvetkov
     *
     */
    public interface Remote extends AttachmentService {
    }

    /**
     * Создает доменный объект (ДО) Вложение на основе его типа, не сохраняя его в СУБД.
     * @param domainObjectType тип ДО, для которого создается ДО Вложение.
     * @return пустой ДО Вложение
     */
    DomainObject createAttachmentDomainObjectFor(String domainObjectType);
    
    /**
     * Сохраняет доменный объект Вложение и его контент. Если id ДО Вложение пустое, то создает новый объект. Иначе,
     * замещает файл вложения для существующего ДО Вложение.
     * @param inputStream обертка для для java.io.InputStream, используется для перемещения файлов в потоке по сети
     * @param attachmentDomainObject доменный объект Вложение
     */
    
    void saveAttachment(RemoteInputStream  inputStream, DomainObject attachmentDomainObject);

    /**
     * Загружает контент для доменного объекта Вложение.
     * @param attachmentDomainObject ДО Вложение
     * @return обертка для для java.io.InputStream, используется для перемещения файлов в потоке по сети
     */
    
    RemoteInputStream loadAttachment(DomainObject attachmentDomainObject);

    /**
     * Удаление ДО Вложение и его контента.
     * @param path id в БД или абсолютный путь на жёстком диске
     */
    void deleteAttachment(DomainObject attachmentDomainObject);

    /**
     * Получает список ДО Вложений для переданного ДО.
     * @param domainObject ДО, для которого находятся вложения
     * @return список ДО Вложений
     */
    List<DomainObject> getAttachmentDomainObjectsFor(DomainObject domainObject);
    
}
