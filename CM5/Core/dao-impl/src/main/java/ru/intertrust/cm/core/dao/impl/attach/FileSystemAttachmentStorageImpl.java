package ru.intertrust.cm.core.dao.impl.attach;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.DeleteFileConfig;
import ru.intertrust.cm.core.config.FolderStorageConfig;
import ru.intertrust.cm.core.dao.api.BaseActionListener;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.UserTransactionService;
import ru.intertrust.cm.core.dao.dto.AttachmentInfo;
import ru.intertrust.cm.core.dao.exception.DaoException;

public class FileSystemAttachmentStorageImpl implements AttachmentStorage {

    public enum Variable {
        YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, DOCTYPE, CREATOR, EXT
    }

    public static final String PROP_LOCATION = "dir";
    public static final String PROP_PATHMASK = "folders";
    public static final String PROP_LEGACY = "attachment.storage";

    public static final String DEFAULT_PATHMASK = "{year}/{month}/{day}";

    private static final Logger logger = LoggerFactory.getLogger(FileSystemAttachmentStorageImpl.class);

    private final String name;
    private final FolderStorageConfig storageConfig;

    private String rootFolder;
    private String pathMask;
    private FileDeleteStrategy deleteStrategy;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;
    @Autowired
    private UserTransactionService txService;
    @Autowired
    private FileTypeDetector contentDetector;
    @Autowired
    private FileSystemAttachmentStorageHelper helper;
    @Autowired
    private DeleteAttachmentStrategyFactory deleteStrategyFactory;

    @Value("${attachments.path.unixstyle:true}")
    private boolean pathUnixStyle;

    public FileSystemAttachmentStorageImpl(String name, FolderStorageConfig storageConfig) {
        this.name = name;
        this.storageConfig = storageConfig;
    }

    @PostConstruct
    public void initialize() {
        logger.info("Attachment storage {} initialization", name);
        rootFolder = getProperty(PROP_LOCATION);
        if (rootFolder == null) {
            rootFolder = helper.getPureProperty(PROP_LEGACY);
            if (rootFolder == null) {
                throw new ConfigurationException("Directory for storage " + name + " is not defined!");
            }
            if (!new File(rootFolder).isDirectory()) {
                throw new ConfigurationException("Directory " + rootFolder + " not exists or is inaccessible");
            }
        }
        pathMask = storageConfig.getSubfolderMask();
        if (Boolean.TRUE.equals(storageConfig.getConfigurable())) {
            String pathMask = getProperty(PROP_PATHMASK);
            if (pathMask != null) {
                this.pathMask = validatePathMask(pathMask);
            }
        }
        if (pathMask == null || pathMask.isEmpty()) {
            logger.info("Folders mask for storage {} is not configured; use default", name);
            pathMask = DEFAULT_PATHMASK;
        }
        this.deleteStrategy = deleteStrategyFactory.createDeleteStrategy(name, storageConfig.getDeleteFileConfig());
    }

    @Override
    public AttachmentInfo saveContent(InputStream inputStream, Context context) {
        String localPath = generateLocalPath(context);
        Path folder = Paths.get(rootFolder, localPath);
        final Path filePath = folder.resolve(generateFileName(context));
        txService.addListener(new BaseActionListener() {
            @Override
            public void onRollback() {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    logger.error("Failed to delete uncommitted content on transaction rollback", e);
                }
            }
        });

