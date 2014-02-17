package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import ru.intertrust.cm.core.config.gui.form.widget.RendererConfig;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverterFactory;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.ImageCell;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.TextCell;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
public class ColumnFormatter {
    private static final String CUT_STYLE = "cut";

    public static CollectionColumn createFormattedColumn(CollectionColumnProperties columnProperties) {
        String field = (String) columnProperties.getProperty(CollectionColumnProperties.FIELD_NAME);
        String textBreakStyle = (String) columnProperties.getProperty(CollectionColumnProperties.TEXT_BREAK_STYLE);
        Boolean resizable = (Boolean) columnProperties.getProperty(CollectionColumnProperties.RESIZABLE);
        CollectionColumn collectionColumn = null;
        RendererConfig rendererConfig = columnProperties.getRendererConfig();
        if (rendererConfig != null) {
            DefaultImageRenderer renderer = new DefaultImageRenderer();
            collectionColumn = new ImageCollectionColumn(new ImageCell(), field, resizable);
        } else {
            String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
            ValueConverter converter = ValueConverterFactory.getConverter(fieldType);
            converter.init(columnProperties.getProperties());
            collectionColumn = new TextCollectionColumn(new TextCell(getCssStyleForText(textBreakStyle)), field, resizable, converter);
        }
        adjustColumnMinAndMaxWidth(collectionColumn, columnProperties);
        String columnName = (String) columnProperties.getProperty(CollectionColumnProperties.NAME_KEY);
        collectionColumn.setDataStoreName(columnName);
        boolean sortable = (Boolean) columnProperties.getProperty(CollectionColumnProperties.SORTABLE);
        collectionColumn.setSortable(sortable);
        return collectionColumn;
    }

    private static String getCssStyleForText(String textBreakStyle) {
        if (CUT_STYLE.equalsIgnoreCase(textBreakStyle)) {
            return " style = \"overflow: hidden; text-overflow: ellipsis; white-space: nowrap;\"";

        }
        return "";
    }

    private static void adjustColumnMinAndMaxWidth(CollectionColumn column, CollectionColumnProperties columnProperties) {
        String minWidthString = (String) columnProperties.getProperty(CollectionColumnProperties.MIN_WIDTH);

        if (minWidthString != null) {
            int minWidth = Integer.parseInt(minWidthString.replaceAll("\\D+", ""));
            column.setMinWidth(minWidth);
        }
        String maxWidthString = (String) columnProperties.getProperty(CollectionColumnProperties.MAX_WIDTH);
        if (maxWidthString != null) {
            int maxWidth = Integer.parseInt(maxWidthString.replaceAll("\\D+", ""));
            column.setMaxWidth(maxWidth);
        }

    }
}
