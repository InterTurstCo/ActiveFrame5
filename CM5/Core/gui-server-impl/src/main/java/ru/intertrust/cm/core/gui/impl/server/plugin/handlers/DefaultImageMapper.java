package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.collection.view.ImageMappingsConfig;
import ru.intertrust.cm.core.config.gui.collection.view.MappingConfig;
import ru.intertrust.cm.core.gui.model.ComponentName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
@ComponentName("default.image.mapper")
public class DefaultImageMapper {
    private Map<Value, ImagePathValue> imagePathsMappings;

    public DefaultImageMapper() {

    }

    public void init(ImageMappingsConfig imageMappingsConfig, String fieldType) {
        imagePathsMappings = new HashMap<Value, ImagePathValue>();
        List<MappingConfig> mappings = imageMappingsConfig.getMappingConfigs();
        if (fieldType.equalsIgnoreCase("string")) {
            initStringValues(mappings);
        } else if (fieldType.equalsIgnoreCase("boolean")) {
            initBooleanValues(mappings);
        } else if (fieldType.equalsIgnoreCase("long")) {
            initLongValues(mappings);
        }

    }

    private void initStringValues(List<MappingConfig> mappings) {
        for (MappingConfig config : mappings) {
            String value = config.getValue();
            ImagePathValue imagePathValue = new ImagePathValue(config.getImage());
            if (value.equalsIgnoreCase("null")) {
                StringValue stringValueNull = new StringValue();
                imagePathsMappings.put(stringValueNull, imagePathValue);
                StringValue stringValueEmpty = new StringValue("");
                imagePathsMappings.put(stringValueEmpty, imagePathValue);
            } else {
                StringValue stringValue = new StringValue(value);
                imagePathsMappings.put(stringValue, imagePathValue);
            }
        }
    }

    private void initBooleanValues(List<MappingConfig> mappings) {
        for (MappingConfig config : mappings) {
            String value = config.getValue();
            ImagePathValue imagePathValue = new ImagePathValue(config.getImage());
            if (value.equalsIgnoreCase("")) {
                BooleanValue booleanValueNull = new BooleanValue();
                imagePathsMappings.put(booleanValueNull, imagePathValue);
            } else {
                BooleanValue booleanValue = new BooleanValue(Boolean.parseBoolean(value));
                imagePathsMappings.put(booleanValue, imagePathValue);
            }
        }
    }

    private void initLongValues(List<MappingConfig> mappings) {
        for (MappingConfig config : mappings) {
            String value = config.getValue();
            ImagePathValue imagePathValue = new ImagePathValue(config.getImage());
            if (value.equalsIgnoreCase("")) {
                LongValue longValueNull = new LongValue();
                imagePathsMappings.put(longValueNull, imagePathValue);
            } else {
                LongValue longValue = new LongValue(Long.parseLong(value));
                imagePathsMappings.put(longValue, imagePathValue);
            }
        }
    }

    public ImagePathValue getImagePathValue(Value key) {

        return imagePathsMappings.get(key);
    }
}