        try {
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }
            Files.copy(inputStream, filePath);
        } catch (IOException e) {
            throw new DaoException(e);
        }

        AttachmentInfo info = new AttachmentInfo();
        info.setRelativePath(relativizePath(filePath));
        info.setContentLength(filePath.toFile().length());
        info.setMimeType(contentDetector.detectMimeType(filePath.toString()));
        return info;
    }

    @Override
    public InputStream getContent(String localPath) throws FileNotFoundException {
        Path filePath = Paths.get(rootFolder, localPath);
        return new FileInputStream(filePath.toFile());
    }

    @Override
    public boolean deleteContent(String localPath) {
        Path filePath = Paths.get(rootFolder, localPath);
        if (!Files.exists(filePath)) {
            logger.trace("File '{}' doesn't exist...", filePath);
            return false;
        }
        logger.trace("File '{}' will be deleted by strategy {}", filePath, deleteStrategy);
        deleteStrategy.deleteFile(filePath.toString());
        return true;
    }

    @Override
    public boolean hasContent(AttachmentInfo contentInfo) {
        Path filePath = Paths.get(rootFolder, contentInfo.getRelativePath());
        return Files.isRegularFile(filePath) && filePath.toFile().length() == contentInfo.getContentLength();
    }

    private String validatePathMask(String pathMask) {
        int pos = 0;
        //TODO check for absence of .. (upper dir) links in the mask
        while (true) {
            pos = pathMask.indexOf('{', pos);
            if (pos == -1) {
                break;
            }
            int end = pathMask.indexOf('}', pos + 1);
            if (end == -1) {
                throw new ConfigurationException("Subfolder mask syntax error: no closing bracket [" + pathMask + "]");
            }
            String varName = pathMask.substring(pos + 1, end);
            try {
                /*Variable var =*/ Variable.valueOf(varName.toUpperCase());
            } catch (Exception e) {
                throw new ConfigurationException("Subfolder mask syntax error: unknown variable " + varName, e);
            }
            pos = end + 1;
        }
        return pathMask;
    }

    private String generateLocalPath(Context context) {
        StringBuilder path = new StringBuilder();
        int start = 0;
        while (true) {
            int end = pathMask.indexOf('{', start);
            if (end == -1) {
                break;
            }
            path.append(pathMask.substring(start, end));
            start = end + 1;
            end = pathMask.indexOf('}', start);
            /*if (end == -1) {
                throw new ConfigurationException("Subfolder mask syntax error: no closing bracket [" + pathMask + "]");
            }*/
            String varName = pathMask.substring(start, end);
            Variable var = Variable.valueOf(varName.toUpperCase());
            path.append(getVarValue(var, context));
            /*for (Variable var : Variable.values()) {
                if (var.name().equalsIgnoreCase(varName)) {
                    path.append(getVarValue(var, context));
                    varName = null;
                    break;
                }
            }
            if (varName != null) {
                throw new ConfigurationException("Subfolder mask syntax error: unknown variable " + varName);
            }*/
            start = end + 1;
        }
        if (start < pathMask.length()) {
            path.append(pathMask.substring(start));
        }
        return path.toString();
    }

    private String getVarValue(Variable var, Context context) {
        switch (var) {
        case YEAR:
            return String.format("%4d", context.getCreationTime().get(Calendar.YEAR));
        case MONTH:
            return String.format("%02d", context.getCreationTime().get(Calendar.MONTH) + 1);
        case DAY:
            return String.format("%02d", context.getCreationTime().get(Calendar.DATE));
        case HOUR:
            return String.format("%02d", context.getCreationTime().get(Calendar.HOUR_OF_DAY));
        case MINUTE:
            return String.format("%02d", context.getCreationTime().get(Calendar.MINUTE));
        case SECOND:
            return String.format("%02d", context.getCreationTime().get(Calendar.SECOND));
        case DOCTYPE:
            return context.getParentObject().getTypeName();
        case CREATOR:
            Id id = currentUserAccessor.getCurrentUserId();
            if (id == null) {
                return "0";
            } else if (id instanceof RdbmsId) {
                return String.format("%d", ((RdbmsId) id).getId());
            } else {
                return id.toStringRepresentation();
            }
        case EXT:
            String ext = findExtension(context.getFileName());
            return ext.length() > 1 ? ext.substring(1) : "_";
        }
        // Check in init() must prevent running here
        throw new ConfigurationException("Subfolder mask syntax error: unknown variable " + var.name());
    }

    private String getProperty(String propName) {
        return helper.getProperty(propName, name);
    }

    private String generateFileName(Context context) {
        return UUID.randomUUID() + findExtension(context.getFileName());
    }

    private String findExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) {
            return "";
        }
        String ext = fileName.substring(lastDot);
        for (int i = 1; i < ext.length(); ++i) {
            char ch = ext.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != '_') {
                return "";
            }
        }
        return ext;
    }

    private String relativizePath(Path fullPath) {
        String relativePath = Paths.get(rootFolder).relativize(fullPath).toString();
        if (pathUnixStyle && !"/".equals(File.separator)) {
            relativePath = relativePath.replaceAll(Pattern.quote(File.separator), "/");
        }
        return relativePath;
    }
}
