package ru.intertrust.cm.core.business.impl;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ru.intertrust.cm.core.business.api.ConfigurationLoadService;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.extension.OnLoadConfigurationExtensionHandler;
import ru.intertrust.cm.core.dao.exception.DaoException;

/**
 * Класс, предназначенный для загрузки конфигурации доменных объектов
 * 
 * @author vmatsukevich Date: 5/6/13 Time: 9:36 AM
 */
public class ConfigurationLoader implements ApplicationContextAware {

    @Autowired
    private ConfigurationLoadService configurationLoadService;
    
    private ApplicationContext context;

    @Resource
    private TransactionSynchronizationRegistry txReg;    
    
    private boolean configurationLoaded;

    /*
     * @Autowired private ExtensionService extensionService;
     */

    public ConfigurationLoader() {
    }

    /**
     * Устанавливает {@link #configurationLoadService}
     * 
     * @param configurationLoadService
     *            сервис для работы с конфигурацией доменных объектов
     */
    public void setConfigurationLoadService(
            ConfigurationLoadService configurationLoadService) {
        this.configurationLoadService = configurationLoadService;
    }

    /**
     * Загружает конфигурацию доменных объектов, валидирует и создает
     * соответствующие сущности в базе. Добавляет запись администратора
     * (admin/admin) в таблицу authentication_info.
     * 
     * @throws Exception
     */
    public void load() throws Exception {
        configurationLoadService.loadConfiguration();

        // Вызов точки расширения
        if (context != null) {
            ExtensionService extensionService = context
                    .getBean(ExtensionService.class);
            OnLoadConfigurationExtensionHandler extension = extensionService.getExtentionPoint(
                    OnLoadConfigurationExtensionHandler.class, null);
            extension.onLoad();
        }
        
        //Установка флага загруженности конфигурации
        configurationLoaded = true;
        // /setLoadedFlag();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.context = applicationContext;

    }
    
    /**
     * Метод возвращает флаг загруженности конфигурации
     * @return
     */
    public boolean isConfigurationLoaded(){
        return configurationLoaded;
    }

    /**
     * Установка флага доступности конфигурации. 
     * Флаг устанавливается не сразу, а только после окончания транзакции, иначе созданные таблицы не будут доступны другим потокам
     */
    private void setLoadedFlag() {

        //не обрабатываем вне транзакции
        if (getTxReg() == null || getTxReg().getTransactionKey() == null) {
            return;
        }
        SetConfigurationLoaded setConfigurationLoaded =
                (SetConfigurationLoaded) getTxReg().getResource(SetConfigurationLoaded.class);
        if (setConfigurationLoaded == null) {
            setConfigurationLoaded = new SetConfigurationLoaded();
            getTxReg().putResource(SetConfigurationLoaded.class, setConfigurationLoaded);
            getTxReg().registerInterposedSynchronization(setConfigurationLoaded);
        }
    }

    /**
     * Получение контекта транзакции
     * @return
     */
    private TransactionSynchronizationRegistry getTxReg() {
        if (txReg == null) {
            try {
                txReg =
                        (TransactionSynchronizationRegistry) new InitialContext()
                                .lookup("java:comp/TransactionSynchronizationRegistry");
            } catch (NoInitialContextException ignoreEx) {
                //Игнорируем ошибку. Она возникает при тестах, когда контекст не создан
            }catch(NamingException ex){
                throw new DaoException("Error get transaction context", ex);
            }
        }
        return txReg;
    }

    /**
     * Класс используется для установки флага загруженности конфигурации после окончания транзакции
     * @author larin
     *
     */
    private class SetConfigurationLoaded implements Synchronization {

        @Override
        public void afterCompletion(int arg0) {
            configurationLoaded = true;
            
        }

        @Override
        public void beforeCompletion() {
        }
    }
    
    
}
