package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.model.SystemException;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 14:41
 */
public class GuiException extends SystemException implements Dto {
    public GuiException() {
        super();
    }

    public GuiException(String message) {
        super(message);
    }

    public GuiException(String message, Throwable cause) {
        super(message, cause);
    }

    public GuiException(Throwable cause) {
        super(cause);
    }

    public GuiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
