package ru.intertrust.cm.core.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.GlobalServerSettingsService;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.dao.api.UserTransactionService;
import ru.intertrust.cm.core.dao.api.extension.AfterClearGlobalCacheExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.BeforeSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Точка расширения вызывается при сохранениии всех доменных объектов и в
 * зависимости от настройки логирует или нет стек вызова Логирование
 * настраивается в глобальных серверных настройках с ключем LOG_SAVE_TYPE В
 * качестве значения можно записывать имена типов разделенных запятой
 * @author larin
 *
 */
@ExtensionPoint
public class SaveLogger implements BeforeSaveExtensionHandler, AfterClearGlobalCacheExtentionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SaveLogger.class);
    private static final String LOG_SAVE_TYPE = "LOG_SAVE_TYPE";
    private static final String STRING_SETTINGS = "string_settings";

    @Autowired
    private GlobalServerSettingsService globalServerSettingsService;
    
    @Autowired
    private UserTransactionService transactionService;

    private Set<String> typeNames;

    @Override
    public void onBeforeSave(DomainObject domainObject, List<FieldModification> changedFields) {
        if (!logger.isDebugEnabled()) {
            return;
        }

        if (typeNames == null) {
            init();
        }
        
        if (typeNames.contains(Case.toLower(domainObject.getTypeName()))) {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            String stackTrace = "";
            for (StackTraceElement stackTraceElement : stack) {
                stackTrace += "\t" + stackTraceElement + "\n";
            }
            
            String transactionId = transactionService.getTransactionId();
            
            long modifyDate = domainObject.getModifiedDate() != null ? domainObject.getModifiedDate().getTime() : 0;
            
            logger.debug("Save " + domainObject + "\ntransactionId=" + transactionId + "\nmodifyDate=" + modifyDate + "\n" + stackTrace);
        }

        //На текущем узле кэш настроек скидывается при сохранении настроек, на остальных узлах надо скинуть глобальный 
        if (domainObject.getTypeName().equalsIgnoreCase(STRING_SETTINGS) 
                && domainObject.getString("name").equalsIgnoreCase(LOG_SAVE_TYPE)) {
            typeNames = null;
        }
    }

    private void init() {
        synchronized (SaveLogger.class) {
            Set<String> names = new HashSet<String>();
            String types = globalServerSettingsService.getString(LOG_SAVE_TYPE);
            logger.debug("Init SaveLogger from param " + types);
            if (types != null) {
                String[] typeArr = types.split(",");
                for (String type : typeArr) {
                    names.add(Case.toLower(type));
                }
            }
            typeNames = names;
        }
    }

    @Override
    public void onClearGlobalCache() {
        typeNames = null;        
    }

}
