package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;

import ru.intertrust.cm.core.gui.model.counters.CounterKey;

import java.util.Map;

/**
 * Created by andrey on 19.03.14.
 */
public class TreeItemCounterDecorator implements CounterDecorator {

    private TreeItem treeItem;
    private CounterKey counterKey;

    public TreeItemCounterDecorator(TreeItem treeItem) {
        this.treeItem = treeItem;
    }

    @Override
    public void decorate(Long counterValue) {

        Label label = new Label();
        label.setStyleName("tree-label");
        label.removeStyleName("gwt-Label");
        Map userObjects = (Map) treeItem.getUserObject();
        Object originalText = userObjects.get("originalText");
        String text;
        if (counterValue == null || counterValue == 0) {
            text = originalText.toString();
        } else {
            text = originalText + " " + counterValue;
        }
        label.setText(text);
        treeItem.setWidget(label);
    }

    public void setCounterKey(CounterKey counterKey) {
        this.counterKey = counterKey;
    }

    public CounterKey getCounterKey() {
        return counterKey;
    }
}
