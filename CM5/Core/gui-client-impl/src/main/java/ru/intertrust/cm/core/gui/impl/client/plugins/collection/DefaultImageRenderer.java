package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.ImageCell;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
@ComponentName("default.image.renderer")
public class DefaultImageRenderer {
    public CollectionColumn getImageColumn(String field) {
        return new ImageCollectionColumn(new ImageCell(), field, true);
    }
}
