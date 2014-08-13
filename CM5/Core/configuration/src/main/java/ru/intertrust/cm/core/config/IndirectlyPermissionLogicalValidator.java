package ru.intertrust.cm.core.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Валидация матриц доступа с целью наличия в матрицах иерархии объектов только
 * одно типа матриц или с косвенными или непосредственно настроенными правами
 * @author larin
 * 
 */
public class IndirectlyPermissionLogicalValidator {
    final static Logger logger = LoggerFactory.getLogger(IndirectlyPermissionLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;

    public IndirectlyPermissionLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет логическую валидацию конфигурации. Находим все матрицы с типом
     * косвенные права и проверяем всю иерархию вверх и врих на то чтобы у всех
     * были аналогичные тип матрицы
     */
    public void validate() {
        Collection<AccessMatrixConfig> accessMatrixConfigs = configurationExplorer.getConfigs(AccessMatrixConfig.class);
        //Ищем все матрицы с косвенными правами
        for (AccessMatrixConfig accessMatrixConfig : accessMatrixConfigs) {
            if (accessMatrixConfig.getMatrixReference() != null && accessMatrixConfig.getMatrixReference().length() > 0) {
                //Получаем родительский тип для того типа к которому относится матрица прав
                String rootType = configurationExplorer.getDomainObjectRootType(accessMatrixConfig.getType());
                //Получаем всех потомков
                List<String> checkTypes = getChildTypes(rootType);
                //В список проверяемых типов добавляем и самого рута
                checkTypes.add(rootType);
                for (String checkType : checkTypes) {
                    //Для каждого потомка проверяем тип матрицы
                    AccessMatrixConfig checkMatrixConfig = configurationExplorer.getAccessMatrixByObjectType(checkType);
                    if (checkMatrixConfig != null && (checkMatrixConfig.getMatrixReference() == null || checkMatrixConfig.getMatrixReference().length() == 0)) {
                        throw new ConfigurationException("Access Matrix tor type " + accessMatrixConfig.getType() + " has indirectly permissions, but type "
                                + checkType + " from " + rootType + " type hierarchy has direct permission");
                    }
                }
            }
        }
    }

    /**
     * Получение всех дочерних типов для переданного типа
     * @param typeName
     * @return
     */
    private List<String> getChildTypes(String typeName) {
        List<String> result = new ArrayList<String>();
        Collection<DomainObjectTypeConfig> allTypes = configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
        for (DomainObjectTypeConfig domainObjectTypeConfig : allTypes) {
            if (domainObjectTypeConfig.getExtendsAttribute() != null && domainObjectTypeConfig.getExtendsAttribute().equalsIgnoreCase(typeName)) {
                result.add(domainObjectTypeConfig.getName());
                result.addAll(getChildTypes(domainObjectTypeConfig.getName()));
            }
        }
        return result;
    }
}
