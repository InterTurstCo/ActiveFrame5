package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.ImageCell;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
public class ImageCollectionColumn extends CollectionParameterizedColumn {

    public ImageCollectionColumn(ImageCell cell) {
        super(cell);
    }

    @Override
    public String getValue(CollectionRowItem object) {
        return object.getStringValue(fieldName);
    }
}
