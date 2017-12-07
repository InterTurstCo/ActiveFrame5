package ru.intertrust.cm.core.business.impl;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.ResourceService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.model.ReportServiceException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    protected AttachmentService attachmentService;

    @Override
    public String getString(String name) {
        String locale = profileService.getPersonLocale();
        String query = "select sr.string_value from string_resources sr";
        query += "inner join resources r on r.id = sr.id ";
        query += "inner join locale l on l.id = r.locale ";
        query += "where lower(l.name) = {0} and lower(r.name) = {1}";

        List<Value> params = new ArrayList<Value>();
        params.add(new StringValue(Case.toLower(locale)));
        params.add(new StringValue(Case.toLower(name)));

        String result = null;
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(query, params);
        if (collection.size() > 0) {
            result = collection.get(0).getString("string_value");
        }
        return result;
    }

    @Override
    public Long getNumber(String name) {
        String locale = profileService.getPersonLocale();
        String query = "select nr.number_value from number_resources nr";
        query += "inner join resources r on r.id = nr.id ";
        query += "inner join locale l on l.id = r.locale ";
        query += "where lower(l.name) = {0} and lower(r.name) = {1}";

        List<Value> params = new ArrayList<Value>();
        params.add(new StringValue(Case.toLower(locale)));
        params.add(new StringValue(Case.toLower(name)));

        Long result = null;
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(query, params);
        if (collection.size() > 0) {
            result = collection.get(0).getLong("number_value");
        }
        return result;
    }

    @Override
    public byte[] getBlob(String name) {
        String locale = profileService.getPersonLocale();
        String query = "select r.id from blob_resources br ";
        query += "inner join resources r on r.id = br.id ";
        query += "inner join locale l on l.id = r.locale ";
        query += "where lower(l.name) = {0} and lower(r.name) = {1}";

        List<Value> params = new ArrayList<Value>();
        params.add(new StringValue(Case.toLower(locale)));
        params.add(new StringValue(Case.toLower(name)));

        byte[] result = null;
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(query, params);
        if (collection.size() > 0) {
            Id blobResId = collection.get(0).getId();
            List<DomainObject> attachments = attachmentService.findAttachmentDomainObjectsFor(blobResId, "blob_resources_attach");
            if (attachments.size() > 0) {
                result = getAttachmentContent(attachments.get(0));
            }
        }
        return result;
    }

    @Override
    public String getResourcePath(String name) {
        throw new UnsupportedOperationException();
    }

    private byte[] getAttachmentContent(DomainObject attachment) {
        InputStream contentStream = null;
        RemoteInputStream inputStream = null;
        try {
            inputStream = attachmentService.loadAttachment(attachment.getId());
            contentStream = RemoteInputStreamClient.wrap(inputStream);
            ByteArrayOutputStream attachmentBytes = new ByteArrayOutputStream();

            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = contentStream.read(buffer)) > 0) {
                attachmentBytes.write(buffer, 0, read);
            }
            return attachmentBytes.toByteArray();
        } catch (Exception ex) {
            throw new ReportServiceException("Error on get attachment body", ex);
        } finally {
            try {
                contentStream.close();
                inputStream.close(true);
            } catch (IOException ignoreEx) {
            }
        }
    }
}
