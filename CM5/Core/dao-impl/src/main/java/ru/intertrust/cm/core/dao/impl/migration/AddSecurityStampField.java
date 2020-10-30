package ru.intertrust.cm.core.dao.impl.migration;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.Migrator;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;

/**
 * Добавляет поле security_stamp и security_stamp_type во все корневые доменные объекты
 */
@ServerComponent(name = "addSecurityStampField")
public class AddSecurityStampField implements Migrator {
    private static Logger logger = LoggerFactory.getLogger(AddSecurityStampField.class);

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DataStructureDao dataStructureDao;

    @Override
    public void execute() {
        logger.info("Start execute AddSecurityStampField migrator");

        Map<String, Map<String, ColumnInfo>> schemaTables = dataStructureDao.getSchemaTables();

        Collection<DomainObjectTypeConfig> typeConfigs = configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
        for (DomainObjectTypeConfig typeConfig : typeConfigs) {
            if (typeConfig.getExtendsAttribute() == null && !typeConfig.isTemplate() && !ifStampColumnExists(schemaTables, typeConfig.getName())){
                addSecurityStamp(typeConfig);
            }
        }
        logger.info("Finish AddSecurityStampField migrator");
    }

    private boolean ifStampColumnExists(Map<String, Map<String, ColumnInfo>> schemaTables, String typeName){
        return schemaTables.get(typeName.toLowerCase()).containsKey("security_stamp");
    }

    private void addSecurityStamp(DomainObjectTypeConfig typeConfig) {
        logger.debug("Add security_stamp to " + typeConfig.getName());
        ReferenceFieldConfig securityStampField = new ReferenceFieldConfig();
        securityStampField.setName("security_stamp");
        securityStampField.setType("security_stamp");
        dataStructureDao.addColumns(typeConfig, Collections.singletonList(securityStampField));
    }
}
