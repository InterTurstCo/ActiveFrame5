package ru.intertrust.cm.core.dao.impl.attach;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.dao.dto.AttachmentInfo;

public interface AttachmentStorage {

    public interface Context {
        String getAttachmentType();
        DomainObject getParentObject();
        String getFileName();
        Calendar getCreationTime();
    }

    public static class StaticContext implements Context {
        private String attachmentType;
        private DomainObject parentObject;
        private String fileName;
        private Calendar creationTime;

        @Override
        public String getAttachmentType() {
            return attachmentType;
        }

        @Override
        public DomainObject getParentObject() {
            return parentObject;
        }

        @Override
        public String getFileName() {
            return fileName;
        }

        @Override
        public Calendar getCreationTime() {
            return creationTime;
        }

        public StaticContext attachmentType(String attachmentType) {
            this.attachmentType = attachmentType;
            return this;
        }

        public StaticContext parentObject(DomainObject parentObject) {
            this.parentObject = parentObject;
            return this;
        }

        public StaticContext fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public StaticContext creationTime(Calendar creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public StaticContext creationTime(Date creationTime) {
            this.creationTime = Calendar.getInstance();
            this.creationTime.setTime(creationTime);
            return this;
        }

        public StaticContext creationTime() {
            this.creationTime = Calendar.getInstance();
            return this;
        }
    }

    public static class CachedContext implements Context {
        private Context baseContext;
        private String attachmentType;
        private DomainObject parentObject;
        private String fileName;
        private Calendar creationTime;

        public CachedContext(Context baseContext) {
            this.baseContext = baseContext;
        }

        @Override
        public String getAttachmentType() {
            if (attachmentType == null) {
                attachmentType = baseContext.getAttachmentType();
            }
            return attachmentType;
        }

        @Override
        public DomainObject getParentObject() {
            if (parentObject == null) {
                parentObject = baseContext.getParentObject();
            }
            return parentObject;
        }

        @Override
        public String getFileName() {
            if (fileName == null) {
                fileName = baseContext.getFileName();
            }
            return fileName;
        }

        @Override
        public Calendar getCreationTime() {
            if (creationTime == null) {
                creationTime = baseContext.getCreationTime();
            }
            return creationTime;
        }
    }

    AttachmentInfo saveContent(InputStream inputStream, Context context);

    InputStream getContent(String localPath) throws FileNotFoundException;

    boolean deleteContent(String localPath);

    boolean hasContent(AttachmentInfo contentInfo);
}
