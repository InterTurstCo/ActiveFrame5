package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.*;

/**
 * Валидатор уникальности имен конфигураций верхнего уровня одного типа
 */
public class UniqueNameLogicalValidator implements ConfigurationValidator {

    final static Logger logger = LoggerFactory.getLogger(UniqueNameLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;

    public UniqueNameLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет логическую валидацию конфигурации
     */
    @Override
    public List<LogicalErrors> validate() {
        List<LogicalErrors> logicalErrorsList = new ArrayList<>();

        Set<Class<?>> topLevelConfigClasses = configurationExplorer.getTopLevelConfigClasses();
        if (topLevelConfigClasses == null || topLevelConfigClasses.isEmpty()) {
            return logicalErrorsList;
        }

        for (Class topLevelConfigClass : topLevelConfigClasses) {
            List<String> configNames = getConfigNames(topLevelConfigClass);
            NameComparator nameComparator = new NameComparator();
            Collections.sort(configNames, nameComparator);

            for (int i = 1; i < configNames.size(); i ++) {
                String name1 = configNames.get(i - 1);
                String name2 = configNames.get(i);

                if (name1 != null && nameComparator.compare(name1, name2) == 0) {
                    LogicalErrors logicalErrors = LogicalErrors.getInstance(name1, getTopLevelConfigTagName(topLevelConfigClass));
                    logicalErrors.addError("There are top level configurations with identical name");
                    logicalErrorsList.add(logicalErrors);
                }
            }
        }

        return logicalErrorsList;
    }

    private List<String> getConfigNames(Class clazz) {
        Configuration configuration = configurationExplorer.getConfiguration();
        List<String> result = new LinkedList<>();

        for(TopLevelConfig config : configuration.getConfigurationList()) {
            if (config.getClass().equals(clazz)) {
                result.add(config.getName());

                // Для типов ДО необходимо включать в проверку имена вложений, т.к. они также являются типами ДО
                if (DomainObjectTypeConfig.class.equals(clazz)) {
                    DomainObjectTypeConfig doTypeConfig = (DomainObjectTypeConfig) config;

                    if (doTypeConfig.getAttachmentTypesConfig() != null &&
                            doTypeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs() != null) {
                        for (AttachmentTypeConfig attachmentTypeConfig :
                                doTypeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs()) {
                            result.add(attachmentTypeConfig.getName());
                        }
                    }
                }
            }
        }

        return result;
    }

    private class NameComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            } else {
                return o1.compareToIgnoreCase(o2);
            }
        }
    }

    private String getTopLevelConfigTagName(Class<TopLevelConfig> topLevelConfigClass) {
        Root root = topLevelConfigClass.getAnnotation(Root.class);
        if (root != null) {
            return root.name();
        } else {
            return topLevelConfigClass.getName();
        }
    }
}


