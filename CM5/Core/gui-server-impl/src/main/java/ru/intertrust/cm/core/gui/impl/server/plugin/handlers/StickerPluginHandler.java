package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 26.11.13
 * Time: 11:09
 * To change this template use File | Settings | File Templates.
 */
@ComponentName("sticker.plugin")
public class StickerPluginHandler extends PluginHandler {
    @Override
    public PluginData initialize(Dto param) {
        System.out.println("StickerPluginHandler initialized");
        return null;
    }
}