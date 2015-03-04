package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.CLEAR_FORM_KEY;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.FIND_BUTTON_KEY;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CLEAR_FORM;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.FIND_BUTTON;


/**
 * Created by tbilyi on 14.02.14.
 */
public class NewExtSearchPanelHelper implements IsWidget {
    private AbsolutePanel sdfWrapper;
    private AbsolutePanel sdfTopBlock;
    private AbsolutePanel sdfMiddleBlock;
    private AbsolutePanel sdfFooterBlock;

    private AbsolutePanel sdfKeyWords;

    private AbsolutePanel sdfLanguage;

    private AbsolutePanel sdfTitle;
    private AbsolutePanel sdfPublisher;
    private AbsolutePanel sdfDocNumber;
    private AbsolutePanel sdfDocView;
    private AbsolutePanel sdfCountries;
    private AbsolutePanel sdfAuthors;
    private AbsolutePanel sdfDocType;
    private AbsolutePanel sdfUnitOwner;
    private AbsolutePanel sdfPublicationDate;

    private AbsolutePanel sdfEntryDate;

    public NewExtSearchPanelHelper() {
        sdfWrapper = new AbsolutePanel();
        sdfWrapper.setStyleName("sdf-wrapper");

        sdfTopBlock = new AbsolutePanel();
        sdfTopBlock.setStyleName("sdf-top-block");

        sdfTopBlock.add(new HTML("<span class=\"sdf-simple-search\" title=\"Переход на простой Поиск\"><a href=\"#\">Простой поиск</a></span>\n" +
                "            <span class=\"sdf-top-line\">|</span>\n" +
                "            <span class=\"sdf-help\" title=\"Помощь\"><a href=\"#\">Помощь</a></span>\n" +
                "            <span class=\"sdf-top-line\">|</span>\n" +
                "            <span class=\"sdf-close\" title=\"Закрыть\"><a href=\"#\">Закрыть</a></span>"));


        sdfMiddleBlock = new AbsolutePanel();
        sdfMiddleBlock.setStyleName("sdf-middle-block");


        sdfFooterBlock = new AbsolutePanel();
        sdfFooterBlock.setStyleName("sdf-footer-block");

        FocusPanel sdfButtonClearForm = new FocusPanel();

        sdfButtonClearForm.getElement().setClassName("sdf-button-clear-form");
        sdfButtonClearForm.add(new HTML("<span>" + LocalizeUtil.get(CLEAR_FORM_KEY, CLEAR_FORM) + "</span>"));

        FocusPanel sdfEnterSearch = new FocusPanel();
        sdfEnterSearch.getElement().setClassName("sdf-enter-search");
        sdfEnterSearch.add(new HTML("<span>" + LocalizeUtil.get(FIND_BUTTON_KEY, FIND_BUTTON) + "</span>"));

        sdfFooterBlock.add(sdfButtonClearForm);
        sdfFooterBlock.add(sdfEnterSearch);


        sdfWrapper.add(sdfTopBlock);
        sdfWrapper.add(sdfMiddleBlock);
        sdfWrapper.add(sdfFooterBlock);

        sdfKeyWords = new AbsolutePanel();
        sdfKeyWords.setStyleName("top");

        sdfLanguage = new AbsolutePanel();
        sdfLanguage.setStyleName("middle");
        sdfTitle = new AbsolutePanel();
        sdfTitle.setStyleName("middle");
        sdfPublisher = new AbsolutePanel();
        sdfPublisher.setStyleName("middle");
        sdfDocNumber = new AbsolutePanel();
        sdfDocNumber.setStyleName("middle");
        sdfDocView = new AbsolutePanel();
        sdfDocView.setStyleName("middle");
        sdfCountries = new AbsolutePanel();
        sdfCountries.setStyleName("middle");
        sdfAuthors = new AbsolutePanel();
        sdfAuthors.setStyleName("middle");
        sdfDocType = new AbsolutePanel();
        sdfDocType.setStyleName("middle");
        sdfUnitOwner = new AbsolutePanel();
        sdfUnitOwner.setStyleName("middle");
        sdfPublicationDate = new AbsolutePanel();
        sdfPublicationDate.setStyleName("middle");


        sdfEntryDate = new AbsolutePanel();
        sdfEntryDate.setStyleName("bottom");

        //область добавления в центральный блок
        sdfMiddleBlock.add(sdfKeyWords);
        sdfMiddleBlock.add(sdfLanguage);
        sdfMiddleBlock.add(sdfTitle);
        sdfMiddleBlock.add(sdfPublisher);
        sdfMiddleBlock.add(sdfDocNumber);
        sdfMiddleBlock.add(sdfDocView);
        sdfMiddleBlock.add(sdfCountries);
        sdfMiddleBlock.add(sdfAuthors);
        sdfMiddleBlock.add(sdfDocType);
        sdfMiddleBlock.add(sdfUnitOwner);
        sdfMiddleBlock.add(sdfPublicationDate);

        sdfMiddleBlock.add(sdfEntryDate);

    }

    @Override
    public Widget asWidget() {
        return sdfWrapper;
    }
}
