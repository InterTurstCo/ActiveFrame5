/**
 * Copyright 2000-2013 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;


import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.Event;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.DGCellTableResourceAdapter;
import ru.intertrust.cm.core.gui.model.plugin.MyData;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Описание (от mike-khukh)
 *
 * @author mike-khukh
 * @since 4.1
 */
public class CellTableHeader<T extends MyData> extends CellTableEx<T> {



    private final List<Header<?>>               headers                       = new ArrayList<Header<?>>();

    public CellTableHeader(DGCellTableResourceAdapter adapter) {
        super(999, adapter.getResources());

        initialize(adapter);
    }



    @Override
    public void insertColumn(int beforeIndex, Column<T, ?> col, Header<?> header, Header<?> footer) {
        super.insertColumn(beforeIndex, col, header, footer);
        headers.add(beforeIndex, header);
    }

    @Override
    public void removeColumn(int index) {
        super.removeColumn(index);
        headers.remove(index);
    }


    private void initialize(final DGCellTableResourceAdapter adapter) {
        sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEMOVE | Event.ONMOUSEOVER);

        setStyleName(adapter.getResources().cellTableStyle().docsCelltableHeader());

        setTableLayoutFixed(true);
        setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
    }

}





