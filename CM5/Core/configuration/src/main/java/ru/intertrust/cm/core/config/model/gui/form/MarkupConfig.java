package ru.intertrust.cm.core.config.model.gui.form;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 11.09.13
 *         Time: 20:13
 */
public class MarkupConfig implements Dto {
    private HeaderConfig header;
    private BodyConfig body;

    public HeaderConfig getHeader() {
        return header;
    }

    public void setHeader(HeaderConfig header) {
        this.header = header;
    }

    public BodyConfig getBody() {
        return body;
    }

    public void setBody(BodyConfig body) {
        this.body = body;
    }
}
