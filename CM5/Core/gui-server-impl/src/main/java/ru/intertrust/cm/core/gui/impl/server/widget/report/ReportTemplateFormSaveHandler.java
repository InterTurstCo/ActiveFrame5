package ru.intertrust.cm.core.gui.impl.server.widget.report;

import ru.intertrust.cm.core.gui.api.server.form.FormBeforeSaveInterceptor;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.*;

import java.util.ArrayList;
import java.util.List;

@ComponentName("report.template.form.save.handler")
public class ReportTemplateFormSaveHandler implements FormBeforeSaveInterceptor {

    @Override
    public void beforeSave(FormState formState) {
        List<String> errorMessages = new ArrayList<>(3);
        // Имя шаблона
        WidgetState widgetState = formState.getWidgetState("nameValue");
        if (widgetState != null) {
            // edit mode
            String nameValue = ((TextState) widgetState).getText();
            if (nameValue == null || nameValue.isEmpty()) {
                errorMessages.add("Поле \"Название\" обязятельно для заполнения.");
            }
        }

        // Конструктор шаблона
        widgetState = formState.getWidgetState("constructorValue");
        if (widgetState != null) {
            String constructorValue = ((EnumBoxState) widgetState).getSelectedText();
            if (constructorValue == null || constructorValue.isEmpty()) {
                errorMessages.add("Поле \"Конструктор\" обязятельно для заполнения.");
            }

            // Файл docx шаблона
            if ("Файл docx".equalsIgnoreCase(constructorValue)) {
                if (widgetState != null) {
                    widgetState = formState.getWidgetState("docxTemplateValue");
                    List<AttachmentItem> attachments = ((AttachmentBoxState) widgetState).getAttachments();
                    if (attachments == null || attachments.isEmpty()) {
                        errorMessages.add("Поле \"Файл шаблона docх\" обязятельно для заполнения, "
                                + "если в поле \"Конструктор\" выбрано значение \"Печатная форма\".");
                    }
                }
            }
        }

        if (!errorMessages.isEmpty()) {
            String errorMessage = "";
            for (String msg : errorMessages) {
                errorMessage += msg + "\n";
            }
            throw new GuiException(errorMessage);
        }
    }
}
