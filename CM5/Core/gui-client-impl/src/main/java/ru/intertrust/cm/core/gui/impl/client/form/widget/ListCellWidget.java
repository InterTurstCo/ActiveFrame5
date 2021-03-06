package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.ListCellRenderFactory;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.ListBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.ListCellState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Created by Ravil on 18.05.2017.
 */
@ComponentName("list-cell")
public class ListCellWidget extends BaseWidget {


    private static class Contact {
        private static int nextId = 0;
        private final int id;
        private String name;
        private String address;
        private Date born;

        public Contact(String name, String address) {
            nextId++;
            this.id = nextId;
            this.name = name;
            this.address = address;
            born = new Date();
        }
    }

    private static final List<Contact> CONTACTS = Arrays.asList(new Contact(
                    "John", "Alvorado st. 12"), new Contact("Joe", "Park Bell 45"),
            new Contact("Michael", "San Antonio blv. 3"),
            new Contact("Sarah", "New Jercey district 34"), new Contact("George", "Palo Alto road 3"));
    private DateTimeFormat dateFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_LONG);


    private ListCellState currentState;

    @Override
    public Component createNew() {
        return new ListCellWidget();
    }

    @Override
    public void setValue(Object value) {
        //TODO: Implementation required
    }

    @Override
    public void disable(Boolean isDisabled) {
        //TODO: Implementation required
    }

    @Override
    public void reset() {
        //TODO: Implementation required
    }

    @Override
    public void applyFilter(String value) {
        //TODO: Implementation required
    }

    @Override
    public Object getValueTextRepresentation() {
        return getValue();
    }

    @Override
    public void setCurrentState(WidgetState currentState) {
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    protected boolean isChanged() {
        return false;
    }

    @Override
    protected WidgetState createNewState() {
        return new ListBoxState();
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        return asNonEditableWidget(state);
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        currentState = (ListCellState) state;
        FlowPanel rootPanel = new FlowPanel();
        FlowPanel buttonPanel = new FlowPanel();
        final FlowPanel listPanel = new FlowPanel();


        ListCellRenderFactory renderer = ComponentRegistry.instance.get(((ListCellState) state).getRenderComponentName());

        rootPanel.addStyleName("rootPanelListCellWidget");
        buttonPanel.addStyleName("btnPanelListCellWidget");
        listPanel.addStyleName("listPanelListCellWidget");
        buttonPanel.add(new Label(currentState.getHeaderValue() + ((currentState.getCounterRequired()) ? " [" + currentState.getItems().size() + "]" : "")));
        listPanel.setVisible(false);

        final ToggleButton openCloseBtn = new ToggleButton();
        openCloseBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                if (openCloseBtn.isDown()) {
                    listPanel.setVisible(true);
                } else {
                    listPanel.setVisible(false);
                }
            }
        });

        if(renderer!=null) {
            ProvidesKey<CollectionRowItem> keyProvider = new ProvidesKey<CollectionRowItem>() {
                public Object getKey(CollectionRowItem item) {
                    // Always do a null check.
                    if (item == null){
                        return null;
                    }else{
                        RdbmsId rId = new RdbmsId(item.getId());
                        return rId.getId();
                    }
                }
            };

            CellList<CollectionRowItem> cellList = new CellList<CollectionRowItem>(renderer.getCellRendererInstance(currentState.getRenderComponentType()),
                    keyProvider);


            cellList.setRowCount(currentState.getItems().size(), true);
            cellList.setRowData(0, currentState.getItems());


            SelectionModel<CollectionRowItem> selectionModel
                    = new SingleSelectionModel<CollectionRowItem>(
                    keyProvider);
            cellList.setSelectionModel(selectionModel);


            listPanel.add(cellList);
        } else {
            Window.alert("Не найден компонент рендеринга: "+currentState.getRenderComponentName());
        }
        buttonPanel.add(openCloseBtn);
        rootPanel.add(buttonPanel);
        rootPanel.add(listPanel);
        return rootPanel;
    }


}
