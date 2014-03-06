package ru.intertrust.cm.core.business.load;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.module.ImportFileConfiguration;
import ru.intertrust.cm.core.config.module.ImportFilesConfiguration;
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
        	//перезаписывать ли данные(по умолчанию false)
        	Boolean rewriteGroup;
        	Boolean rewriteFile;
        	Boolean rewrite = false;
            for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
                ImportFilesConfiguration importFiles = moduleConfiguration.getImportFiles();
                //Получаем значение перезаписи для группы файлов
                rewriteGroup = importFiles.getRewrite();
                for (ImportFileConfiguration importFile : importFiles.getImportFiles()) {
                	//Получаем значение перезаписи для файла
                	rewriteFile = importFile.getRewrite();
                	//Если у файла установлен флаг - используем его
                	if (rewriteFile != null){
                		rewrite = rewriteFile;
                	}
                	//Если у файла нет флага, а у группы есть - используем его
                	else if (rewriteGroup != null){
                		rewrite = rewriteGroup;
                	}
                	//Если флаг ни у файла ни у группы не установлен используем false
                	else{
                		rewrite = false;
                	}
                    ImportData importData = new ImportData(collectionsDao, 
                            configurationExplorer, domainObjectDao, accessControlService, attachmentContentDao, null);

                    importData.importData(readFile(new URL(moduleConfiguration.getModuleUrl().toString() + importFile.getFileName())), importFiles.getCsvEncoding(),rewrite);
                    logger.info("Import system data from file " + importFile.getFileName());
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
