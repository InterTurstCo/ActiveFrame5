package ru.intertrust.cm.core.pdf;

import ru.intertrust.cm.core.dao.api.component.ServerComponentHandler;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 12.05.2016
 * Time: 14:58
 * To change this template use File | Settings | File and Code Templates.
 */
public interface PropertiesProvider extends ServerComponentHandler {
    Object getProperties();
}
