package ru.intertrust.cm.core.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Валидация матриц доступа в дереве ДО на корректность флага read-everybody.
 * Если данный флаг установлен хотя бы в одной матрице, в остальных матрицах
 * значение read-everybody=false и наличие блоков read в матрице не допускается
 * @author larin
 * 
 */
public class ReadEvrybodyPermissionLogicalValidator implements ConfigurationValidator {
    final static Logger logger = LoggerFactory.getLogger(ReadEvrybodyPermissionLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;

    public ReadEvrybodyPermissionLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет логическую валидацию конфигурации. Находим все матрицы с типом
     * read-everybody=true и проверяем всю иерархию вверх и вриз на то чтобы у
     * всех были был read-everybody=true или read-everybody=null и отсутствовал
     * блок read в матрицах
     */
    public List<LogicalErrors> validate() {
        ConfigurationException exception = null;
        Collection<AccessMatrixConfig> accessMatrixConfigs = configurationExplorer.getConfigs(AccessMatrixConfig.class);
        for (AccessMatrixConfig accessMatrixConfig : accessMatrixConfigs) {
            //Ищем все матрицы с read-everybody=true
            if (accessMatrixConfig.isReadEverybody() != null && accessMatrixConfig.isReadEverybody()) {
                //Получаем родительский тип для того типа к которому относится матрица прав
                String rootType = configurationExplorer.getDomainObjectRootType(accessMatrixConfig.getType());
                //Получаем всех потомков
                List<String> checkTypes = getChildTypes(rootType);
                //В список проверяемых типов добавляем и самого рута
                checkTypes.add(rootType);
                for (String checkType : checkTypes) {
                    //Для каждого потомка проверяем тип матрицы
                    AccessMatrixConfig checkMatrixConfig = configurationExplorer.getAccessMatrixByObjectType(checkType);
                    if (checkMatrixConfig != null && checkMatrixConfig.isReadEverybody() != null && !checkMatrixConfig.isReadEverybody()) {
                        exception = new ConfigurationException("Access Matrix tor type " + accessMatrixConfig.getType() + " has read-everybody=true, but type "
                                + checkType + " from " + rootType + " type hierarchy has matrix with read-everybody=" + checkMatrixConfig.isReadEverybody());
                        //Не брасаем Exception а пишем в консоль
                        if (exception != null) {
                            logger.warn("Not valid configuration.", exception);
                        }

                    }

                    //Теперь проверяем отсутствие блоков read
                    if (checkMatrixConfig != null && checkMatrixConfig.getStatus() != null) {
                        for (AccessMatrixStatusConfig accessMatrixStatusConfig : checkMatrixConfig.getStatus()) {
                            if (accessMatrixStatusConfig.getPermissions() != null) {
                                for (BaseOperationPermitConfig baseOperationPermit : accessMatrixStatusConfig.getPermissions()) {
                                    if (baseOperationPermit instanceof ReadConfig) {
                                        exception =
                                                new ConfigurationException("Access Matrix tor type " + accessMatrixConfig.getType()
                                                        + " has read-everybody=true, but type "
                                                        + checkType + " from " + rootType + " type hierarchy has matrix with read block.");
                                        //Не брасаем Exception а пишем в консоль
                                        if (exception != null) {
                                            logger.warn("Not valid configuration.", exception);
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return new ArrayList<>();
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
