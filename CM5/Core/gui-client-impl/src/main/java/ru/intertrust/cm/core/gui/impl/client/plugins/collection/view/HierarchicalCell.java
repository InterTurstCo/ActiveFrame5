package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig;
import ru.intertrust.cm.core.gui.model.plugin.collection.ChildCollectionColumnData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.util.StringUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Lesia Puhova
 * Date: 14/02/14
 * Time: 12:05 PM
 */
public class HierarchicalCell extends AbstractTextCell {

    public static final String ARROW_SYMBOL = "►";

    private final ChildCollectionViewerConfig childCollectionViewerConfig;
    private String drillDownStyle;

    public HierarchicalCell(ChildCollectionViewerConfig childCollectionViewerConfig, String style, String drillDownStyle, String field) {
        super(style, field);
        this.drillDownStyle = drillDownStyle;
        this.childCollectionViewerConfig = childCollectionViewerConfig;
    }

    @Override
    public void render(Context context, String text, SafeHtmlBuilder sb) {
        CollectionRowItem collectionRowItem = (CollectionRowItem) context.getKey();

        final boolean hideArrowIfEmpty = childCollectionViewerConfig.getHideArrowIfEmpty();

        final Map<String, ChildCollectionColumnData> childCollectionColumnFieldData = collectionRowItem.getChildCollectionColumnFieldDataMap();
        // достаем данные по конкретному полю (колонке), потому что их может быть больше одной
        final ChildCollectionColumnData childCollectionColumnData = childCollectionColumnFieldData.get(field);

        final boolean hasCollectionAnyChild = childCollectionColumnData.isHasCollectionAnyChild();

        // показываем стрелку для перехода к дочерней коллекции только если ее не надо скрывать, либо надо при условии отсутствия дочерних элементов
        final boolean showArrow = (!hideArrowIfEmpty || hasCollectionAnyChild);
        String textToInsert = buildTextToInsert(text, childCollectionColumnData);

        doRender(sb, textToInsert, showArrow);
    }

    /**
     * Производит рендер ячейки иерархической коллекции в зависимости от параметров.
     *
     * @param sb           объект билдера результирующего HTML
     * @param textToInsert текст для вставки в ячейку
     * @param showArrow    флаг показа стрелки перехода к дочерней коллекции в структуре иерархии
     */
    private void doRender(SafeHtmlBuilder sb, String textToInsert, boolean showArrow) {
        if ("combined-link".equals(drillDownStyle)) {
            sb.append(SafeHtmlUtils.fromTrustedString("<div class=\"hierarchical-column\" " + style + "/>"));
            if (showArrow) {
                sb.append(SafeHtmlUtils.fromTrustedString("<span class=\"expand-arrow\">"));
            }
            sb.append(SafeHtmlUtils.fromString(textToInsert));
            if (showArrow) {
                sb.append(SafeHtmlUtils.fromTrustedString(" " + ARROW_SYMBOL + "</span>"));
            }
            sb.append(SafeHtmlUtils.fromTrustedString("</div>"));
        } else {
            sb.append(SafeHtmlUtils.fromTrustedString("<div class=\"hierarchical-column\" " + style + "/>"));
            sb.append(SafeHtmlUtils.fromString(textToInsert));
            if (showArrow) {
                sb.append(SafeHtmlUtils.fromTrustedString(" <span class=\"expand-arrow\">" + ARROW_SYMBOL + "</span>"));
            }
            sb.append(SafeHtmlUtils.fromTrustedString("</div>"));
        }
    }

    /**
     * Генерирует текст для вставки в зависимости от параметров и условий.
     *
     * @param initText                  изначальный текст для вставки.
     * @param childCollectionColumnData объект параметров дочерней коллекции
     * @return итоговый текст для вставки
     */
    private String buildTextToInsert(String initText, ChildCollectionColumnData childCollectionColumnData) {

        final boolean hasChildsCountAlreadyCalculated = childCollectionColumnData.isHasChildsCountAlreadyCalculated();
        final boolean showChildsCount = childCollectionViewerConfig.getShowChildsCount();
        final int childCollectionItemsCount = childCollectionColumnData.getChildCollectionItemsCount();

        String textToInsert = initText.trim();
        // обрабатываем в случае, если количество не было уже заранее посчитано в коллекции (отдельная колонка с count), но его нужно показать
        if (!hasChildsCountAlreadyCalculated && showChildsCount) {

            if (!StringUtil.isNullOrBlank(textToInsert)) {
                // если в колонке уже был какой-то текст - оставляем его как есть и после двоеточия выводим количество
                textToInsert += " : " + String.valueOf(childCollectionItemsCount);
            } else {
                // если текста не было, то просто вставляем цифру с количеством
                textToInsert = String.valueOf(childCollectionItemsCount);
            }
        }
        return textToInsert;
    }

    @Override
    public Set<String> getConsumedEvents() {
        HashSet<String> events = new HashSet<String>();
        events.add(BrowserEvents.CLICK);
        return events;
    }

}