package ru.intertrust.cm.core.config.module;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import ru.intertrust.cm.core.config.ConfigurationSchemaValidator;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Сервис управления модулями. Сервис при старте просматривает все файлы
 * cm.module.xml и создает реестр модулей в системе
 * @author larin
 * 
 */
public interface ModuleService {
    /**
     * Получение списка модулей, отсортированных в порядке зависимости друг от тдруга
     * @return
     */
    List<ModuleConfiguration> getModuleList();
}
