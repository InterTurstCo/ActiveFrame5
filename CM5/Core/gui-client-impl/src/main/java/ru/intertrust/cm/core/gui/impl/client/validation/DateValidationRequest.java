package ru.intertrust.cm.core.gui.impl.client.validation;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Lesia Puhova
 *         Date: 24.02.14
 *         Time: 16:10
 */
public class DateValidationRequest implements Dto {

    private String dateStr;

    public DateValidationRequest(Object value) {
        dateStr = (String) value;
    }

    private DateValidationRequest() {
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }
}
