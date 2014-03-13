package ru.intertrust.cm.core.gui.api.server.plugin;

import java.util.Map;

import ru.intertrust.cm.core.business.api.dto.ImagePathValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 18/02/14
 *         Time: 12:05 PM
 */
public interface DefaultImageMapper {
    public Map<String, Map<Value, ImagePathValue>> getImageMaps(
            Map<String, CollectionColumnProperties> columnsProperties);
}
