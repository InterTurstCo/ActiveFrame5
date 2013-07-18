package ru.intertrust.cm.core.dao.impl;

import java.io.InputStream;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;

/**
 * Реализация {@see AttachmentContentDao} для работы с файлами Вложений на файловой системе.
 * @author atsvetkov
 */
public class FileSystemAttachmentContentDaoImpl implements AttachmentContentDao {

    @Override
    public String saveContent(InputStream inputStream) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream loadContent(DomainObject domainObject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteContent(DomainObject domainObject) {
        // TODO Auto-generated method stub
        
    }

}
