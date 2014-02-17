package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.ImageCell;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
@ComponentName("default.image.renderer")
public class DefaultImageRenderer implements Component{
    public CollectionColumn getImageColumn(String fieldName) {
        ImageCollectionColumn imageColumn = new ImageCollectionColumn(new ImageCell());
        imageColumn.setFieldName(fieldName);
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
