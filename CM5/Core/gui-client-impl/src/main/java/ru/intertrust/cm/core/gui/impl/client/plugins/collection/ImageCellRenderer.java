package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import ru.intertrust.cm.core.config.gui.collection.view.ImageMappingsConfig;
import ru.intertrust.cm.core.gui.api.client.Component;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 16.08.2014
 *         Time: 16:32
 */
public interface ImageCellRenderer extends Component {
   CollectionColumn getImageColumn(ImageMappingsConfig imageMappingsConfig);
}
