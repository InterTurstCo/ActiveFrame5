package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import java.util.List;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractHasData;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;

/**
 * @author Sergey.Okolot
 *         Created on 13.10.2014 14:21.
 */
public class OrganizerHasData<T> extends AbstractHasData<T> {

    public OrganizerHasData(Element elem, int pageSize, ProvidesKey<T> keyProvider) {
        super(elem, pageSize, keyProvider);
    }

    public OrganizerHasData(Widget widget, int pageSize, ProvidesKey<T> keyProvider) {
        super(widget, pageSize, keyProvider);
    }

    @Override
    protected boolean dependsOnSelection() {
        return true;
    }

    @Override
    protected Element getChildContainer() {
        return null;
    }

    @Override
    protected Element getKeyboardSelectedElement() {
        return null;
    }

    @Override
    protected boolean isKeyboardNavigationSuppressed() {
        return false;
    }

    @Override
    protected void renderRowValues(SafeHtmlBuilder sb, List<T> values, int start,
                                   SelectionModel<? super T> selectionModel) throws UnsupportedOperationException {

    }

    @Override
    protected boolean resetFocusOnCell() {
        return false;
    }

    @Override
    protected void setKeyboardSelected(int index, boolean selected, boolean stealFocus) {

    }
}
