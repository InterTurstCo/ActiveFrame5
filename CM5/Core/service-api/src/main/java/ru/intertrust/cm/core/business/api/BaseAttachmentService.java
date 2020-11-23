package ru.intertrust.cm.core.business.api;

import com.healthmarketscience.rmiio.RemoteInputStream;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.List;
import java.util.Map;

/**
 * Created by andrey on 25.04.14.
 */
public interface BaseAttachmentService {
    /*
         * константы для зарезервированных названий полей доменного объекта вложения
         */
    String NAME =  "Name";
    String PATH = "Path";
    String MIME_TYPE = "MimeType";
    String DESCRIPTION = "Description";
    String CONTENT_LENGTH = "ContentLength";
    String FIELD_NAME = "FieldName";

    /**
     * Создает доменный объект (ДО) Вложение на основе его типа, не сохраняя его в СУБД.
     * @param objectId доменный объект, для которого создается ДО Вложение
     * @param attachmentType тип Вложения
     * @return пустой ДО Вложение
     */
    DomainObject createAttachmentDomainObjectFor(Id objectId, String attachmentType);

    /**
     * Сохраняет доменный объект Вложение и его контент. Если id ДО Вложение пустое, то создает новый объект. Иначе,
     * замещает файл вложения для существующего ДО Вложение.
     * @param inputStream обертка для для java.io.InputStream, используется для перемещения файлов в потоке по сети
     * @param attachmentDomainObject доменный объект Вложение
     * @return сохраненный доменный объект Вложение
     */

    DomainObject saveAttachment(RemoteInputStream inputStream, DomainObject attachmentDomainObject);

    /**
     * Загружает контент для доменного объекта Вложение.
     * @param attachmentDomainObjectId ID ДО Вложение
     * @return обертка для для java.io.InputStream, используется для перемещения файлов в потоке по сети
     */

    RemoteInputStream loadAttachment(Id attachmentDomainObjectId);

    /**
     * Удаление ДО Вложение и его контента.
     * @param attachmentDomainObjectId ID ДО Вложение
     */
    void deleteAttachment(Id attachmentDomainObjectId);

    /**
     * Получает список ДО Вложений для переданного ID ДО.
     * @param domainObjectId ID ДО, для которого находятся вложения
     * @return список ДО Вложений
     */
    List<DomainObject> findAttachmentDomainObjectsFor(Id domainObjectId);

    /**
     * Получает все доменные объекты вложений определённого типа
     * @param domainObjectId ID ДО, для которого находятся вложения
     * @param attachmentType типа вложения
     * @return список ДО Вложений
     */
    List<DomainObject> findAttachmentDomainObjectsFor(Id domainObjectId, String attachmentType);

    /**
     * Копирует вложение по идентификатору
     * @param attachmentDomainObjectId идентификатор копируемого вложения
     * @param destinationDomainObjectId идентификатор ДО, в который копируется вложение
     * @param destinationAttachmentType тип вложения в ДО, в который копируется вложение
     * @return ДО копии вложения
     */
    DomainObject copyAttachment(Id attachmentDomainObjectId, Id destinationDomainObjectId, String destinationAttachmentType);

    /**
     * Копирует вложения по идентификатору
     * @param attachmentDomainObjectIds идентификаторы копируемых вложений
     * @param destinationDomainObjectId идентификатор ДО, в который копируется вложение
     * @param destinationAttachmentType тип вложения в ДО, в который копируется вложение
     * @return список ДО копий вложений
     */
    List<DomainObject> copyAttachments(List<Id> attachmentDomainObjectIds, Id destinationDomainObjectId,
                                       String destinationAttachmentType);

    /**
     * Копирует вложения определенного типа из ДО в ДО
     * @param sourceDomainObjectId идентификатор ДО, из которого копируются вложения
     * @param sourceAttachmentType тип копируемых вложений
     * @param destinationDomainObjectId идентификатор ДО, в который копируются вложения
     * @param destinationAttachmentType тип вложения в ДО, в который копируются вложения
     * @return список ДО копий вложений
     */
    List<DomainObject> copyAttachmentsFrom(Id sourceDomainObjectId, String sourceAttachmentType,
                                           Id destinationDomainObjectId, String destinationAttachmentType);

    /**
     * Копирует все вложения из ДО в ДО
     * @param sourceDomainObjectId идентификатор ДО, из которого копируются вложения
     * @param destinationDomainObjectId идентификатор ДО, в который копируются вложения
     * @param destinationAttachmentType тип вложения в ДО, в который копируются вложения
     * @return список ДО копий вложений
     */
    List<DomainObject> copyAllAttachmentsFrom(Id sourceDomainObjectId, Id destinationDomainObjectId,
                                              String destinationAttachmentType);

    /**
     * Копирует все вложения из ДО в ДО
     * @param sourceDomainObjectId идентификатор ДО, из которого копируются вложения
     * @param destinationDomainObjectId идентификатор ДО, в который копируются вложения
     * @param attachmentTypeMap {@link Map<String, String>} соответситвия типов вложений в исходном ДО и
     *                                                     ДО, в который копируются вложения
     * @return список ДО копий вложений
     */
    List<DomainObject> copyAllAttachmentsFrom(Id sourceDomainObjectId, Id destinationDomainObjectId,
                                              Map<String, String> attachmentTypeMap);

    /**
     * Проверяет указанный тип доступа к вложению для текущего пользователя
     *
     * @param attachId вложение
     * @param permission тип доступа
     * @return признак того, что у пользователя есть доступ этого типа
     */
    boolean checkAccess(Id attachId, DomainObjectPermission.Permission permission);

    /**
     * Проверяет указанный тип доступа к вложению
     *
     * @param attachId вложение
     * @param userId пользователь
     * @param permission тип доступа
     * @return признак того, что у пользователя есть доступ этого типа
     */
    boolean checkAccess(Id attachId, Id userId, DomainObjectPermission.Permission permission);
}
