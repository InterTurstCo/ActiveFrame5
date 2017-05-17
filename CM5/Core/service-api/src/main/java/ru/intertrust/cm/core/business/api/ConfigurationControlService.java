package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.SummaryConfigurationException;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Сервис загрузки и работы с конфигурацией доменных объектов
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:32 PM
 */
public interface ConfigurationControlService {
    String CONFIGURATION_CHANGE_MESSAGE = "__CONFIG_CHANGED";

    public interface Remote extends ConfigurationControlService {

    }
    /**
     * Обновляет конфигурацию системы фрагментом конфигурации {@code configurationString}.
     * Обновляются только те части конфигурации, изменение которых не требует изменений структуры базы данных.
     * Например, изменения конфигурации доменных объектов будут проигнорированны,
     * а изменения конфигурации коллекций будут обработаны.
     * @param configurationString обновляемый фрагмент конфигурации
     * @throws ru.intertrust.cm.core.config.ConfigurationException
     */
    @Deprecated
    void updateConfiguration(String configurationString, String fileName) throws ConfigurationException;

    /**
     * Проверяет требуется ли рестарт приложения для полного апдейта конфигурации
     * @param configurationString строка конфигурации
     * @return true, если требуется перезагрузка, false - в противном случае
     */
    @Deprecated // marked for removal
    boolean restartRequiredForFullUpdate(String configurationString);

    /**
     * Производит активацию конфигурационных расширений из черновиков, применяет локально и уведомляет узлы кластера
     * @param toolingIds идентификаторы черновиков, которые необходимо активировать
     * @throws ConfigurationException
     */
    void activateDraftsById(List<Id> toolingIds) throws ConfigurationException;

    /**
     * Производит активацию конфигурационных расширений из черновиков, применяет локально и уведомляет узлы кластера
     * @param toolingDOs доменные объекты черновиков, которые необходимо активировать
     * @throws ConfigurationException
     */
    void activateDrafts(List<DomainObject> toolingDOs) throws ConfigurationException;

    /**
     * Производит проверку конфигурационных расширений из черновиков
     * @param toolingDOs доменные объекты черновиков, которые необходимо проверить
     * @throws SummaryConfigurationException, содержащий список проблем (исключений), возникающих при попытке слияния черновиков
     */
    void validateDrafts(List<DomainObject> toolingDOs) throws SummaryConfigurationException;

    /**
     * Сохраняет черновики конфигурационных расширений
     * @param toolingDOs доменные объекты черновиков, которые необходимо проверить
     * @throws ConfigurationException
     */
    List<DomainObject> saveDrafts(List<DomainObject> toolingDOs) throws ConfigurationException;

    /**
     * Производит активацию конфигурационных расширений из всех черновиков, применяет локально и уведомляет узлы кластера
     * @throws ConfigurationException
     */
    void activateDrafts() throws ConfigurationException;

    /**
     * Производит активацию конфигурационных расширений из набора файлов, применяет локально и уведомляет узлы кластера
     * @param files файлы конфигураций
     * @throws ConfigurationException
     */
    void activateFromFiles(Collection<File> files) throws ConfigurationException;

    /**
     * Возвращает TopLevelConfig из дистрибутива конфигурации
     * @param tagType тип тэга
     * @param tagName название тэга
     * @return TopLevelConfig дистрибутива конфигурации
     */
    TopLevelConfig getDistributiveConfig(String tagType, String tagName);
}
