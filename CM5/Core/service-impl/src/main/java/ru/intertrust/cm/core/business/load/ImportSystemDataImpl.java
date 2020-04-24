package ru.intertrust.cm.core.business.load;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
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
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

/**
 * Данный сервис загружает системные справочники из *.CSV файлов которые указаны
 * в конфигурации модулей.
 * @author larin
 * 
 */
@Stateless
@Local(ImportSystemData.class)
@Remote(ImportSystemData.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ImportSystemDataImpl implements ImportSystemData, ImportSystemData.Remote {

    private static final Logger logger = Logger.getLogger(ImportSystemDataImpl.class);

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
    @Autowired
    private ApplicationContext springContext;
    @Resource
    private EJBContext context;        

    public void load() {
        String fileName = null;
        String moduleName = null;
        try {
            //перезаписывать ли данные(по умолчанию false)
            Boolean rewriteGroup;
            Boolean rewriteFile;
            Boolean rewrite = false;
            //цикл по модулям
            for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
                moduleName = moduleConfiguration.getName();
                List<ImportFilesConfiguration> importFiles = moduleConfiguration.getImportFiles();
                //Проверка на то что у модуля есть что импортировать
                if (importFiles != null) {
                    for (ImportFilesConfiguration importFilesConfiguration : importFiles) {
                        //У группы файлов проверяем что группа импортируется только на чистую базу (тестовые данные)
                        if (isNeedImportByCleanBase(importFilesConfiguration)) {
                            //Получаем значение перезаписи для группы файлов
                            rewriteGroup = importFilesConfiguration.getRewrite();
                            if (importFilesConfiguration.getImportFiles() != null) {
                                for (ImportFileConfiguration importFile : importFilesConfiguration.getImportFiles()) {
                                    fileName = importFile.getFileName();
                                    //Получаем значение перезаписи для файла
                                    rewriteFile = importFile.getRewrite();
                                    //Если у файла установлен флаг - используем его
                                    if (rewriteFile != null) {
                                        rewrite = rewriteFile;
                                    }
                                    //Если у файла нет флага, а у группы есть - используем его
                                    else if (rewriteGroup != null) {
                                        rewrite = rewriteGroup;
                                    }
                                    //Если флаг ни у файла ни у группы не установлен используем false
                                    else {
                                        rewrite = false;
                                    }
                                    ImportData importData = (ImportData) springContext.getBean(ImportData.SYSTEM_IMPORT_BEAN);

                                    importData.importData(readFile(new URL(moduleConfiguration.getModuleUrl().toString()
                                            + importFile.getFileName())), importFilesConfiguration.getCsvEncoding(), rewrite, null, null);
                                    logger.info("Import system data from file " + importFile.getFileName());
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            throw new FatalException("Can not load system dictionaries module=" + moduleName + "; file=" + fileName, ex);
        }
    }

    /**
     * Проверка на то что импорт надо делать только на чистой базе и проверка
     * чистая ли база
     * @param importFilesConfiguration
     * @return
     */
    private boolean isNeedImportByCleanBase(ImportFilesConfiguration importFilesConfiguration) {
        boolean result = true;
        //Проверяем установлен ли атрибут проверки заполненности базы
        if (importFilesConfiguration.getOnCleanBaseByType() != null) {
            //Выполняем запрос на получение записей в базе
            String query = "select id from " + importFilesConfiguration.getOnCleanBaseByType();
            IdentifiableObjectCollection collection =
                    collectionsDao.findCollectionByQuery(query, 0, 1, accessControlService.createSystemAccessToken(this.getClass().getName()));
            //Если есть хотя бы одна запись то импорт не производим
            result = collection.size() == 0;
        }
        return result;
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
            if (input != null) {
                input.close();
            }
        }
    }

}
