package ru.intertrust.cm.core.gui.impl.client.form.widget.confirm;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;

/**
 * @author Yaroslav Bondacrhuk
 * Date: 09.08.2014
 * Time: 15:50
 */
public class ConfirmDialog extends DialogBox {

    private ConfirmCallback confirmCallback;

    /**
     * Базовый конструктор диалога.
     *
     * @param text            текст диалога, воспринимается как обычный текст, т.е. будет вставлен в диалог как есть, любые управляющие теги/символы будут проигнорированы.
     * @param confirmCallback объект обратного вызова при клике на кнопки диалога
     */
    public ConfirmDialog(String text, ConfirmCallback confirmCallback) {
        this.confirmCallback = confirmCallback;
        init(text, false);
    }

    /**
     * Расширенные конструктор диалога
     *
     * @param text            текст диалога, воспринимается как обычный текст, т.е. будет вставлен в диалог как есть,
     *                        любые управляющие теги/символы будут проигнорированы.
     * @param isHtml          должен ли текст восприниматься как HTML, либо как обычный текст
     *                        (в последнем случае управляющие html-теги (ровно как и любые другие) работать не будут)
     * @param confirmCallback объект обратного вызова при клике на кнопки диалога
     */
    public ConfirmDialog(String text, boolean isHtml, ConfirmCallback confirmCallback) {
        this.confirmCallback = confirmCallback;
        init(text, isHtml);
    }

    /**
     * Выполняет инициализационные действия по созданию диалога.
     *
     * @param text   текст диалога
     * @param isHtml должен ли текст восприниматься как HTML, либо как обычный текст
     *               (в последнем случае управляющие html-теги (ровно как и любые другие) работать не будут)
     */
    private void init(String text, boolean isHtml) {
        this.setAnimationEnabled(true);
        this.setStyleName("confirmDialogWindow");

        AbsolutePanel panel = new AbsolutePanel();
        panel.setStyleName("confirmDialogContent");

        if (isHtml) {
            final HTML html = new HTML();
            html.setHTML(text);
            html.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().confirmDialogIm());

            panel.add(html);
        } else {
            Label label = new Label(text);
            label.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().confirmDialogIm());

            panel.add(label);
        }

        Panel buttonsPanel = initButtonsPanel();
        panel.add(buttonsPanel);
        this.add(panel);
    }

    private Panel initButtonsPanel() {
        AbsolutePanel buttonsPanel = new AbsolutePanel();
        buttonsPanel.setStyleName("buttonsPanel");
        Button okButton = new Button("ОК");
        okButton.setStyleName("darkButton");
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                doAffirmative();
            }
        });
        buttonsPanel.add(okButton);
        final Button cancelButton = new Button(LocalizeUtil.get(LocalizationKeys.CANCELLATION_BUTTON_KEY,
                BusinessUniverseConstants.CANCELLATION_BUTTON));
//        cancelButton.setFocus(true);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                cancelButton.setFocus(true);
            }
        });

        cancelButton.setStyleName("lightButton");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                doCancel();
            }
        });
        buttonsPanel.add(cancelButton);
        return buttonsPanel;
    }

    private void doAffirmative() {
        this.hide();
        if (confirmCallback != null) {
            confirmCallback.onAffirmative();
        }
    }

    private void doCancel() {
        this.hide();
        if (confirmCallback != null) {
            confirmCallback.onCancel();
        }
    }

    public void confirm() {
        this.show();
        Style style = this.getElement().getStyle();
        style.setTop(80, Style.Unit.PX);
        style.setLeft(39, Style.Unit.PCT);
    }

}
