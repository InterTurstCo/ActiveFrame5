package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class CmjPreHeader extends DockLayoutPanel {

    public CmjPreHeader() {
        super(Unit.EM);
        addContentPreHeader();

    }

    void addContentPreHeader() {

        Image logo = new Image("images/cm-logo.png");
        logo.getElement().getStyle().setProperty("marginRight", "30px");

        ListBox search = new ListBox();
        search.addItem("Подписан проект Исходящий документ для шаблона + Локер от  Преображенская  М.М. ");
        search.addItem("Подписан проект Исходящий документ для шаблона + Локер от  Преображенская  М.М. ");
        search.addItem("Подписан проект Исходящий документ для шаблона + Локер от  Преображенская  М.М. ");
        search.addItem("Подписан проект Исходящий документ для шаблона + Локер от  Преображенская  М.М. ");
        search.addItem("Подписан проект Исходящий документ для шаблона + Локер от  Преображенская  М.М. ");
        search.setVisibleItemCount(1);

        Image lupa = new Image("css/i/ext-search.png");
        lupa.getElement().getStyle().setProperty("marginRight", "30px");

        Label profileUserLink = new Label("User");
        Image imageUserLink = new Image("images/user.png");
        imageUserLink.getElement().getStyle().setProperty("marginRight", "30px");

        Image settings = new Image("css/images/settings.png");

        Image help = new Image("css/images/help.png");
        help.getElement().getStyle().setProperty("marginRight", "30px");

        Label exit = new Label("Выход");

        this.addWest(logo, 15);
        this.addEast(exit, 4);

        this.addEast(help, 2);
        this.addEast(settings, 2);

        this.addEast(imageUserLink, 2);
        this.addEast(profileUserLink, 2);

        this.addEast(lupa, 2);
        this.add(search);
        this.setHeight("34px");
    }

}
