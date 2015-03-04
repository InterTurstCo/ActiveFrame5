package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.BeforeActionExecutionConfig;
import ru.intertrust.cm.core.config.gui.action.LinkedDomainObjectConfig;
import ru.intertrust.cm.core.config.gui.action.OnSuccessMessageConfig;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.FormDialogBox;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.SimpleTextTooltip;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

/**
 * <p>
 * Действие, которое можно выполнить из плагина при помощи соответствующего элемент пользовательского интерфейса
 * (например, кнопкой или гиперссылкой).
 * </p>
 * <p>
 * Действие является компонентом GUI и должно быть именовано {@link ru.intertrust.cm.core.gui.model.ComponentName}.
 * </p>
 *
 * @author Denis Mitavskiy
 *         Date: 27.08.13
 *         Time: 16:13
 */
public abstract class Action extends BaseComponent {
    /**
     * Плагин, инициирующий данное действие
     */
    protected Plugin plugin;

    /**
     * Конфигурация данного действия
     */
    protected ActionContext initialContext;

    /**
     * Возвращает плагин, инициирующий данное действие
     * @return плагин, инициирующий данное действие
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Устанавливает плагин, инициирующий данное действие
     * @param plugin плагин, инициирующий данное действие
     */
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Возвращает конфигурацию данного действия
     * @return конфигурация данного действия
     */
    public <T extends ActionContext> T getInitialContext() {
        return (T) initialContext;
    }

    /**
     * Устанавливает конфигурацию данного действия
     * @param initialContext конфигурация данного действия
     */
    public void setInitialContext(ActionContext initialContext) {
        this.initialContext = initialContext;
    }

    public final void perform() {
        final ActionConfig config = getInitialContext().getActionConfig();
        final ConfirmCallback checkDirtyCallback = new DirtyExecuteConfirmCallback();
        if (config != null && config.isDirtySensitivity()) {
            Application.getInstance().getActionManager().checkChangesBeforeExecution(checkDirtyCallback);
        } else {
            checkDirtyCallback.onAffirmative();
        }
    }

    public boolean shouldBeValidated() {
        ActionConfig config =
                initialContext.getActionConfig() == null ? null : (ActionConfig) initialContext.getActionConfig();
        return config != null && !config.isImmediate();
    }

    public boolean isValid() {
        return true;
    }

    protected abstract void execute();

    protected void showOnSuccessMessage(final ActionData actionData, final String defaultMessage) {
        final ActionConfig config = getInitialContext().getActionConfig();
        final String onSuggestMessageType;
        if (config == null || config.getAfterConfig() == null || config.getAfterConfig().getMessageConfig() == null) {
            onSuggestMessageType = OnSuccessMessageConfig.DEFAULT_NOTIFICATION_TYPE;
        } else {
            onSuggestMessageType = config.getAfterConfig().getMessageConfig().getSuccessNotificationType();
        }
        String onSuccessMessage = (actionData == null || actionData.getOnSuccessMessage() == null)
                ? defaultMessage
                : actionData.getOnSuccessMessage();
        if (onSuccessMessage != null) {
            if (OnSuccessMessageConfig.DEFAULT_NOTIFICATION_TYPE.equals(onSuggestMessageType)) {
                new SimpleTextTooltip(onSuccessMessage).center();
            } else {
                ApplicationWindow.infoAlert(onSuccessMessage);
            }
        }
    }

    private class DirtyExecuteConfirmCallback implements ConfirmCallback {
        @Override
        public void onAffirmative() {
            final ConfirmCallback beforeExecutionCallback = new BeforeExecutionConfirmationCallback();
            final ActionConfig config = getInitialContext().getActionConfig();
            final String confirmMessage =
                    (config == null || config.getBeforeConfig() == null || config.getBeforeConfig().getMessageConfig() == null)
                            ? null
                            : config.getBeforeConfig().getMessageConfig().getText();
            if (confirmMessage != null) {
                ApplicationWindow.confirm(confirmMessage, beforeExecutionCallback);
            } else {
                beforeExecutionCallback.onAffirmative();
            }
        }

        @Override
        public void onCancel() {
        }
    }


    private class BeforeExecutionConfirmationCallback implements ConfirmCallback {
        @Override
        public void onAffirmative() {
            if (!shouldBeValidated() || isValid()) {
                final ActionConfig actionConfig = Action.this.getInitialContext().getActionConfig();
                final BeforeActionExecutionConfig beforeExecutionConfig =
                        actionConfig == null ? null : actionConfig.getBeforeConfig();
                if (beforeExecutionConfig != null && beforeExecutionConfig.getLinkedDomainObjectConfig() != null) {
                    final LinkedDomainObjectConfig doToCreateConfig =
                            beforeExecutionConfig.getLinkedDomainObjectConfig();
                    final FormDialogBox formDialogBox = new FormDialogBox(doToCreateConfig.getTitle());
                    final FormPluginConfig config = new FormPluginConfig();
                    config.setFormViewerConfig(new FormViewerConfig()
                            .addFormMappingConfig(actionConfig.getBeforeConfig().getLinkedDomainObjectConfig()
                                    .getFormMappingConfig()));
                    config.setDomainObjectTypeToCreate(actionConfig.getBeforeConfig().getLinkedDomainObjectConfig()
                            .getFormMappingConfig().getDomainObjectType());
                    final FormPlugin formPlugin = formDialogBox.createFormPlugin(config, new SimpleEventBus());
                    String continueButtonText = LocalizeUtil.get(LocalizationKeys.CONTINUE_BUTTON_KEY, BusinessUniverseConstants.CONTINUE_BUTTON);
                    formDialogBox.initButton(continueButtonText, new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            if (actionConfig.getBeforeConfig().getLinkedDomainObjectConfig().isPerformValidation()) {
                                // FIXME check validation after https://jira.inttrust.ru:8443/browse/CMFIVE-1789
                            }
                            formDialogBox.hide();
                            Action.this.getInitialContext().setConfirmFormState(formPlugin.getFormState());
                            execute();
                        }
                    });
                    formDialogBox.initButton(LocalizeUtil.get(LocalizationKeys.CANCELLATION_BUTTON_KEY,
                            BusinessUniverseConstants.CANCELLATION_BUTTON), new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            formDialogBox.hide();
                        }
                    });
                } else {
                    execute();
                }
            }
        }

        @Override
        public void onCancel() {
        }
    }
}
