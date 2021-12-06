package ru.intertrust.cm.core.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.config.AccessMatrixConfig.BorrowPermissisonsMode;

/**
 * Валидация матриц доступа с целью наличия в матрицах иерархии объектов только
 * одно типа матриц или с косвенными или непосредственно настроенными правами
 * @author larin
 * 
 */
public class IndirectlyPermissionLogicalValidator implements ConfigurationValidator {
    final static Logger logger = LoggerFactory.getLogger(IndirectlyPermissionLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;

    public IndirectlyPermissionLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет логическую валидацию конфигурации. Находим все матрицы с типом
     * косвенные права и проверяем всю иерархию вверх и врих на то чтобы у всех
     * были аналогичные тип матрицы.
     * Также проверяем соответствие атрибутов matrix-reference-field и borrow-permissisons
     * так же проверяем корректность имени атрибута matrix-reference-field
     */
    @Override
    public List<LogicalErrors> validate() {
        List<LogicalErrors> logicalErrorsList = new ArrayList<>();

        Collection<AccessMatrixConfig> accessMatrixConfigs = configurationExplorer.getConfigs(AccessMatrixConfig.class);
        //Ищем все матрицы с косвенными правами
        for (AccessMatrixConfig accessMatrixConfig : accessMatrixConfigs) {
            LogicalErrors logicalErrors = LogicalErrors.getInstance(accessMatrixConfig.getName(), "access-matrix");
            //Проверка на то что в матрице указан корректный тип
            DomainObjectTypeConfig typeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, accessMatrixConfig.getType());
            if (typeConfig == null){
                logicalErrors.addError("Access Matrix tor type " + accessMatrixConfig.getType() +
                        " can not find type config with name " + accessMatrixConfig.getType());
            }
            
            //Проверка конфигурации заимствования прав
            if (accessMatrixConfig.getMatrixReference() != null && accessMatrixConfig.getMatrixReference().length() > 0){
                //Проверка на то что matrix-reference-field указывает на существующее поле и это поле типа reference
                FieldConfig fieldConfig = configurationExplorer.getFieldConfig(accessMatrixConfig.getType(), accessMatrixConfig.getMatrixReference());
                if (fieldConfig == null){
                    logicalErrors.addError("Access Matrix tor type " + accessMatrixConfig.getType() +
                            " field " + accessMatrixConfig.getMatrixReference() + " not found in type " + accessMatrixConfig.getType());                    
                }else if(!(fieldConfig instanceof ReferenceFieldConfig)){
                    logicalErrors.addError("Access Matrix tor type " + accessMatrixConfig.getType() +
                            " field " + accessMatrixConfig.getMatrixReference() + " in matrix-reference-field attribute must be reference type");                    
                }
                
                //Проверка допустимых комбинаций matrix-reference-field и borrow-permissisons и status
                if (accessMatrixConfig.getBorrowPermissisons() == null || accessMatrixConfig.getBorrowPermissisons() == BorrowPermissisonsMode.all){
                    if (accessMatrixConfig.getStatus() != null && accessMatrixConfig.getStatus().size() > 0
                            && !isAllowedStatusesForMatrixRef(accessMatrixConfig)){
                        /* Права на чтение вложений не наследуются, поэтому указание статусов для них разрешено */
                        logicalErrors.addError("Access Matrix tor type " + accessMatrixConfig.getType()
                                + " can not has status config when it has matrix-reference-field=" + accessMatrixConfig.getMatrixReference()
                                + " and borrow-permissisons=" + accessMatrixConfig.getBorrowPermissisons());
                    }
                    if (accessMatrixConfig.getCreateConfig() != null 
                            && accessMatrixConfig.getCreateConfig().getPermitGroups() != null 
                            && accessMatrixConfig.getCreateConfig().getPermitGroups().size() > 0){
                        logicalErrors.addError("Access Matrix tor type " + accessMatrixConfig.getType() +
                                " can not has create config when it has matrix-reference-field=" + accessMatrixConfig.getMatrixReference() +
                                " and borrow-permissisons=" + accessMatrixConfig.getBorrowPermissisons());
                    }
                }else if(accessMatrixConfig.getBorrowPermissisons() == BorrowPermissisonsMode.readWriteDelete){
                    if (accessMatrixConfig.getStatus() != null && accessMatrixConfig.getStatus().size() > 0
                            && !isAllowedStatusesForMatrixRef(accessMatrixConfig)){
                        logicalErrors.addError("Access Matrix tor type " + accessMatrixConfig.getType() +
                                " can not has status config when it has matrix-reference-field=" + accessMatrixConfig.getMatrixReference() +
                                " and borrow-permissisons=" + accessMatrixConfig.getBorrowPermissisons());
                    }
                }else if(accessMatrixConfig.getBorrowPermissisons() == BorrowPermissisonsMode.read){
                    //При borrow-permissisons == none не должно быть read пав в статусах
                    if (accessMatrixConfig.getStatus() != null)
                    for (AccessMatrixStatusConfig accessMatrixStatusConfig : accessMatrixConfig.getStatus()) {
                        if (accessMatrixStatusConfig.getPermissions() != null)
                        for (BaseOperationPermitConfig operationPermitConfig : accessMatrixStatusConfig.getPermissions()) {
                            if (operationPermitConfig instanceof ReadConfig){
                                logicalErrors.addError("Access Matrix tor type " + accessMatrixConfig.getType() +
                                        " can not has read config when it has matrix-reference-field=" + accessMatrixConfig.getMatrixReference() +
                                        " and borrow-permissisons=" + accessMatrixConfig.getBorrowPermissisons());
                            }
                        }                        
                    }
                }else{
                    //При borrow-permissisons == none не должно быть matrix-reference-field
                    logicalErrors.addError("Access Matrix tor type " + accessMatrixConfig.getType() +
                            " can not has matrix-reference-field=" + accessMatrixConfig.getMatrixReference() +
                            " when borrow-permissisons=" + accessMatrixConfig.getBorrowPermissisons());
                }
            }else{
                //При отсутствие matrix-reference-field допускается только borrow-permissisons == none или его отсутствие
                if (accessMatrixConfig.getBorrowPermissisons() == BorrowPermissisonsMode.read || 
                        accessMatrixConfig.getBorrowPermissisons() == BorrowPermissisonsMode.all){
                    logicalErrors.addError("Access Matrix tor type " + accessMatrixConfig.getType() +
                            " matrix-reference-field need has value" +
                            " when borrow-permissisons=" + accessMatrixConfig.getBorrowPermissisons());
                }
            }   
            
            
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
                        logicalErrors.addError("Access Matrix tor type " + accessMatrixConfig.getType() +
                                " has indirectly permissions, but type " + checkType + " from " + rootType +
                                " type hierarchy has direct permission");
                    }
                }
                
            }
            if (logicalErrors.getErrorCount() > 0) {
                logicalErrorsList.add(logicalErrors);
            }
        }

        return logicalErrorsList;
    }

    private boolean isAllowedStatusesForMatrixRef(@Nonnull AccessMatrixConfig accessMatrixConfig) {
        if (accessMatrixConfig.getStatus() == null) {
            return true;
        }
        return accessMatrixConfig.getStatus().stream()
                .map(AccessMatrixStatusConfig::getPermissions)
                .flatMap(List::stream)
                .allMatch(conf -> conf instanceof ReadAttachmentConfig);
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
