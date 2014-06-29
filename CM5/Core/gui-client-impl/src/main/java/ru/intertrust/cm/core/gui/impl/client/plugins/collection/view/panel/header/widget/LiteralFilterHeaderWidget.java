package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget;

import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.util.HeaderWidgetUtil;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.HEADER_CLEAR_BUTTON_ID_PART;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.06.2014
 *         Time: 21:31
 */
public class LiteralFilterHeaderWidget extends FilterHeaderWidget {
    private static final String VALUE_SEPARATOR = ", ";
    public LiteralFilterHeaderWidget(CollectionColumn column, CollectionColumnProperties columnProperties,
                                     List<String> initialFilterValues) {
        super(column, columnProperties, initialFilterValues, VALUE_SEPARATOR);
    }

    public void init() {

        StringBuilder htmlBuilder = new StringBuilder(getTitleStyle());

        htmlBuilder.append(" <div class=\"search-container\"");
        htmlBuilder.append(getSearchContainerStyle());
        htmlBuilder.append("id = ");
        htmlBuilder.append(id);

        htmlBuilder.append("><input type=\"text\" class=\"search-box\" id= ");
        htmlBuilder.append(id);
        htmlBuilder.append("input ");
        htmlBuilder.append(getSearchInputStyle());
        htmlBuilder.append("<div type=\"button\" id= ");
        htmlBuilder.append(id);
        htmlBuilder.append(HEADER_CLEAR_BUTTON_ID_PART);
        String clearButtonClass = filterValuesRepresentation.isEmpty() ? " class=\"search-box-clear-button-off\"></div></div>"
                : " class=\"search-box-clear-button-on\"></div></div>";
        htmlBuilder.append(clearButtonClass);
        html = htmlBuilder.toString();

    }

    @Override
    public List<String> getFilterValues() {
        return HeaderWidgetUtil.getFilterValues(VALUE_SEPARATOR, filterValuesRepresentation);
    }
}
