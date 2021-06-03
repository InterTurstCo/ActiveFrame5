package ru.intertrust.cm.core.dao.impl;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.dao.api.ClassPathScanService;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPointHandler;
import ru.intertrust.cm.core.dao.impl.extension.ExtensionInvocationHandler;
import ru.intertrust.cm.core.model.ExtensionPointException;

/**
 * Реализация сервиса точек расширения.
 *
 * @author larin
 */
public class ExtensionServiceImpl implements ExtensionService {
    private static final Logger logger = LoggerFactory.getLogger(ExtensionServiceImpl.class);
    /**
     * Реестр точек расширения
     */
    private static Hashtable<Class<? extends ExtensionInvocationHandler>, Hashtable<String, List<ExtensionPointHandler>>> registry;

    @Autowired
    private ApplicationContext localApplicationContext;

    @Autowired
    private ClassPathScanService scanService;

    /**
     * Флаг инициализации
     */
    private final List<String> initContexts = new ArrayList<>();

    public ExtensionServiceImpl() {
        registry = new Hashtable<>();
    }

    /**
     * Получение точки расширения по имени интерфейса точки расширения и фильтру. Фильтр может быть null в этом случае
     * возвратятся все точки расширения для переданного интерфейса
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getExtensionPoint(
            Class<T> extensionPointInterface,
            String filter) {
        init(ExtensionService.PLATFORM_CONTEXT, localApplicationContext);

        ExtensionInvocationHandler handler = new ExtensionInvocationHandler(this, filter);
        return (T) Proxy.newProxyInstance(extensionPointInterface.getClassLoader(),
                new Class<?>[] {extensionPointInterface}, handler);
    }

    /**
     * Инициализация точек расширения, путем сканирования всех классов, аннотированых ExtensionPoint аннотацией и
     * заполнение реестра
     */
    @Override
    @SuppressWarnings("unchecked")
    public void init(String contextName, ApplicationContext applicationContext) {
        try {
            if (!initContexts.contains(contextName)) {

                // Цикл по найденным классам
                for (BeanDefinition bd : scanService.findClassesByAnnotation(ExtensionPoint.class)) {
                    String className = bd.getBeanClassName();
                    // Получение найденного класса
                    Class<?> extentionPointClass = Class.forName(className);
                    // Получение аннотации ExtensionPoint
                    ExtensionPoint annotation = extentionPointClass.getAnnotation(ExtensionPoint.class);
                    //Проверка на то что загружаем точку расширения в правильном контексте
                    if (annotation.context().equalsIgnoreCase(contextName)) {

                        // Проверка наличия анотации в классе

                        // Получение интерфейса, который имплементит точка
                        // расширения, интерфейс должен быть наследником
                        // ExtensionPointHandler
                        Class<?>[] interfaces = extentionPointClass.getInterfaces();
                        //List<Class> interfaceClasses = new ArrayList<Class>();
                        ExtensionPointHandler extensionPoint = null;
                        for (Class<?> interfaceClass : interfaces) {
                            if (ExtensionPointHandler.class.isAssignableFrom(interfaceClass)) {

                                // Получаем фильтр из аннотации
                                String filter = annotation.filter();

                                // CMFIVE-1491 значение фильтра должно быть case-insensitive
                                if (filter != null) {
                                    filter = Case.toLower(filter);
                                }

                                if (extensionPoint == null) {
                                    // Проверяем есть ли спринг бин этого
                                    // класса, если есть то используем его
                                    String[] beanNames =
                                            applicationContext.getBeanNamesForType(extentionPointClass, false, false);
                                    if (beanNames.length > 0) {
                                        extensionPoint = (ExtensionPointHandler) applicationContext
                                                .getBean(extentionPointClass);
                                    } else {
                                        // Если такого бина нет то создаем
                                        // экземпляр класса
                                        // Создаем экземпляр точки расширения
                                        // Добавляем
                                        // класс как спринговый бин с поддержкой
                                        // autowire
                                        extensionPoint =
                                                (ExtensionPointHandler) applicationContext
                                                        .getAutowireCapableBeanFactory().createBean(
                                                                extentionPointClass,
                                                                AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
                                                                true);

                                        ((ConfigurableApplicationContext) applicationContext)
                                                .getBeanFactory().registerSingleton(
                                                extentionPointClass.getName(),
                                                extensionPoint);
                                    }
                                }

                                // Сохраняем точку расширения в реестр
                                Hashtable<String, List<ExtensionPointHandler>> oneTypeExtensions = registry
                                        .get(interfaceClass);
                                if (oneTypeExtensions == null) {
                                    oneTypeExtensions = new Hashtable<>();
                                    registry.put((Class<? extends ExtensionInvocationHandler>) interfaceClass,
                                            oneTypeExtensions);
                                }
                                List<ExtensionPointHandler> filteredExtension = oneTypeExtensions.computeIfAbsent(filter, k -> new ArrayList<>());
                                //Если ранее не регистрировался данный класс то регистрируем его
                                if (!filteredExtension.contains(extensionPoint)) {
                                    filteredExtension.add(extensionPoint);
                                    logger.info("Register extensionPoint {} ({}) = {}", interfaceClass.getName(), filter, extensionPoint.getClass().getName());
                                }
                            }
                        }
                    }
                }
                initContexts.add(contextName);
            }
        } catch (Exception ex) {
            throw new ExtensionPointException(
                    "Error on init extension point classes", ex);
        }
    }

    /**
     * Получает список точек расширения определенного интерфейса
     */
    public List<ExtensionPointHandler> getExtensionPointList(
            Class<? extends ExtensionPointHandler> extensionPointInterface,
            String filter) {
        List<ExtensionPointHandler> result = new ArrayList<>();

        // Получаем все хэндлеры точек расширения по интерфейсу
        Map<String, List<ExtensionPointHandler>> oneTypeExtensions = registry
                .get(extensionPointInterface);

        // Проверка на наличие точек расширения определенного интерфейса
        if (oneTypeExtensions != null) {
            // Проверка на переданный фильтр точек расширения, в пределах одного
            // интерфейса
            if (filter != null) {
                // Получение точек расширения по фильтру
                List<ExtensionPointHandler> filteredExtension = oneTypeExtensions
                        .get(Case.toLower(filter));
                // Если точки расширения по фильтру найдены то добавляем их в
                // результат
                if (filteredExtension != null && !filteredExtension.isEmpty()) {
                    result.addAll(filteredExtension);
                }
            } else {
                // Если фильтр не указан то получаем обработчики
                // зарегистрированные со всеми значениями фильтрв и добавляем их
                // в результат
                for (List<ExtensionPointHandler> extentionPoints : oneTypeExtensions
                        .values()) {
                    result.addAll(extentionPoints);
                }

            }
        }
        return result;
    }


}
