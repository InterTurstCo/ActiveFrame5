package ru.intertrust.cm.core.business.globalsettings;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPointHandler;

@ExtensionPoint(filter = "global_server_settings")
public class AfterSaveGlobalSettingsExtensionPoint implements AfterSaveExtensionHandler {

    @Autowired
    private GlobalCacheClient globalCacheClient;

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        // При смене глобальных настроек необходимо сбросить глобальный кэш на всех узлах
        globalCacheClient.clear();
    }
}
