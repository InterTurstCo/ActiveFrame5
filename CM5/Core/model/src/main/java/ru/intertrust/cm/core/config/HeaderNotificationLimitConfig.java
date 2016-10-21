package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 21.10.2016
 * Time: 15:29
 * To change this template use File | Settings | File and Code Templates.
 */
@Root(name = "header-notification-limit")
public class HeaderNotificationLimitConfig implements Dto {
    @Attribute(name = "limit")
    private Integer limit;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer time) {
        this.limit = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HeaderNotificationLimitConfig that = (HeaderNotificationLimitConfig) o;

        if (!limit.equals(that.limit)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return limit.hashCode();
    }
}
