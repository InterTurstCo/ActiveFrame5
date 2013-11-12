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

    /*    final ChooseAddresserDialog addresserDialog = new ChooseAddresserDialog();
        addresserDialog.setAnimationEnabled(true);
        Tree addresserTree = new Tree();
        VerticalPanel dialogContent = new VerticalPanel();
        dialogContent.setBorderWidth(1);
        dialogContent.setWidth("100%");
        dialogContent.setHeight("100%");

        addresserDialog.setText("Адресат");


        SimplePanel treeContainer = new SimplePanel();
        final TreeItem rootItem = new TreeItem();
        CheckBoxTreeItemAware rootCheckbox = new CheckBoxTreeItemAware("RootItem", rootItem);

        ValueChangeHandler<Boolean> itemSelectionHandler = new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                CheckBoxTreeItemAware source = (CheckBoxTreeItemAware) event.getSource();
                TreeItem treeItem = source.getTreeItem();
                for (int i = 0; i < treeItem.getChildCount(); i++) {
                    CheckBox widget = (CheckBox) treeItem.getChild(i).getWidget();
                    widget.setValue(event.getValue());
                }
            }
        };
        rootCheckbox.addValueChangeHandler(itemSelectionHandler);
        rootItem.setWidget(rootCheckbox);

        for (int i = 0; i < 3; i++) {
            TreeItem adresser = new TreeItem();
            CheckBoxTreeItemAware adresserCheckbox = new CheckBoxTreeItemAware("adresser" + i, adresser);
            adresserCheckbox.addValueChangeHandler(itemSelectionHandler);
            adresser.setWidget(adresserCheckbox);
            rootItem.addItem(adresser);
        }

        addresserTree.addItem(rootItem);
        addresserDialog.setTitle("title");

        HorizontalPanel buttons = new HorizontalPanel();
        buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        Button expandAllButton = new Button("Развернуть все");
        expandAllButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                changeTreeState(rootItem, true);
            }
        });
        buttons.add(expandAllButton);
        Button collapseAllButton = new Button("Свернуть все");
        collapseAllButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                changeTreeState(rootItem, false);
            }
        });
        buttons.add(collapseAllButton);
        dialogContent.add(buttons);

        treeContainer.add(addresserTree);
        dialogContent.add(new ScrollPanel(treeContainer));
        Button closeButton = new Button();
        closeButton.setText("Отмена");

        HorizontalPanel bottomButtons = new HorizontalPanel();
        bottomButtons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        bottomButtons.add(new Button("Добавить адресата"));
        bottomButtons.add(new Button("Добавить"));
        bottomButtons.add(closeButton);

        dialogContent.add(bottomButtons);

        addresserDialog.setWidget(dialogContent);
        addresserDialog.setGlassEnabled(true);
        addresserDialog.setHeight("300px");
        addresserDialog.setWidth("400px");

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addresserDialog.hide();
            }
        });
        addresserDialog.center();
    }

    private void changeTreeState(TreeItem tree, boolean state) {
        tree.setState(state, false);
        for (int i = 0; i < tree.getChildCount(); i++) {
            TreeItem child = tree.getChild(i);
            child.setState(false, false);
        }
    }

    class ChooseAddresserDialog extends DialogBox {


    }

    class CheckBoxTreeItemAware extends CheckBox {
        TreeItem treeItem;

        CheckBoxTreeItemAware(String label, TreeItem treeItem) {
            super(label);
            this.treeItem = treeItem;
        }

        TreeItem getTreeItem() {
            return treeItem;
        }
    }    */
}
