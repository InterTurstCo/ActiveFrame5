package ru.intertrust.cm.loffice.exceptions;

import ru.intertrust.cm.core.pdf.exceptions.ConverterException;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 13.05.2016
 * Time: 12:23
 * To change this template use File | Settings | File and Code Templates.
 */
public class DefaultPdfConverterException extends ConverterException {
    public static final String NOT_CONFIGURED_MSG = "Сервис не сконфигурирован. Проверьте файл server.properties";
    public static final String XDESKTOP_NOT_INITIALIZED = "Компонент XDesktop системы LibreOffice не инициализирован. Возможно не вызван метод Init()";
    public static final String TMP_FILE_CREATION_ERROR = "Ошибка создания промежуточного файла: %s";
    public static final String CONVERTATION_ERROR = "Ошибка в процессе преобразования";
    public DefaultPdfConverterException() {
        super();
    }

    public DefaultPdfConverterException(String message) {
        super(message);
    }

    public DefaultPdfConverterException(Throwable cause) {
        super(cause);
    }

    public DefaultPdfConverterException(String message, Throwable cause) {
        super(message, cause);
    }

    public DefaultPdfConverterException(String message, Throwable cause, boolean enableSuppression,
                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
