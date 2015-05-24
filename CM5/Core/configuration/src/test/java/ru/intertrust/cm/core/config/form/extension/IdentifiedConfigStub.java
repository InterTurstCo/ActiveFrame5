package ru.intertrust.cm.core.config.form.extension;

import ru.intertrust.cm.core.config.gui.IdentifiedConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.05.2015
 *         Time: 13:00
 */
public class IdentifiedConfigStub implements IdentifiedConfig {
    private String id;
    private String content;

    public IdentifiedConfigStub(String id, String contentPrefix) {
        this.id = id;
        this.content = contentPrefix + id;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
