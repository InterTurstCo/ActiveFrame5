package ru.intertrust.cm.core.business.api.dto;

import java.io.Serializable;

/**
* @author vmatsukevich
*         Date: 10/29/13
*         Time: 12:37 PM
*/
public abstract class TimeZoneContext implements Serializable {

    public abstract String getTimeZoneId();
}
