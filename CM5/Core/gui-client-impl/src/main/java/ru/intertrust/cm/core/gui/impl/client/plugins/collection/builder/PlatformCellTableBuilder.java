package ru.intertrust.cm.core.gui.impl.client.plugins.collection.builder;

import com.google.gwt.dom.builder.shared.DivBuilder;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.DefaultCellTableBuilder;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.05.2015
 *         Time: 8:06
 */
public class PlatformCellTableBuilder extends DefaultCellTableBuilder<CollectionRowItem> {

    public PlatformCellTableBuilder(AbstractCellTable<CollectionRowItem> cellTable) {
        super(cellTable);

    }

    @Override
    public void buildRowImpl(CollectionRowItem rowValue, int absRowIndex) {
        if(CollectionRowItem.RowType.BUTTON.equals(rowValue.getRowType())){
            TableRowBuilder tr = startRow();
            TableCellBuilder cellBuilder = tr.startTD();
            cellBuilder.colSpan(cellTable.getColumnCount());
            AbstractCellTable.Style style = cellTable.getResources().style();

            String styleForCell = style.cell();
            cellBuilder.className(styleForCell);

            DivBuilder wrapBuilder = cellBuilder.startDiv();
            wrapBuilder.className("wrap_btn");
            for(int index = 0; index < rowValue.getNestingLevel()+1; index++){
                DivBuilder divBuilder = cellBuilder.startDiv();
                divBuilder.className("expand_offset");
                cellBuilder.endDiv();
            }
            DivBuilder divBuilder = cellBuilder.startDiv();
            divBuilder.className("collectionButtonCellWrapper");
            divBuilder.title(LocalizeUtil.get(LocalizationKeys.MORE_ITEMS_KEY));
            divBuilder.text("...");
            cellBuilder.endDiv();
            cellBuilder.endDiv();

            tr.endTD();
            tr.endTR();
        }else {
            super.buildRowImpl(rowValue, absRowIndex);
        }
    }

}

