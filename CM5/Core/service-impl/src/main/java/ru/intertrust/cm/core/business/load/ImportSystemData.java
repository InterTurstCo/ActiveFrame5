package ru.intertrust.cm.core.business.load;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Данный сервис загружает системные справочники из *.CSV файлов которые указаны в конфигурации модулей.
 * @author larin
 * 
 */
public class ImportSystemData {

    private static final Logger logger = Logger.getLogger(ImportSystemData.class);

    @Autowired
    private CollectionsDao collectionsDao;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private DomainObjectDao domainObjectDao;
    @Autowired
    private AccessControlService accessControlService;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private AttachmentContentDao attachmentContentDao;

    public void onLoad() {
        try {

            for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
                for (String importFile : moduleConfiguration.getImportFiles()) {


                    ImportData importData = new ImportData(collectionsDao, 
                            configurationExplorer, domainObjectDao, accessControlService, attachmentContentDao, null);

                    importData.importData(readFile(new URL(moduleConfiguration.getModuleUrl().toString() + importFile)));
                    logger.info("Import system data from file " + importFile);
                }
            }

        } catch (Exception ex) {
            throw new FatalException("Can not load system dictionaries.", ex);
        }
    }

    /**
     * Получение файла в виде массива байт
     * @param file
     * @return
     * @throws IOException
     */
    private byte[] readFile(URL fileUrl) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream input = null;
        try {
            input = fileUrl.openStream();
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } finally {
            input.close();
        }
    }

}
