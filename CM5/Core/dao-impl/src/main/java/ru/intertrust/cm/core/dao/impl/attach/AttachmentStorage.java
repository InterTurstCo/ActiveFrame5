package ru.intertrust.cm.core.dao.impl.attach;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.dao.dto.AttachmentInfo;

public interface AttachmentStorage {

    public class Context {
        private String attachmentType;
        private DomainObject parentObject;
        private String fileName;
        private Calendar creationTime;

        public String getAttachmentType() {
            return attachmentType;
        }

        public DomainObject getParentObject() {
            return parentObject;
        }

        public String getFileName() {
            return fileName;
        }

        public Calendar getCreationTime() {
            return creationTime;
        }

        public Context attachmentType(String attachmentType) {
            this.attachmentType = attachmentType;
            return this;
        }

        public Context parentObject(DomainObject parentObject) {
            this.parentObject = parentObject;
            return this;
        }

        public Context fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Context creationTime(Calendar creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Context creationTime() {
            this.creationTime = Calendar.getInstance();
            return this;
        }
    }

    AttachmentInfo saveContent(InputStream inputStream, Context context);

    InputStream getContent(String localPath) throws FileNotFoundException;

    boolean deleteContent(String localPath);

    boolean hasContent(AttachmentInfo contentInfo);
}
