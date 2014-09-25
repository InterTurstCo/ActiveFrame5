package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.collection.view.ImageMappingsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RendererConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverterFactory;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.HierarchyCell;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.TextCell;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.util.StringUtil;

import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
public class ColumnFormatter {
    private static final String CUT_STYLE = "cut";

    private ColumnFormatter() {
    }

    public static CollectionColumn createFormattedColumn(CollectionColumnProperties columnProperties, EventBus eventBus) {
        String field = (String) columnProperties.getProperty(CollectionColumnProperties.FIELD_NAME);
        String textBreakStyle = (String) columnProperties.getProperty(CollectionColumnProperties.TEXT_BREAK_STYLE);
        Boolean resizable = (Boolean) columnProperties.getProperty(CollectionColumnProperties.RESIZABLE);
        final CollectionColumn collectionColumn;
        RendererConfig rendererConfig = columnProperties.getRendererConfig();
        if (rendererConfig != null) {
            DefaultImageRenderer renderer = ComponentRegistry.instance.get(rendererConfig.getValue());
            ImageMappingsConfig mappingsConfig = columnProperties.getImageMappingsConfig();
            collectionColumn = renderer.getImageColumn(mappingsConfig);
            collectionColumn.setFieldName(field);
            collectionColumn.setResizable(resizable);
        } else  if (columnProperties.getProperty(CollectionColumnProperties.CHILD_COLLECTIONS_CONFIG) != null) {
            List<ChildCollectionViewerConfig> childCollectionsConfig =
                    (List<ChildCollectionViewerConfig>) columnProperties.getProperty(CollectionColumnProperties.CHILD_COLLECTIONS_CONFIG);
            String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
            String drillDownStyle = (String) columnProperties.getProperty(CollectionColumnProperties.DRILL_DOWN_STYLE);
            ValueConverter converter = ValueConverterFactory.getConverter(fieldType);
            converter.init(columnProperties.getProperties());
            collectionColumn = new HierarchicalCollectionColumn(new HierarchyCell(getCssStyleForText(textBreakStyle), drillDownStyle),
                    field, resizable, converter, childCollectionsConfig, eventBus);
        } else {
            String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
            ValueConverter converter = ValueConverterFactory.getConverter(fieldType);
            converter.init(columnProperties.getProperties());
            collectionColumn = new TextCollectionColumn(new TextCell(getCssStyleForText(textBreakStyle)), field, resizable, converter);
        }
        setColumnWidthProperties(collectionColumn, columnProperties);
        String columnName = (String) columnProperties.getProperty(CollectionColumnProperties.NAME_KEY);
        collectionColumn.setDataStoreName(columnName);
        boolean sortable = (Boolean) columnProperties.getProperty(CollectionColumnProperties.SORTABLE);
        collectionColumn.setSortable(sortable);
        boolean hidden = (Boolean) columnProperties.getProperty(CollectionColumnProperties.HIDDEN);
        collectionColumn.setVisible(!hidden);
        return collectionColumn;
    }

    private static String getCssStyleForText(String textBreakStyle) {
        if (CUT_STYLE.equalsIgnoreCase(textBreakStyle)) {
            return " style = \"overflow: hidden; text-overflow: ellipsis; white-space: nowrap;\"";
        }
        return "";
    }

    private static void setColumnWidthProperties(
            final CollectionColumn column, final CollectionColumnProperties columnProperties) {
        final Integer minWidth = StringUtil.integerFromString(
                (String) columnProperties.getProperty(CollectionColumnProperties.MIN_WIDTH), null);
        if (minWidth != null) {
            column.setMinWidth(minWidth);
        }
        final Integer maxWidth = StringUtil.integerFromString(
                (String) columnProperties.getProperty(CollectionColumnProperties.MAX_WIDTH), null);
        if (maxWidth != null) {
            column.setMaxWidth(maxWidth);
        }
        column.setUserWidth(StringUtil.integerFromString(
                (String) columnProperties.getProperty(CollectionColumnProperties.WIDTH), 0));
    }
}
