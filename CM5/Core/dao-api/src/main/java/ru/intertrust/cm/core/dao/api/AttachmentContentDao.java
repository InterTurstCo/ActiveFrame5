package ru.intertrust.cm.core.dao.api;

import java.io.InputStream;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.dao.dto.AttachmentInfo;

/**
 * Предоставляет операции для сохранения/загрузки/удаления Вложений на файловой системе.
 * @author atsvetkov
 *
 */
public interface AttachmentContentDao {

    /**
     * Сохраняет Вложение в хранилище на файловой системе. Путь к хранилищу указывается в настройках разворачивания приложения. 
     * Для правильного определения mimiType нужно передать имя файла вложения в параметре fileName.
     * @param inputStream поток с вложением
     * @param fileName имя файла вложения.
     * @return информация о вложении
     */
    AttachmentInfo saveContent(InputStream inputStream, DomainObject parentObject, String attachmentType, String fileName);

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

    @Deprecated
    String toRelativeFromAbsPathFile(String absFilePath);
}
