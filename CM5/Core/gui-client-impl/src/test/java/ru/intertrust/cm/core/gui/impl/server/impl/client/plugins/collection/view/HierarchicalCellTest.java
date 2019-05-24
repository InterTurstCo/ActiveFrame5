package ru.intertrust.cm.core.gui.impl.server.impl.client.plugins.collection.view;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.junit.Assert;
import org.junit.Test;
import ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.HierarchicalCell;
import ru.intertrust.cm.core.gui.model.plugin.collection.ChildCollectionColumnData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * Created by Myskin Sergey on 21.05.2019.
 */
public class HierarchicalCellTest {

    private final String FIELD_NAME = "field_name";
    private final String STYLE_NAME = "some_style";
    private final String SOME_TEXT = "some_text";
    private final String DRILL_DOWN_STYLE = "drill_down_style";
    private final String SPAN_WITH_EXPAND_ARROW_CLASS = "<span class=\"expand-arrow\">";

    @Test
    public void testBuildTextToInsertMethod() {
        checkWithEmptyChildCollectionViewerConfig();
        checkWithDefinedChildCollectionViewerConfig();
    }

    private void checkWithEmptyChildCollectionViewerConfig() {
        final ChildCollectionColumnData childCollectionColumnData1 = new ChildCollectionColumnData(false, false, 0);
        final ChildCollectionViewerConfig emptyChildCollectionViewerConfig = new ChildCollectionViewerConfig();
        String resultSafeHtml = doRender(childCollectionColumnData1, emptyChildCollectionViewerConfig, "0");
        Assert.assertTrue(resultSafeHtml.contains(SPAN_WITH_EXPAND_ARROW_CLASS));
        Assert.assertTrue(resultSafeHtml.contains(HierarchicalCell.ARROW_SYMBOL));

        final ChildCollectionColumnData childCollectionColumnData2 = new ChildCollectionColumnData(true, false, 0);
        String resultSafeHtml2 = doRender(childCollectionColumnData2, emptyChildCollectionViewerConfig, "0");
        Assert.assertTrue(resultSafeHtml2.contains(SPAN_WITH_EXPAND_ARROW_CLASS));
        Assert.assertTrue(resultSafeHtml2.contains(HierarchicalCell.ARROW_SYMBOL));

        final ChildCollectionColumnData childCollectionColumnData3 = new ChildCollectionColumnData(false, true, 0);
        String resultSafeHtml3 = doRender(childCollectionColumnData3, emptyChildCollectionViewerConfig, "0");
        Assert.assertTrue(resultSafeHtml3.contains(SPAN_WITH_EXPAND_ARROW_CLASS));
        Assert.assertTrue(resultSafeHtml3.contains(HierarchicalCell.ARROW_SYMBOL));

        final ChildCollectionColumnData childCollectionColumnData4 = new ChildCollectionColumnData(true, true, 0);
        String resultSafeHtml4 = doRender(childCollectionColumnData4, emptyChildCollectionViewerConfig, "0");
        Assert.assertTrue(resultSafeHtml4.contains(SPAN_WITH_EXPAND_ARROW_CLASS));
        Assert.assertTrue(resultSafeHtml4.contains(HierarchicalCell.ARROW_SYMBOL));

        final ChildCollectionColumnData childCollectionColumnData5 = new ChildCollectionColumnData(false, false, 0);
        String resultSafeHtml5 = doRender(childCollectionColumnData5, emptyChildCollectionViewerConfig, "");
        Assert.assertTrue(resultSafeHtml5.contains(SPAN_WITH_EXPAND_ARROW_CLASS));
        Assert.assertTrue(resultSafeHtml5.contains(HierarchicalCell.ARROW_SYMBOL));

        final ChildCollectionColumnData childCollectionColumnData6 = new ChildCollectionColumnData(false, false, 10);
        String resultSafeHtml6 = doRender(childCollectionColumnData6, emptyChildCollectionViewerConfig, "");
        Assert.assertTrue(resultSafeHtml6.contains(SPAN_WITH_EXPAND_ARROW_CLASS));
        Assert.assertTrue(resultSafeHtml6.contains(HierarchicalCell.ARROW_SYMBOL));

        final ChildCollectionColumnData childCollectionColumnData7 = new ChildCollectionColumnData(false, false, 10);
        String resultSafeHtml7 = doRender(childCollectionColumnData7, emptyChildCollectionViewerConfig, "0");
        Assert.assertTrue(resultSafeHtml7.contains(SPAN_WITH_EXPAND_ARROW_CLASS));
        Assert.assertTrue(resultSafeHtml7.contains(HierarchicalCell.ARROW_SYMBOL));
    }

