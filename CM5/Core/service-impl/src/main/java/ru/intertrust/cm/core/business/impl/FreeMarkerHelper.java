package ru.intertrust.cm.core.business.impl;

import freemarker.template.*;
import ru.intertrust.cm.core.model.NotificationException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Класс для работы с FreeMarker
 */
public class FreeMarkerHelper {

    /**
     * Метод сведения шаблона FreeMarker и модели данных
     * @param templateStr шаблон FreeMarker
     * @param model модель данных
     * @return результат форматирования
     */
    public static String format(String templateStr, Map<String, Object> model) throws NotificationException {

        try {
            Template template = new Template("Template", new StringReader(templateStr), new Configuration());
            StringWriter out = new StringWriter();
            template.process(model, out);
            return out.toString();
        } catch (IOException | TemplateException e) {
            throw new NotificationException(e);
        }
    }
}
