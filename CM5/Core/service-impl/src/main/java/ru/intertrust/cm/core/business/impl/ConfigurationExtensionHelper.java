package ru.intertrust.cm.core.business.impl;

import org.simpleframework.xml.Root;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveMap;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Denis Mitavskiy
 *         Date: 15.05.2017
 *         Time: 18:21
 */
public class ConfigurationExtensionHelper {
    final static org.slf4j.Logger logger = LoggerFactory.getLogger(ConfigurationExtensionHelper.class);
    private volatile Map<Class<?>, CaseInsensitiveMap<TopLevelConfig>> topLevelDistributiveConfigs = new HashMap<>();
    private final Object LOCK = new Object();
    
    @Autowired
    private ConfigurationExplorer configurationExplorer;

    public TopLevelConfig getDistributiveConfig(Class type, String name) {
        if (topLevelDistributiveConfigs.isEmpty()) {
            synchronized (LOCK) {
                if (topLevelDistributiveConfigs.isEmpty()) {
                    final List<TopLevelConfig> topLevelConfigs = configurationExplorer.getDistributiveConfiguration().getConfigurationList();
                    for (TopLevelConfig topLevelConfig : topLevelConfigs) {
                        CaseInsensitiveMap<TopLevelConfig> typeConfigsByName = topLevelDistributiveConfigs.get(topLevelConfig.getClass());
                        if (typeConfigsByName == null) {
                            typeConfigsByName = new CaseInsensitiveMap<>();
                            topLevelDistributiveConfigs.put(topLevelConfig.getClass(), typeConfigsByName);
                        }
                        typeConfigsByName.put(topLevelConfig.getName(), topLevelConfig);
                    }
                }
            }
        }
        final CaseInsensitiveMap<TopLevelConfig> typeConfigsByName = topLevelDistributiveConfigs.get(type);
        if (typeConfigsByName == null) {
            return null;
        }
        return typeConfigsByName.get(name);
    }

    public Map<String, TagTypeInfo> getTagClassMapping() {
        final Set<Class<?>> topLevelConfigClasses = configurationExplorer.getTopLevelConfigClasses();
        HashMap<String, TagTypeInfo> mapping = new HashMap<>(topLevelConfigClasses.size() * 3 / 2);
        for (Class clazz : topLevelConfigClasses) {
            final String tagType = getTagType(clazz);
            if (mapping.containsKey(tagType)) {
                logger.error("Top level Tag Type is defined twice (possible in different namespaces: " + tagType);
            }
            mapping.put(tagType, new TagTypeInfo(clazz));
        }
        return mapping;
    }

    private static String getTagType(Class<? extends TopLevelConfig> clazz) {
        return clazz.getAnnotation(Root.class).name();
    }

    public static class TagTypeInfo {
        private final Class<? extends TopLevelConfig> clazz;
        private TopLevelConfig.ExtensionPolicy creationPolicy;

        public TagTypeInfo(Class<? extends TopLevelConfig> clazz) {
            this.clazz = clazz;
            try {
                final TopLevelConfig config = clazz.newInstance();
                creationPolicy = config.getCreationPolicy();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Can't instantiate class: " + clazz, e);
                creationPolicy = TopLevelConfig.ExtensionPolicy.None;
            }
        }

        public Class<? extends TopLevelConfig> getTopLevelConfigClass() {
            return clazz;
        }

        public TopLevelConfig.ExtensionPolicy getCreationPolicy() {
            return creationPolicy;
        }
    }
}
