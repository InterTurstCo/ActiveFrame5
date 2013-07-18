package ru.intertrust.cm.core.business.impl;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import com.healthmarketscience.rmiio.RemoteInputStream;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

/**
 * Реализация сервиса работы с Вложениями.
 * @author atsvetkov
 *
 */
@Stateless
@Local(AttachmentService.class)
@Remote(AttachmentService.Remote.class)
public class AttachmentServiceImpl implements AttachmentService {

    private AttachmentContentDao attachmentContentDao;

    private DomainObjectDao domainObjectDao;

    @Override
    public DomainObject createAttachmentDomainObjectFor(String domainObjectType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveAttachment(RemoteInputStream inputStream, DomainObject attachmentDomainObject) {
        // TODO Auto-generated method stub

    }

    @Override
    public RemoteInputStream loadAttachment(DomainObject attachmentDomainObject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteAttachment(DomainObject attachmentDomainObject) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<DomainObject> getAttachmentDomainObjectsFor(DomainObject domainObject) {
        // TODO Auto-generated method stub
        return null;
    }

}
