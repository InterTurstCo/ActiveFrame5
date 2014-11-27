package ru.intertrust.cm.core.gui.api.client;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: IPetrov
 * Date: 03.01.14
 * Time: 14:19
 * Абстракция для хранения данных приложения во время выполнения
 */
public abstract class ApplicationContext implements Dto {

    // Поле для хранения конфигурации областей поиска и целевых ДО
    private HashMap<String, ArrayList<String>> searchAreasData = new HashMap<String, ArrayList<String>>();
    public abstract HashMap getSearchAreasData();
}
