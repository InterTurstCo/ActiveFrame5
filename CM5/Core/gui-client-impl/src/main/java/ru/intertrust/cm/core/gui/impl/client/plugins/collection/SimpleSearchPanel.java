package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.event.SimpleSearchEvent;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 26.12.13
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */
public class SimpleSearchPanel extends Composite implements IsWidget {

    private FlowPanel simpleSearchContainer;
    private FlowPanel textPanel = new FlowPanel();
    private HorizontalPanel panel = new HorizontalPanel();
    private Button searchButton = new Button();
    private Button clearButton = new Button();
    private TextBox textBox = new TextBox();
    private EventBus eventBus;


    public SimpleSearchPanel(FlowPanel simpleSearchContainer, EventBus eventBus) {
       this.simpleSearchContainer = simpleSearchContainer;
       this.eventBus = eventBus;
       initWidget(simpleSearchContainer);
       init();
    }

    private void init(){
        textPanel.add(textBox);
        textBox.getElement().setAttribute("placeHolder", LocalizeUtil.get(BusinessUniverseConstants.SEARCH));
        textPanel.add(searchButton);
        textPanel.addStyleName("search-panel-text-and-button-container");
        searchButton.addStyleName("simple-search-button");
        panel.add(textPanel);
        panel.add(clearButton);
        clearButton.addStyleName("flush-button");
        textBox.addStyleName("search-field");
        textBox.removeStyleName("gwt-TextBox");
        clearButton.removeStyleName("gwt-Button");
        searchButton.removeStyleName("gwt-Button");
        simpleSearchContainer.add(panel);

        simpleSearchContainer.addStyleName("simple-search-container");

        createEvent();
    }

    public void createEvent(){

        clearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                textBox.setText("");
                eventBus.fireEvent(new SimpleSearchEvent("", false));

            }
        });

        searchButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                eventBus.fireEvent(new SimpleSearchEvent(textBox.getText(), true));

            }
        });

        textBox.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {

                    eventBus.fireEvent(new SimpleSearchEvent(textBox.getText(), true));

                }
            }
        });


    }

    public FlowPanel getSimpleSearchContainer() {
        return simpleSearchContainer;
    }

    public void setSimpleSearchContainer(FlowPanel simpleSearchContainer) {
        this.simpleSearchContainer = simpleSearchContainer;
    }
}
