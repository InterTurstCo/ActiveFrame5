package ru.intertrust.cm.core.gui.impl.client.crypto;


import com.google.gwt.cell.client.DateCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import ru.intertrust.cm.core.business.api.dto.crypto.DocumentVerifyResult;
import ru.intertrust.cm.core.business.api.dto.crypto.SignerInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VerifySignatureDialog extends DialogBox {
    private CellTable<SignInfo> table;
    private List<DocumentVerifyResult> documentVerifyResults;
    private VerticalPanel panel;
    private VerticalPanel tablePanel;
    private Label label;
    private Image progressBar;

    public VerifySignatureDialog() {
        this.documentVerifyResults = documentVerifyResults;

        setText("Проверка подписи");

        // Enable animation.
        setAnimationEnabled(true);

        // Enable glass background.
        //setGlassEnabled(true);

        removeStyleName("gwt-DialogBox ");
        addStyleName("popup-body");
        addStyleName("verifySignaturePopup");

        // DialogBox is a SimplePanel, so you have to set its widget 
        // property to whatever you want its contents to be.
        Button closeButton = new Button("Закрыть");
        closeButton.removeStyleName("gwt-Button");
        closeButton.addStyleName("lightButton");
        closeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                VerifySignatureDialog.this.hide();
            }
        });

        panel = new VerticalPanel();
        panel.setHeight("200pt");
        panel.setWidth("500pt");
        panel.setSpacing(10);
        panel.setSpacing(10);
        panel.setStyleName("grey-background");
        panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        buttonPanel.add(closeButton);

        tablePanel = new VerticalPanel();
        tablePanel.setHeight("200pt");
        tablePanel.setWidth("500pt");
        tablePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        
        panel.add(tablePanel);
        
        label = new Label("Выполняется проверка подписи");
        progressBar = new Image();
        progressBar.setUrl("CMJSpinner.gif");
        tablePanel.add(label);
        tablePanel.add(progressBar);
        
        panel.add(buttonPanel);

        setWidget(panel);

        center();
    }

    public void setDocumentVerifyResults(List<DocumentVerifyResult> documentVerifyResults) {

        table = new CellTable<SignInfo>();
        table.setWidth("500pt");

        // Add a text column to show the name.
        TextColumn<SignInfo> nameColumn =
                new TextColumn<SignInfo>() {
                    @Override
                    public String getValue(SignInfo object) {
                        return object.name;
                    }
                };
        table.addColumn(nameColumn, "Название");

        // Add a valid column.
        TextColumn<SignInfo> validColumn = new TextColumn<SignInfo>() {
            @Override
            public String getValue(SignInfo object) {
                return object.valid;
            }
        };
        table.addColumn(validColumn, "Валидность");

        
        // Add a date column to show the birthday.
        DateCell dateCell = new DateCell();
        Column<SignInfo, Date> dateColumn = new Column<SignInfo, Date>(dateCell) {
            @Override
            public Date getValue(SignInfo object) {
                return object.signDate;
            }
        };
        table.addColumn(dateColumn, "Дата");

        // Add a text column to show the signer.
        TextColumn<SignInfo> signerColumn = new TextColumn<SignInfo>() {
            @Override
            public String getValue(SignInfo object) {
                return object.signer;
            }
        };
        table.addColumn(signerColumn, "Подписант");

        List<SignInfo> data = new ArrayList<SignInfo>();
        for (DocumentVerifyResult documentVerifyResult : documentVerifyResults) {
            for (SignerInfo signerInfo : documentVerifyResult.getSignerInfos()) {
                data.add(new SignInfo(documentVerifyResult.getDocumentName(), signerInfo));
            }
        }

        // Push the data into the widget.
        table.setRowData(data);
        
        tablePanel.remove(label);
        tablePanel.remove(progressBar);
        tablePanel.insert(table, 0);

    }

    class SignInfo {
        String name;
        Date signDate;
        String signer;
        String valid;

        SignInfo(String name, SignerInfo signerInfo) {
            this.name = name;
            this.signDate = signerInfo.getSignDate();
            this.signer = signerInfo.getName();
            this.valid = signerInfo.isValid() ? "Действительна" : "Не действительна";
        }
    }

}
