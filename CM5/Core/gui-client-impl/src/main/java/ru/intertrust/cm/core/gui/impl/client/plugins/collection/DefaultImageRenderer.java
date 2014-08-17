package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import ru.intertrust.cm.core.config.gui.collection.view.ImageMappingsConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.ImageCell;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
@ComponentName("default.image.renderer")
public class DefaultImageRenderer implements ImageCellRenderer{
    public CollectionColumn getImageColumn(ImageMappingsConfig imageMappingsConfig) {
        String imageWidth = imageMappingsConfig.getImageWidth();
        String imageHeight = imageMappingsConfig.getImageHeight();
        ImageCollectionColumn imageColumn = new ImageCollectionColumn(new ImageCell(imageWidth, imageHeight));

        return imageColumn;
    }

    @Override
    public String getName() {
        return "default.image.renderer";
    }

    @Override
    public Component createNew() {
        return new DefaultImageRenderer();
    }
}
