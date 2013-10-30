package ru.intertrust.cm.core.gui.impl.markup.DialogBoxWidget;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 24.10.13
 * Time: 13:52
 * To change this template use File | Settings | File Templates.
 */
public class DialogBoxForm {

    VerticalPanel container = new VerticalPanel();
    HorizontalPanel horizontLine = new HorizontalPanel();
    VerticalPanel vpResult = new VerticalPanel();
    TextBox txtB = new TextBox();
    Button openDialog = new Button("add Query");
    DialogBox dBox = new DialogBox();
    CellTable table = new CellTable();
    Button dBoxOk = new Button("OK");
    Button dBoxCansel = new Button("cancel");
    ArrayList<Entity> list = new ArrayList<Entity>();
    HorizontalPanel dBoxHorPanel = new HorizontalPanel();
    FlowPanel dialogBoxContent = new FlowPanel();




    public DialogBoxForm(){
        createTable();
        createQueryList();
        createPanel();
        openDialogListener();
        createDialogContent();

    }

    private void createQueryList(){
        list.add(new Entity(false, "Kiev", "3000000"));
        list.add(new Entity(false, "Odessa", "200000"));
        list.add(new Entity(false, "NY", "10000000"));
        list.add(new Entity(false, "York", "1000000"));

        table.setRowCount(list.size());
        table.setRowData(0, list);
    }


    public void createPanel(){
        container.add(horizontLine);
        horizontLine.add(txtB);
        horizontLine.add(openDialog);
        container.add(vpResult);

    }

    private void createDialogContent(){
        dialogBoxContent.add(table);
        dialogBoxContent.add(dBoxHorPanel);
        dBoxHorPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        dBoxHorPanel.add(dBoxOk);
        dBoxHorPanel.add(dBoxCansel);
        dBox.add(dialogBoxContent);

    }

    public void openDialogListener(){

        openDialog.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dBox.setText("DIALOG BOX");
                dBox.center();
                dBox.show();
            }
        });

        dBoxOk.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getSelectedRow();
                dBox.hide();
            }
        });

        dBoxCansel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dBox.hide();
            }
        });
    }

    private void getSelectedRow(){
        vpResult.clear();

        for (int i =0; i < list.size(); i++){

            if (list.get(i).isaBoolean()){
              final Button moveUp = new Button("up");
              final Button moveDown = new Button("down");
              final Button delete = new Button("delete");
              final HorizontalPanel hor = new HorizontalPanel();

                hor.add(new Label(list.get(i).getString()+" "+list.get(i).getPop()));
                hor.getWidget(0).setWidth("200px");
                hor.add(moveUp);
                hor.add(moveDown);
                hor.add(delete);
                vpResult.add(hor);

                delete.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        delete.getElement().getParentElement().getParentElement().removeFromParent();
                    }
                });

                moveUp.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (vpResult.getWidgetIndex(hor)>0){
                            int index = vpResult.getWidgetIndex(hor);
                            vpResult.remove(index);
                            vpResult.insert(hor, index-1);
                        }
                    }
                });

                moveDown.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (vpResult.getWidgetIndex(hor)< vpResult.getWidgetCount()-1){
                            int index = vpResult.getWidgetIndex(hor);
                            vpResult.remove(index);
                            vpResult.insert(hor, index+1);

                        }
                    }
                });
            }
        }

    }

    private void createTable(){
        Column<Entity, Boolean> checkColumn = new Column<Entity, Boolean>(
                new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(Entity object) {
                return object.isaBoolean();
            }
        };

        checkColumn.setFieldUpdater(new FieldUpdater<Entity, Boolean>() {
            @Override
            public void update(int index, Entity object, Boolean value) {
                object.setaBoolean(value);
            }
        });

        Column<Entity, String> stringColumn = new Column<Entity, String>(
                new TextCell()) {
            @Override
            public String getValue(Entity object) {
                return object.getString();
            }
        };

        Column<Entity, String> intColumn = new Column<Entity, String>(
                new TextCell()) {
            @Override
            public String getValue(Entity object) {
                   return object.getPop();
            }
        };

        table.addColumn(checkColumn, "use");
        table.addColumn(stringColumn, "City");
        table.addColumn(intColumn, "Population");
    }

    public VerticalPanel getContainer() {
        return container;
    }
}


