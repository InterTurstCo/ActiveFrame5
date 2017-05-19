package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObject;
import ru.intertrust.cm.core.gui.api.client.Component;
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
                    "John","Alvorado st. 12"), new Contact("Joe","Park Bell 45"),
            new Contact("Michael","San Antonio blv. 3"),
            new Contact("Sarah","New Jercey district 34"), new Contact("George","Palo Alto road 3"));
    private DateTimeFormat dateFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_LONG);


    private ListCellState currentState;

    @Override
    public Component createNew() {
        return new ListCellWidget();
    }

    @Override
    public void setCurrentState(WidgetState currentState) {
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
        buttonPanel.add(new Label(currentState.getHeaderValue() + ((currentState.getCounterRequired()) ? currentState.getItems().size() : "")));

        final ToggleButton openCloseBtn = new ToggleButton(">", "<");
        openCloseBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                Window.alert("Нажал таки...");
                if (openCloseBtn.isDown()) {
                    listPanel.setVisible(true);
                } else {
                    listPanel.setVisible(false);
                }
            }
        });


        ProvidesKey<Contact> keyProvider = new ProvidesKey<Contact>() {
            public Object getKey(Contact item) {
                // Always do a null check.
                return (item == null) ? null : item.id;
            }
        };

        CellList<Contact> cellList = new CellList<Contact>(new RowItemCell(),keyProvider);
        cellList.setRowCount(CONTACTS.size(), true);
        cellList.setRowData(0, CONTACTS);

        // Add a selection model using the same keyProvider.
        SelectionModel<Contact> selectionModel
                = new SingleSelectionModel<Contact>(
                keyProvider);
        cellList.setSelectionModel(selectionModel);

        Contact sarah = CONTACTS.get(3);
        selectionModel.setSelected(sarah, true);
        listPanel.add(cellList);

        buttonPanel.add(openCloseBtn);
        rootPanel.add(buttonPanel);
        rootPanel.add(listPanel);
        return rootPanel;
    }

    private class RowItemCell extends AbstractCell<Contact> {
        @Override
        public void render(Context context, Contact value, SafeHtmlBuilder sb) {
            if (value != null) {
                sb.appendHtmlConstant("<div style=\"size:200%;font-weight:bold;\">");
                sb.appendEscaped(value.name);
                sb.appendHtmlConstant("</div>");

                // Display the address in normal text.
                sb.appendHtmlConstant("<div style=\"padding-left:10px;\">");
                sb.appendEscaped(value.address);
                sb.appendHtmlConstant("</div>");

                // Format that birthday and display it in light gray.
                sb.appendHtmlConstant("<div style=\"padding-left:10px;color:#aaa;\">");
                sb.append(SafeHtmlUtils.fromTrustedString("Born: "));
                sb.appendEscaped(dateFormat.format(value.born));
                sb.appendHtmlConstant("</div>");
            }
        }
    }
}
