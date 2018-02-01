package ru.intertrust.cm.core.dao.impl.extension;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import ru.intertrust.cm.core.dao.api.extension.ExtensionPointHandler;
import ru.intertrust.cm.core.dao.impl.ExtensionServiceImpl;

/**
 * Обработчик вызова точки расширения. Производит поиск всех точек расширения в
 * реестре точек расширения и вызов нужного типа
 * 
 * @author larin
 * 
 */
public class ExtentionInvocationHandler implements InvocationHandler {
    private ExtensionServiceImpl extensionService;
    private String filter;

    public ExtentionInvocationHandler(ExtensionServiceImpl extentionService,
            String filter) {
        this.extensionService = extentionService;
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
            List<ExtensionPointHandler> interfaceClass = extensionService.getExtentionPointList(
                    (Class<? extends ExtensionPointHandler>) proxy.getClass().getInterfaces()[0], filter);

            for (ExtensionPointHandler extentionPointBase : interfaceClass) {
                method.invoke(extentionPointBase, args);
            }
            return null;
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}
