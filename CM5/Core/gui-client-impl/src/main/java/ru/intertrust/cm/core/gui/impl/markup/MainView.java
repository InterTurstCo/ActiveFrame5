package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * @author Denis Mitavskiy Date: 24.07.13 Time: 13:52
 */
public class MainView implements EntryPoint {
    @Override
    public void onModuleLoad() {
        // Window.alert("Hello");

        final Mainform mf = new Mainform();

        final Button drugBtn = new Button("dragpanel");
        drugBtn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                mf.showDrag();
            }
        });

        Button stickerBtn = new Button("sticker");
        stickerBtn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                mf.showSticker();
            }
        });

        Button treeBtn = new Button("Tree");
        treeBtn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                mf.showTree();
            }
        });

        mf.addElementInDragPanel(drugBtn);
        // mf.addElementInNavigation(new Button("navigation"));
        // mf.addElementInTree(treeBtn);
        mf.addElementInSticker(stickerBtn);
        RootLayoutPanel rp = RootLayoutPanel.get();
        rp.add(mf);
    }
}
