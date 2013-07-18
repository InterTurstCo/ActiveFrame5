package ru.intertrust.cm.core.dao.api;

import java.io.InputStream;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Предоставляет операции для сохранения/загрузки/удаления Вложений на файловой системе.
 * @author atsvetkov
 *
 */
public interface AttachmentContentDao {

    /**
     * Сохраняет Вложение в хранилище на файловой системе. Путь к хранилищу указывается в настройках разворачивания приложения.
     * @param inputStream поток с Вложением
     * @param attachmentDomainObject доменный объект Вложение
     * @return относительный путь к сохраненному Вложению
     */    
    String saveContent(InputStream  inputStream);

    /**
     * Загружает Вложение по относительному пути в хранилище.
     * @param domainObject ДО Вложение
     * @return поток с Вложением
     */    
    InputStream loadContent(DomainObject domainObject);

    /**
     * Удаление Вложения по относительному пути в хранилище.
     * @param domainObject ДО Вложение
     */
    void deleteContent(DomainObject domainObject);

}
