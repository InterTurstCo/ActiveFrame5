package ru.intertrust.cm.core.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import ru.intertrust.cm.core.business.api.FileSystemAttachmentCleaner;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.AttachmentStorageConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FolderStorageConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.model.FatalException;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import javax.transaction.Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class FileSystemAttachmentCleanerImpl implements FileSystemAttachmentCleaner {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemAttachmentCleanerImpl.class);

    @org.springframework.beans.factory.annotation.Value("${attachment.delete.batch.size:100}")
    private int fileDeleteBatchSize;

    @org.springframework.beans.factory.annotation.Value("${attachment.delete.time.gap:3600}")
    private int fileDeleteTimeGap;  // in seconds

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private AttachmentContentDao attachmentContentDao;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private Environment env;

    @Override
    public String clean(EJBContext ejbContext){
        return clean(ejbContext, null, null);
    }

    @Override
    public String clean(EJBContext ejbContext, Date from, Date to) {
        SessionContext sessionContext = (SessionContext)ejbContext;

        Worker worker = new Worker(sessionContext, from, to);
        try {
            if (Status.STATUS_ACTIVE != ejbContext.getUserTransaction().getStatus()) {
                ejbContext.getUserTransaction().begin();
            }

            worker.attachmentTypes = configurationExplorer.getAllAttachmentTypes();
            if (worker.attachmentTypes == null || worker.attachmentTypes.length == 0) {
                return "No attachment types";
            }
            worker.accessToken = accessControlService.createSystemAccessToken(getClass().getName());

            for (String root : getAllStorageRoots()) {
                worker.root = Paths.get(root);
                Files.walkFileTree(worker.root, worker);
                if (sessionContext.wasCancelCalled()) {
                    logger.info("Work is canceled");
                    break;
                }
            }

            if (Status.STATUS_ACTIVE == ejbContext.getUserTransaction().getStatus()) {
                ejbContext.getUserTransaction().commit();
            }
        } catch (Exception ex) {
            throw new FatalException(ex);
        }

        String result = sessionContext.wasCancelCalled() ? "CancelCalled" : "Complete";
        result += "; " + worker.deleteCount + " file(s) deleted [processed "
                + worker.fileCount + " files in " + worker.dirCount + " directories]";
        if (worker.errorCount > 0) {
            result += "; " + worker.errorCount + " error(s) encountered!";
        }
        logger.info(result);
        return result;
    }

    private String[] getAllStorageRoots() {
        Set<String> roots = new HashSet<>();
        for (AttachmentStorageConfig config : configurationExplorer.getConfigs(AttachmentStorageConfig.class)) {
            if (config.getStorageConfig() instanceof FolderStorageConfig) {
                String path = env.getProperty("attachment.storage." + config.getName() + "dir");
                if (path != null) {
                    roots.add(path);
                }
            }
        }
        String path = env.getProperty("attachment.storage.dir");
        if (path != null) {
            roots.add(path);
        }
        path = env.getProperty("attachment.storage");
        if (path != null) {
            roots.add(path);
        }
        return roots.toArray(new String[roots.size()]);
    }

    private class Worker implements FileVisitor<Path> {

        private Path root;
        private String[] attachmentTypes;
        private AccessToken accessToken;
        private SessionContext sessionContext;
        private Date from;
        private Date to;

        private int fileCount = 0;
        private int dirCount = 0;
        private int deleteCount = 0;
        private int errorCount = 0;

        public Worker(SessionContext sessionContext, Date from, Date to){
            this.sessionContext = sessionContext;
            this.from = from;
            this.to = to;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) {
            logger.trace("Processing directory " + dir);
            return sessionContext.wasCancelCalled() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException ex) {
            // TODO directory can be removed if it became empty
            ++dirCount;
            return sessionContext.wasCancelCalled() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            String path = root.relativize(file).toString();
            if ((attr.creationTime().toMillis() - fileDeleteTimeGap * 1000 < System.currentTimeMillis()) // skip new file
                    && checkPeriod(attr.creationTime())) {
                if (!isLinkedInDo(path)) {
                    try {
                        Files.delete(file);
                        ++deleteCount;
                        logger.info("File " + path + " has no links form domain objects and was deleted");
                    } catch (IOException e) {
                        ++errorCount;
                        logger.warn("Error deleting not linked file " + file, e);
                    }
                }
            }else{
                logger.debug("Ignore file " + path);
            }
            ++fileCount;
            if (fileCount % fileDeleteBatchSize == 0) {
                try {
                    sessionContext.getUserTransaction().commit();
                    sessionContext.getUserTransaction().begin();
                } catch (Exception e) {
                    throw new FatalException("Transaction switching problem", e);
                }
            }
            return sessionContext.wasCancelCalled() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
        }

        /**
         * Проверка входит ли дата создания файла в проверяемый период
         * @param creationTime
         * @return
         */
        private boolean checkPeriod(FileTime creationTime) {
            return (from == null && to == null) ||
                    (from == null && to != null && creationTime.toMillis() < to.getTime()) ||
                    (from != null && creationTime.toMillis() > from.getTime() && to == null) ||
                    (from != null && creationTime.toMillis() > from.getTime() && to != null && creationTime.toMillis() < to.getTime());
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException ex) {
            ++errorCount;
            logger.warn("Error access file " + file, ex);
            return sessionContext.wasCancelCalled() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
        }

        private boolean isLinkedInDo(String relativePath) {

            String alternatePath = relativePath.replaceAll(Pattern.quote(File.separator), "/");
            @SuppressWarnings("rawtypes")
            List<Value> params = Arrays.<Value>asList(new StringValue(relativePath),
                    new StringValue(alternatePath), new StringValue("/" + alternatePath));

            for (String attachmentType : attachmentTypes) {
                IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(
                        "select t.id from " + attachmentType + " t where t.path = {0} or t.path = {1} or t.path = {2}",
                        params, 0, 2, accessToken);
                if (collection != null && collection.size() > 0) {
                    return true;
                }
            }

            return false;
        }
    }

}
