package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Created by URIY on 21.01.2016.
 */
public class LabledCheckboxCell extends CheckboxCell {
    public LabledCheckboxCell() {
    }

    public LabledCheckboxCell(boolean dependsOnSelection, boolean handlesSelection) {
        super(dependsOnSelection, handlesSelection);
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, Boolean value, NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
        Element labelParent = parent.getFirstChildElement();
        super.onBrowserEvent(context, labelParent, value, event, valueUpdater);
    }

    @Override
    public void render(Context context, Boolean value, SafeHtmlBuilder sb) {

          sb.append(SafeHtmlUtils.fromTrustedString("<label class=\"checkbox\">"));
           super.render(context, value, sb);
          sb.append(SafeHtmlUtils.fromTrustedString("<span></span>"));
          sb.append(SafeHtmlUtils.fromTrustedString("</label>"));

    }
}
