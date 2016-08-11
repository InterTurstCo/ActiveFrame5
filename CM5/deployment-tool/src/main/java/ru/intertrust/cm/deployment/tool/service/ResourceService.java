package ru.intertrust.cm.deployment.tool.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.intertrust.cm.deployment.tool.property.AttachmentType;
import ru.intertrust.cm.deployment.tool.property.UpgradeProperties;
import ru.intertrust.cm.deployment.tool.xml.StandaloneXml;
import ru.intertrust.cm.deployment.tool.xml.SystemProperty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import static java.io.File.separator;
import static org.springframework.util.StringUtils.isEmpty;
import static ru.intertrust.cm.deployment.tool.config.AppConstants.INIT_DATA_PROPERTY_NAME;
import static ru.intertrust.cm.deployment.tool.property.AttachmentType.*;
import static ru.intertrust.cm.deployment.tool.util.ResourceUtil.getServerPropValue;

/**
 * Created by Alexander Bogatyrenko on 28.07.16.
 * <p>
 * This class represents...
 */
@Service
public class ResourceService {

    private static Logger logger = LoggerFactory.getLogger(ResourceService.class);

    @Autowired
    private UpgradeProperties props;

    private EnumMap<AttachmentType, String> serverProps;

    private Path initData;

    public EnumMap<AttachmentType, String> parseServerProperties() {
        if (CollectionUtils.isEmpty(serverProps)) {
            String serverPropertiesPath = props.getServerProperties();

            serverProps = new EnumMap<>(AttachmentType.class);
            LineIterator it;
            try {
                it = FileUtils.lineIterator(new File(serverPropertiesPath), "UTF-8");
                try {
                    while (it.hasNext()) {
                        String line = it.nextLine();
                        if (!isEmpty(line)) {
                            if (line.contains(STORAGE.getTypeValue())) {
                                serverProps.put(STORAGE, getServerPropValue(line));
                            } else if (line.contains(TEMP_STORAGE.getTypeValue())) {
                                serverProps.put(TEMP_STORAGE, getServerPropValue(line));
                            } else if (line.contains(PUBLICATION_STORAGE.getTypeValue())) {
                                serverProps.put(PUBLICATION_STORAGE, getServerPropValue(line));
                            }
                        }
                    }
                } finally {
                    LineIterator.closeQuietly(it);
                }

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return serverProps;
    }

    public boolean createBackupFolder(String earVersion) {
        File file = new File(props.getBackupFolder() + separator + earVersion);
        if (!file.exists()) {
            try {
                boolean created = file.mkdir();
                logger.info("Backup folder with name {} {} has been created", earVersion, created ? "" : "not"); // :)
                return created;
            } catch (SecurityException e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        } else {
            logger.info("Backup folder with name {} already exists", earVersion);
        }
        return false;
    }

    public boolean copy(Path source, Path target, String fileName, boolean force) {
        source = Paths.get(source.toAbsolutePath().toString(), fileName + ".ear");
        Path targetFilePath = Paths.get(target.toAbsolutePath().toString(), fileName + ".ear");
        try {
            if (Files.exists(targetFilePath)) {
                if (force) {
                    Files.delete(targetFilePath);
                } else {
                    logger.info("File {} already exists", fileName);
                    return true;
                }
            }
            logger.info("Start copy {} file", fileName);
            Files.copy(source, targetFilePath);
            logger.info("File {} has been copied", fileName);
            return true;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean copy(Path source, Path target, String fileName) {
        return copy(source, target, fileName, true);
    }

    public void cleanServer() {
        logger.info("Start clean deployment directory");
        File folder = new File(props.getJbossHome() + separator + "standalone" + separator + "deployments");
        File[] files = folder.listFiles();

        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (!isEmpty(fileName) && fileName.contains(".ear")) {
                        boolean delete = file.delete();
                        if (delete) {
                            logger.info("Ear {} has been deleted from server deployment directory", file);
                        }
                    }
                }
            }
        }
    }

    public String getLastSuccessful(String ear) {
        List<String> earSequence = props.getEarSequence();
        int idx = earSequence.indexOf(ear);
        if (idx != -1) {
            ListIterator<String> iterator = earSequence.listIterator(idx);
            if (iterator.hasPrevious()) {
                return iterator.previous();
            } else {
                String current = props.getEarCurrent();
                logger.error("Previous version of ear - {} not found, use ear.current version - {}", ear, current);
                return current;
            }
        }
        return null;
    }

    public Path parseStandalone() {
        if (initData == null) {
            Serializer serializer = new Persister();
            Path standalonePath = Paths.get(props.getJbossHome(), "standalone", "configuration", "standalone.xml");
            File standalone = standalonePath.toFile();
            try {
                StandaloneXml standaloneXml = serializer.read(StandaloneXml.class, standalone);
                Set<SystemProperty> propertySet = standaloneXml.getSystemProperty();
                if (!CollectionUtils.isEmpty(propertySet)) {
                    for (SystemProperty property : propertySet) {
                        if (INIT_DATA_PROPERTY_NAME.equalsIgnoreCase(property.getName())) {
                            initData = Paths.get(property.getValue());
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }
        logger.info("Init data path is - {}", initData.toString());
        return initData;
    }

    public boolean copyDir(Path source, Path target) {
        File sourceDir = source.toFile();
        File targetDir = target.toFile();
        try {
            FileUtils.copyDirectory(sourceDir, targetDir);
            return true;
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
            return false;
        }
    }

    public boolean delete(Path source) {
        File dir = source.toFile();
        try {
            FileUtils.deleteDirectory(dir);
            return true;
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
            return false;
        }
    }
}
