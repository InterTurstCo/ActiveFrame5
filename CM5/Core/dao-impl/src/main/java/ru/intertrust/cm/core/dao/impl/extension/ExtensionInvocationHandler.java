package ru.intertrust.cm.core.dao.impl.extension;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPointHandler;
import ru.intertrust.cm.core.dao.impl.ExtensionServiceImpl;

/**
 * Обработчик вызова точки расширения. Производит поиск всех точек расширения в
 * реестре точек расширения и вызов нужного типа
 * 
 * @author larin
 * 
 */
public class ExtensionInvocationHandler implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionInvocationHandler.class);

    private final ExtensionServiceImpl extensionService;
    private final String filter;

    public ExtensionInvocationHandler(ExtensionServiceImpl extensionService, String filter) {
        this.extensionService = extensionService;
        this.filter = filter;
    }

    /**
     * Обработчик вызова точки расширения. Получает список обработчиков и
     * вызывает их в цикле
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        try {
            List<ExtensionPointHandler> interfaceClass = extensionService.getExtensionPointList(
                    (Class<? extends ExtensionPointHandler>) proxy.getClass().getInterfaces()[0], filter);

            logger.trace("{} handlers found. Attempting to execute them", interfaceClass.size());
            for (ExtensionPointHandler extensionPointHandler : interfaceClass) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Handler {} will be invoked", extensionPointHandler.getClass().getName());
                }
                method.invoke(extensionPointHandler, args);
            }

            logger.trace("All handlers successfully executed");
            return null;
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
            throw e.getCause();
        }
    }
}
