package ru.intertrust.cm.core.gui.impl.client.panel;


import com.google.gwt.user.client.ui.*;

/**
 * Entry point classes define <code>createHeader()</code>
 */
public class HeaderContainer {

    public static final int FIRST_ROW = 0;

    /**
     * This is the entry point method.
     */
    public SimplePanel createHeader() {

        MainContainer mainContainer = new MainContainer().build();

        FlexTable headTable = new FlexTable();
        headTable.addStyleName("HeadTable");

        headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 0, "H_td_logo");

        headTable.setWidget(FIRST_ROW, 0, new Image("logo.gif"));

        //suggest box
        MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();

        oracle.add("Cat");
        oracle.add("Cat");
        oracle.add("Cat");
        oracle.add("Dog");
        oracle.add("Dog");
        oracle.add("Dog");
        oracle.add("Dog");

        SuggestBox suggestBox = new SuggestBox(oracle);
        suggestBox.setWidth("100%");

        headTable.setWidget(FIRST_ROW, 1, suggestBox);
        headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 1, "H_td_notes");

        InlineLabel userName = new InlineLabel("Sergey Borisov");
        userName.addStyleName("HeadUserName");
        InlineLabel userPosition = new InlineLabel("Head of department");
        userPosition.addStyleName("HeadUserPost");

        FlowPanel userInfoPanel = new FlowPanel();
        userInfoPanel.add(new SimplePanel(userName));
        userInfoPanel.add(new SimplePanel(userPosition));

        headTable.setWidget(FIRST_ROW, 2, userInfoPanel);

        headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 2, "H_td_user");

        FlowPanel linksPanel = new FlowPanel();
        Anchor settings = new Anchor("Settings", "Settings");
        //settings.removeStyleName("gwt-Anchor");
        linksPanel.add(new SimplePanel(settings));
        Anchor help = new Anchor("Help", "Help");
        //help.removeStyleName("gwt-Anchor");
        linksPanel.add(new SimplePanel(help));

        headTable.setWidget(FIRST_ROW,3,linksPanel);
        headTable.getCellFormatter().setStyleName(FIRST_ROW,3,"H_td_links");

        headTable.setWidget(FIRST_ROW,4,new Hyperlink("Exit","Exit"));
        headTable.getCellFormatter().setStyleName(FIRST_ROW,4,"H_td_logout");

        mainContainer.getHead().add(headTable);
        return mainContainer.getContainer();
    }


    private class MainContainer {
        private SimplePanel container;
        private SimplePanel head;

        public SimplePanel getContainer() {
            return container;
        }

        public SimplePanel getHead() {
            return head;
        }

        public MainContainer build() {
            container = new SimplePanel();
            container.getElement().setId("container");
            container.getElement().getStyle().setProperty("position","relative");
            head = new SimplePanel();
            container.add(head);
            head.getElement().setId("Head");
            return this;
        }
    }
}
