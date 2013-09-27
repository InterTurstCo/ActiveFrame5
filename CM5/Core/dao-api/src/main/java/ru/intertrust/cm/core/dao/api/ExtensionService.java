package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.dao.api.extension.ExtensionPointHandler;

/**
 * интерфейс сервиса точек расширения
 * 
 * @author larin
 * 
 */
public interface ExtensionService {
	/**
	 * Получение точки расширения в месте ее вызова
	 * 
	 * @param extentionPointInterface
	 * @return
	 */
	ExtensionPointHandler getExtentionPoint(Class<? extends ExtensionPointHandler> extentionPointInterface, String filter);

}
