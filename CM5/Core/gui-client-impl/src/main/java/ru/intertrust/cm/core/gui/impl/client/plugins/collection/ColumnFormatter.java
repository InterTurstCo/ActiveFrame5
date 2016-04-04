package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.collection.view.ImageMappingsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RendererConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverterFactory;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.ControlExpandableCell;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.EditableTextCell;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.HierarchicalCell;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.util.StringUtil;

import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CHECK_BOX_COLUMN_NAME;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CHECK_BOX_MAX_WIDTH;

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
        boolean expandable = (Boolean) columnProperties.getProperty(CollectionColumnProperties.EXPANDABLE);
        final CollectionColumn collectionColumn;
        RendererConfig rendererConfig = columnProperties.getRendererConfig();
        if (rendererConfig != null) {
            DefaultImageRenderer renderer = ComponentRegistry.instance.get(rendererConfig.getValue());
            ImageMappingsConfig mappingsConfig = columnProperties.getImageMappingsConfig();
            collectionColumn = renderer.getImageColumn(mappingsConfig);
            collectionColumn.setFieldName(field);
            collectionColumn.setResizable(resizable);
        }
        else if(rendererConfig == null && columnProperties.getActionRefConfig()!=null){
            DefaultActionCellRenderer actionCellRenderer = ComponentRegistry.instance.get(DefaultActionCellRenderer.ACT_CELL_RENDERER_COMPONENT);
            String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
            ValueConverter converter = ValueConverterFactory.getConverter(fieldType);
            converter.init(columnProperties.getProperties());
            collectionColumn = actionCellRenderer.getActionColumn(field, eventBus,resizable, converter,columnProperties.getActionRefConfig());
        }
        else  if (columnProperties.getProperty(CollectionColumnProperties.CHILD_COLLECTIONS_CONFIG) != null) {
            List<ChildCollectionViewerConfig> childCollectionsConfig =
                    (List<ChildCollectionViewerConfig>) columnProperties.getProperty(CollectionColumnProperties.CHILD_COLLECTIONS_CONFIG);
            String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
            String drillDownStyle = (String) columnProperties.getProperty(CollectionColumnProperties.DRILL_DOWN_STYLE);
            ValueConverter converter = ValueConverterFactory.getConverter(fieldType);
            converter.init(columnProperties.getProperties());
            collectionColumn = new HierarchicalCollectionColumn(new HierarchicalCell(getCssStyleForText(textBreakStyle), drillDownStyle, field),
                    field, resizable, converter, childCollectionsConfig, eventBus);
        } else if(expandable){
            String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
            ValueConverter converter = ValueConverterFactory.getConverter(fieldType);
            converter.init(columnProperties.getProperties());
            ControlExpandableCell expandableCell = new ControlExpandableCell(getCssStyleForText(textBreakStyle), field);
            collectionColumn = new ExpandableColumn(expandableCell, field, resizable, converter, eventBus);
        }
        else {
            String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
            ValueConverter converter = ValueConverterFactory.getConverter(fieldType);
            converter.init(columnProperties.getProperties());
            EditableTextCell textCell = new EditableTextCell(getCssStyleForText(textBreakStyle), field);
            collectionColumn = new TextCollectionColumn(textCell, field, eventBus,resizable, converter);
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

    public static void formatCheckBoxColumn(CollectionColumn checkBoxColumn){
        checkBoxColumn.setMaxWidth(CHECK_BOX_MAX_WIDTH);
        checkBoxColumn.setMinWidth(CHECK_BOX_MAX_WIDTH);
        checkBoxColumn.setUserWidth(CHECK_BOX_MAX_WIDTH);
        checkBoxColumn.setVisible(true);
        checkBoxColumn.setResizable(false);
        checkBoxColumn.setMoveable(false);
        checkBoxColumn.setDataStoreName(CHECK_BOX_COLUMN_NAME);
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
