package ru.intertrust.cm.core.gui.impl.server.plugin;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.util.ModelConstants;
import ru.intertrust.cm.core.config.gui.collection.view.ImageMappingsConfig;
import ru.intertrust.cm.core.config.gui.collection.view.MappingConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.DefaultImageMapper;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 18/02/14
 *         Time: 12:05 PM
 */
public class DefaultImageMapperImpl implements DefaultImageMapper {

    public DefaultImageMapperImpl() {

    }

    public Map<String, Map<Value, ImagePathValue>> getImageMaps(
            final Map<String, CollectionColumnProperties> columnsProperties) {
        Map<String, Map<Value, ImagePathValue>> imagePathsMappings = new HashMap<String, Map<Value, ImagePathValue>>();
        Set<String> fields = columnsProperties.keySet();
        for (String field : fields) {
            CollectionColumnProperties columnProperties = columnsProperties.get(field);
            ImageMappingsConfig imageMappingsConfig = columnProperties.getImageMappingsConfig();
            if (imageMappingsConfig == null) {
                imagePathsMappings.put(field, null);
                continue;
            }
            List<MappingConfig> mappingConfigs = imageMappingsConfig.getMappingConfigs();
            String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
            Map<Value, ImagePathValue> valueMap = initMapDependingOnType(mappingConfigs, fieldType);
            imagePathsMappings.put(field, valueMap);

        }
        return imagePathsMappings;
    }

    private Map<Value, ImagePathValue> initMapDependingOnType(final List<MappingConfig> mappingConfigs,
                                                              final String fieldType) {
        if (fieldType.equalsIgnoreCase(ModelConstants.STRING_TYPE)) {
            return initStringValues(mappingConfigs);
        } else if (fieldType.equalsIgnoreCase(ModelConstants.BOOLEAN_TYPE)) {
            return initBooleanValues(mappingConfigs);
        } else if (fieldType.equalsIgnoreCase(ModelConstants.LONG_TYPE)) {
            return initLongValues(mappingConfigs);
        }
        return null;
    }

    private Map<Value, ImagePathValue> initStringValues(final List<MappingConfig> mappings) {
        Map<Value, ImagePathValue> valueMap = new HashMap<Value, ImagePathValue>();
        for (MappingConfig config : mappings) {
            String value = config.getValue();
            ImagePathValue imagePathValue = new ImagePathValue(config.getImage());
            if (value.equalsIgnoreCase("null")) {
                StringValue stringValueNull = new StringValue();
                valueMap.put(stringValueNull, imagePathValue);
                StringValue stringValueEmpty = new StringValue("");
                valueMap.put(stringValueEmpty, imagePathValue);
            } else {
                StringValue stringValue = new StringValue(value);
                valueMap.put(stringValue, imagePathValue);
            }
        }
        return valueMap;
    }

    private Map<Value, ImagePathValue> initBooleanValues(final List<MappingConfig> mappings) {
        Map<Value, ImagePathValue> valueMap = new HashMap<Value, ImagePathValue>();
        for (MappingConfig config : mappings) {
            String value = config.getValue();
            ImagePathValue imagePathValue = new ImagePathValue(config.getImage());
            if (value.equalsIgnoreCase("")) {
                BooleanValue booleanValueNull = new BooleanValue();
                valueMap.put(booleanValueNull, imagePathValue);
            } else {
                BooleanValue booleanValue = new BooleanValue(Boolean.parseBoolean(value));
                valueMap.put(booleanValue, imagePathValue);
            }
        }
        return valueMap;

    }

    private Map<Value, ImagePathValue> initLongValues(List<MappingConfig> mappings) {
        Map<Value, ImagePathValue> valueMap = new HashMap<Value, ImagePathValue>();
        for (MappingConfig config : mappings) {
            String value = config.getValue();
            ImagePathValue imagePathValue = new ImagePathValue(config.getImage());
            if (value.equalsIgnoreCase("")) {
                LongValue longValueNull = new LongValue();
                valueMap.put(longValueNull, imagePathValue);
            } else {
                LongValue longValue = new LongValue(Long.parseLong(value));
                valueMap.put(longValue, imagePathValue);
            }
        }
        return valueMap;
    }


}