    private void checkWithDefinedChildCollectionViewerConfig() {
        // case 1
        final ChildCollectionColumnData childCollectionColumnData = new ChildCollectionColumnData(false, false, 0);
        final ChildCollectionViewerConfig childCollectionViewerConfig = new ChildCollectionViewerConfig();
        childCollectionViewerConfig.setHideArrowIfEmpty(false);
        childCollectionViewerConfig.setShowChildsCount(false);

        String resultSafeHtml = doRender(childCollectionColumnData, childCollectionViewerConfig, "0");
        Assert.assertTrue(resultSafeHtml.contains(SPAN_WITH_EXPAND_ARROW_CLASS));
        Assert.assertTrue(resultSafeHtml.contains(HierarchicalCell.ARROW_SYMBOL));
        Assert.assertTrue(resultSafeHtml.contains("0"));

        // case 2
        final ChildCollectionColumnData childCollectionColumnData2 = new ChildCollectionColumnData(false, false, 0);
        final ChildCollectionViewerConfig childCollectionViewerConfig2 = new ChildCollectionViewerConfig();
        childCollectionViewerConfig2.setHideArrowIfEmpty(false);
        childCollectionViewerConfig2.setShowChildsCount(false);

        String resultSafeHtml2 = doRender(childCollectionColumnData2, childCollectionViewerConfig2, "");
        Assert.assertTrue(resultSafeHtml2.contains(SPAN_WITH_EXPAND_ARROW_CLASS));
        Assert.assertTrue(resultSafeHtml2.contains(HierarchicalCell.ARROW_SYMBOL));
        Assert.assertFalse(resultSafeHtml2.contains("0"));

        // case 3
        final ChildCollectionColumnData childCollectionColumnData3 = new ChildCollectionColumnData(false, false, 0);
        final ChildCollectionViewerConfig childCollectionViewerConfig3 = new ChildCollectionViewerConfig();
        childCollectionViewerConfig3.setHideArrowIfEmpty(false);
        childCollectionViewerConfig3.setShowChildsCount(false);

        String resultSafeHtml3 = doRender(childCollectionColumnData3, childCollectionViewerConfig3, SOME_TEXT);
        Assert.assertTrue(resultSafeHtml3.contains(SPAN_WITH_EXPAND_ARROW_CLASS));
        Assert.assertTrue(resultSafeHtml3.contains(HierarchicalCell.ARROW_SYMBOL));
        Assert.assertFalse(resultSafeHtml3.contains("0"));

        // case 4
        final ChildCollectionColumnData childCollectionColumnData4 = new ChildCollectionColumnData(false, false, 0);
        final ChildCollectionViewerConfig childCollectionViewerConfig4 = new ChildCollectionViewerConfig();
        childCollectionViewerConfig4.setHideArrowIfEmpty(true);
        childCollectionViewerConfig4.setShowChildsCount(false);

        String resultSafeHtml4 = doRender(childCollectionColumnData4, childCollectionViewerConfig4, SOME_TEXT);
        Assert.assertFalse(resultSafeHtml4.contains(SPAN_WITH_EXPAND_ARROW_CLASS));
        Assert.assertFalse(resultSafeHtml4.contains(HierarchicalCell.ARROW_SYMBOL));
        Assert.assertFalse(resultSafeHtml4.contains("0"));

        // case 5
        final ChildCollectionColumnData childCollectionColumnData5 = new ChildCollectionColumnData(false, false, 0);
        final ChildCollectionViewerConfig childCollectionViewerConfig5 = new ChildCollectionViewerConfig();
        childCollectionViewerConfig5.setHideArrowIfEmpty(false);
        childCollectionViewerConfig5.setShowChildsCount(true);

        String resultSafeHtml5 = doRender(childCollectionColumnData5, childCollectionViewerConfig5, SOME_TEXT);
        Assert.assertTrue(resultSafeHtml5.contains(SPAN_WITH_EXPAND_ARROW_CLASS));
        Assert.assertTrue(resultSafeHtml5.contains(HierarchicalCell.ARROW_SYMBOL));
        Assert.assertTrue(resultSafeHtml5.contains("0"));

        // case 6
        final ChildCollectionColumnData childCollectionColumnData6 = new ChildCollectionColumnData(false, false, 0);
        final ChildCollectionViewerConfig childCollectionViewerConfig6 = new ChildCollectionViewerConfig();
        childCollectionViewerConfig6.setHideArrowIfEmpty(true);
        childCollectionViewerConfig6.setShowChildsCount(true);

        String resultSafeHtml6 = doRender(childCollectionColumnData6, childCollectionViewerConfig6, SOME_TEXT);
        Assert.assertFalse(resultSafeHtml6.contains(SPAN_WITH_EXPAND_ARROW_CLASS));
        Assert.assertFalse(resultSafeHtml6.contains(HierarchicalCell.ARROW_SYMBOL));
        Assert.assertTrue(resultSafeHtml6.contains("0"));

        // case 7
        final ChildCollectionColumnData childCollectionColumnData7 = new ChildCollectionColumnData(false, true, 10);
        final ChildCollectionViewerConfig childCollectionViewerConfig7 = new ChildCollectionViewerConfig();
        childCollectionViewerConfig7.setHideArrowIfEmpty(true);
        childCollectionViewerConfig7.setShowChildsCount(true);

        String resultSafeHtml7 = doRender(childCollectionColumnData7, childCollectionViewerConfig7, SOME_TEXT);
        Assert.assertTrue(resultSafeHtml7.contains(SPAN_WITH_EXPAND_ARROW_CLASS));
        Assert.assertTrue(resultSafeHtml7.contains(HierarchicalCell.ARROW_SYMBOL));
        Assert.assertTrue(resultSafeHtml7.contains("10"));
    }

    private String doRender(final ChildCollectionColumnData childCollectionColumnData, final ChildCollectionViewerConfig childCollectionViewerConfig, final String initText) {
        final HierarchicalCell hierarchicalCell = new HierarchicalCell(childCollectionViewerConfig, DRILL_DOWN_STYLE, STYLE_NAME, FIELD_NAME);

        final CollectionRowItem rowItem = new CollectionRowItem();
        rowItem.putChildCollectionColumnData(FIELD_NAME, childCollectionColumnData);

        final Cell.Context context = new Cell.Context(0, 0, rowItem);

        final SafeHtmlBuilder htmlSb = new SafeHtmlBuilder();

        hierarchicalCell.render(context, initText, htmlSb);
        final String resultSafeHtml = htmlSb.toSafeHtml().toString();
        return resultSafeHtml;
    }

}
