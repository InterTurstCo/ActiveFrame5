package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CmjTree extends VerticalPanel {

    public CmjTree() {
        super();
        drawTree();
    }

    void drawTree() {

        TreeItem all = new TreeItem();
        all.setText("Все");

        TreeItem allItem = new TreeItem(new Label("Все"));
        TreeItem allIitem2 = new TreeItem(new Label("Все"));
        TreeItem allIitem3 = new TreeItem(new Label("Все"));
        all.addItem(allItem);
        all.addItem(allIitem2);
        all.addItem(allIitem3);

        TreeItem notread = new TreeItem();
        notread.setText("Непрочтенные");

        TreeItem notreadItem = new TreeItem(new Label("Непрочтенные"));
        TreeItem notreadIitem2 = new TreeItem(new Label("Непрочтенные"));
        TreeItem notreadIitem3 = new TreeItem(new Label("Непрочтенные"));
        notread.addItem(notreadItem);
        notread.addItem(notreadIitem2);
        notread.addItem(notreadIitem3);

        TreeItem rec = new TreeItem();
        rec.setText("Корзина");

        TreeItem recItem = new TreeItem(new Label("Непрочтенные"));
        TreeItem recIitem2 = new TreeItem(new Label("Непрочтенные"));
        TreeItem recIitem3 = new TreeItem(new Label("Непрочтенные"));
        rec.addItem(recItem);
        rec.addItem(recIitem2);
        rec.addItem(recIitem3);

        Tree mainTree = new Tree();
        mainTree.addItem(all);
        mainTree.addItem(notread);
        mainTree.addItem(rec);

        this.add(mainTree);

    }
}
