package ru.intertrust.cm.core.business.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import javax.transaction.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.config.AttachmentStorageConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FolderStorageConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.model.ScheduleException;
/**
 * Периодическое задание удаляющее все файлы вложений в хранилище,
 * не имеющие ссылающихся на них доменных объектов.
 */
@ScheduleTask(name = "FileSystemAttachmentCleanerScheduleTask", hour = "2", minute = "15", active = true,
              taskTransactionalManagement = true)
public class FileSystemAttachmentCleanerScheduleTask implements ScheduleTaskHandle {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemAttachmentCleanerScheduleTask.class);

    @org.springframework.beans.factory.annotation.Value("${attachment.storage}")
    private String attachmentSaveLocation;

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
    public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) {
        logger.info("FileSystemAttachmentCleanerScheduleTask started");

        Worker worker = new Worker();
        try {
            if (Status.STATUS_ACTIVE != ejbContext.getUserTransaction().getStatus()) {
                ejbContext.getUserTransaction().begin();
            }

            worker.attachmentTypes = configurationExplorer.getAllAttachmentTypes();
            if (worker.attachmentTypes == null || worker.attachmentTypes.length == 0) {
                return "No attachment types";
            }
            worker.accessToken = accessControlService.createSystemAccessToken(getClass().getName());
            worker.ejbContext = ejbContext;

            for (String root : getAllStorageRoots()) {
                worker.root = Paths.get(root);
                Files.walkFileTree(worker.root, worker);
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
            }

            if (Status.STATUS_ACTIVE == ejbContext.getUserTransaction().getStatus()) {
                ejbContext.getUserTransaction().commit();
            }
        } catch (ScheduleException e) {
            throw e;
        } catch (Exception e) {
            throw new ScheduleException(e);
        }

        String result = Thread.currentThread().isInterrupted() ? "Interrupted" : "Complete";
        result += "; " + worker.deleteCount + " file(s) deleted [processed "
                + worker.fileCount + " files in " + worker.dirCount + " directories]";
        if (worker.errorCount > 0) {
            result += "; " + worker.errorCount + " error(s) encountered!";
        }
        logger.info(result);
        return result;
/*
        File storageDir = new File(attachmentSaveLocation);
        String storageDirAbsolutePath = storageDir.getAbsolutePath();
        List<File> allFiles = new ArrayList<>();
        readFiles(allFiles, storageDir, true);
        
        logger.info("Found {} files", allFiles.size());

        long allAttachments = allFiles.size();
        long processed = 0;
        long linked = 0;
        long deleted = 0;
        long error = 0;
        
        int counter = 0;

        try {

            for (File file : allFiles) {
                counter++;
                String absolutePath = file.getAbsolutePath();
                String relativePath = absolutePath.substring(storageDirAbsolutePath.length());
                String unixRelativePath = attachmentContentDao.toRelativeFromAbsPathFile(absolutePath);

                //TODO: Remove Windoes-related code when all clients move to unix-style paths of attachments
                boolean isLinked = isLinkedInDo(unixRelativePath) || (relativePath.startsWith("\\") && isLinkedInDo(relativePath));

                if (!isLinked) {
                    if (file.delete()) {
                        logger.info("File " + relativePath + " has not linked from Domain Objects and was deleted");
                    } else {
                        logger.error("File " + relativePath + " can not be deleted");
                        error++;
                    }
                }else{
                    linked++;
                }

                if (counter % fileDeleteBatchSize == 0) {
                    ejbContext.getUserTransaction().commit();
                    ejbContext.getUserTransaction().begin();
                }
            }
        } catch (NotSupportedException | SystemException | HeuristicRollbackException | HeuristicMixedException | RollbackException e) {
            try {
                ejbContext.getUserTransaction().rollback();
            } finally {
                throw new ScheduleException(e);
            }
        } finally {
            try {
                if (Status.STATUS_ACTIVE ==  ejbContext.getUserTransaction().getStatus()) {
                    ejbContext.getUserTransaction().commit();
                }
            } catch (SystemException | HeuristicRollbackException | HeuristicMixedException | RollbackException e) {
                throw new ScheduleException(e);
            }
        }

        logger.info("FileSystemAttachmentCleanerScheduleTask complete: " + counter + " file(s) deleted");
        result += "Processed: " + processed + "; ";
        result += "Linked: " + linked + "; ";
        result += "Deleted: " + deleted + "; ";
        result += "Errors: " + error + "; ";
        
        return "" + counter + " file(s) deleted";
*/
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

        Path root;
        String[] attachmentTypes;
        AccessToken accessToken;
        EJBContext ejbContext;

        int fileCount = 0;
        int dirCount = 0;
        int deleteCount = 0;
        int errorCount = 0;

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) {
            logger.trace("Processing directory " + dir);
            return Thread.currentThread().isInterrupted() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException ex) {
            // TODO directory can be removed if it became empty
            ++dirCount;
            return Thread.currentThread().isInterrupted() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            if (attr.creationTime().toMillis() - fileDeleteTimeGap * 1000 < System.currentTimeMillis()) { // skip new file
                String path = root.relativize(file).toString();
                if (!isLinkedInDo(path)) {
                    try {
                        Files.delete(file);
                        logger.info("File " + path + " has no links form domain objects and was deleted");
                    } catch (IOException e) {
                        logger.warn("Error deleting not linked file " + file, e);
                        ++errorCount;
                    }
                }
            }
            ++fileCount;
            if (fileCount % fileDeleteBatchSize == 0) {
                try {
                    ejbContext.getUserTransaction().commit();
                    ejbContext.getUserTransaction().begin();
                } catch (Exception e) {
                    throw new ScheduleException("Transaction switching problem", e);
                }
            }
            return Thread.currentThread().isInterrupted() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException ex) {
            ++errorCount;
            logger.warn("Error access file " + file, ex);
            return Thread.currentThread().isInterrupted() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
        }

        private boolean isLinkedInDo(String relativePath) {

            String alternatePath = relativePath.replaceAll(Pattern.quote(File.separator), "/");
            @SuppressWarnings("rawtypes")
            List<Value> params = Arrays.<Value>asList(new StringValue(relativePath), new StringValue(alternatePath));

            for (String attachmentType : attachmentTypes) {
                IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(
                        "select t.id from " + attachmentType + " t where t.path = {0} or t.path = {1}", params, 0, 2, accessToken);
                if (collection != null && collection.size() > 0) {
                    return true;
                }
            }

            return false;
        }
    }
}
