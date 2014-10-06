package ru.intertrust.cm.core.gui.impl.client.util;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import ru.intertrust.cm.core.config.gui.form.title.AbstractTitleRepresentationConfig;
import ru.intertrust.cm.core.config.gui.form.title.TitleConfig;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.09.2014
 *         Time: 10:41
 */
public class GuiUtil {
    public static boolean isChildClicked(ClickEvent event, String id) {
        NodeList<Element> checkBoxes = Document.get().getElementById(id).getElementsByTagName("input");
        Element target = Element.as(event.getNativeEvent().getEventTarget());
        for (int i = 0; i < checkBoxes.getLength(); i++) {
            if (checkBoxes.getItem(i).isOrHasChild(target)) {
                return true;
            }
        }
        NodeList<Element> links = Document.get().getElementById(id).getElementsByTagName("div");
        for (int i = 0; i < links.getLength(); i++) {
            if (links.getItem(i).isOrHasChild(target)) {
                return true;
            }
        }
        return false;
    }

    public static String getConfiguredTitle(FormPlugin formPlugin, boolean isNewObjectForm){
        FormPluginData formPluginData = formPlugin.getInitialData();
        FormDisplayData displayData = formPluginData.getFormDisplayData();
        TitleConfig titleConfig = displayData.getMarkup().getTitle();
        if(titleConfig == null){
            return BusinessUniverseConstants.EMPTY_VALUE;
        }
        AbstractTitleRepresentationConfig config = isNewObjectForm ? titleConfig.getNewObjectConfig()
                : titleConfig.getExistingObjectConfig();
        LabelState labelState = (LabelState) displayData.getFormState().getWidgetState(config.getLabelWidgetId());
        return labelState.getLabel();

    }
}
