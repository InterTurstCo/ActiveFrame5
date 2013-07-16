package ru.intertrust.cm.core.dao.impl;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import com.healthmarketscience.rmiio.RemoteInputStream;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.dao.api.AttachmentContentService;

/**
 * 
 * @author atsvetkov
 *
 */
@Stateless
@Local(AttachmentContentService.class)
@Remote(AttachmentContentService.Remote.class)
public class FileSystemAttachmentContentServiceImpl implements AttachmentContentService,
        AttachmentContentService.Remote {

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
