package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
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
        label.removeStyleName("gwt-Label");
        Map userObjects = (Map) treeItem.getUserObject();
        Object originalText = userObjects.get("originalText");
        Panel container = new AbsolutePanel();
        container.setStyleName("tree-label");
        container.addStyleName("treeItemSelectionDecorator");
        container.getElement().getStyle().clearOverflow();
        String text = originalText.toString();
        label.setText(text);
        label.setStyleName("treeItemTitle");
        container.add(label);
		Label counterLabel = new Label("");
		counterLabel.setStyleName("treeItemCounter");
		container.add(counterLabel);
        if (counterValue != null && counterValue != 0) {
            counterLabel.setText(counterValue.toString());
        
        }

        treeItem.setWidget(container);
    }

    public void setCounterKey(CounterKey counterKey) {
        this.counterKey = counterKey;
    }

    public CounterKey getCounterKey() {
        return counterKey;
    }
}
