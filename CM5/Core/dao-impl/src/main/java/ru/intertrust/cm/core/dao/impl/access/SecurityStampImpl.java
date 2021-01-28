package ru.intertrust.cm.core.dao.impl.access;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.GlobalServerSettingsService;
import ru.intertrust.cm.core.config.AccessMatrixConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.SecurityStamp;
import ru.intertrust.cm.core.dao.api.extension.AfterClearGlobalCacheExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint
public class SecurityStampImpl implements SecurityStamp, AfterClearGlobalCacheExtentionHandler {

    private Map<String, Boolean> configs = new HashMap<>();
    private Set<String> globalConfigs;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private GlobalServerSettingsService globalServerSettingsService;

    @Override
    public boolean isSupportSecurityStamp(String typeName) {

        // Сначала проверяем есть ли хоть один тип в настройке кластера,
        // если нет ни одного остальные вычисления не производим
        if (getGlobalConfig().isEmpty()){
            return false;
        }

        // Получаем из кэша
        Boolean result = configs.get(typeName.toLowerCase());

        // Если в кэше нет то вычисляем
        if (result == null){
            // Проверяем настройку по матрице
            result = isSupportStampByAccessMatrix(typeName);

            if (result) {
                // Проверяем настройку кластера
                result = isStampEnable(typeName);
            }

            configs.put(typeName.toLowerCase(), result);
        }
        return result;
    }

    private Set<String> getGlobalConfig(){
        if (globalConfigs == null) {
            globalConfigs = new HashSet<>();
            String stampedTypes = globalServerSettingsService.getString(STAMPED_TYPES_CONGIG_NAME);
            if (stampedTypes != null) {
                String[] stampedTypesArray = stampedTypes.split("[ ,;]");
                for (int i = 0; i < stampedTypesArray.length; i++) {
                    globalConfigs.add(stampedTypesArray[i].toLowerCase());
                }
            }
        }
        return globalConfigs;
    }


    /**
     * Проверка включены ли грфы в настройках кластера
     * @param typeName
     * @return
     */
    private boolean isStampEnable(String typeName) {

        // Проверяем непосредственно тип
        boolean result = getGlobalConfig().contains(typeName.toLowerCase());

        if (!result){
            // Проверяем все типы в ветке иерархии, сначала для root
            String rootType = configurationExplorer.getDomainObjectRootType(typeName);
            result = getGlobalConfig().contains(rootType.toLowerCase());

            // потом все дочки
            if (!result) {
                for (DomainObjectTypeConfig childType : configurationExplorer.findChildDomainObjectTypes(rootType, true)) {
                    if (getGlobalConfig().contains(childType.getName().toLowerCase())) {
                        result = true;
                        break;
                    }
                }
            }
        }

        if (!result){
            // Проверяем типы, откуда заимствуем права
            AccessMatrixConfig accessmatrix = configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(typeName);
            if (accessmatrix != null && accessmatrix.getMatrixReference() != null) {
                String referenceMatrixType = configurationExplorer.getMatrixReferenceTypeName(typeName);
                if (referenceMatrixType != null) {

                    // Получаем root тип для типа, откуда заимствуем матрицу
                    String rootReferenceMatrixType = configurationExplorer.getDomainObjectRootType(referenceMatrixType);

                    // Проверяем включено ли для него
                    result = getGlobalConfig().contains(rootReferenceMatrixType);

                    if (!result){
                        // Проверяем есть ли в иерархии типа хоть один тип с поддержкой грифов
                        for (DomainObjectTypeConfig childType : configurationExplorer.findChildDomainObjectTypes(rootReferenceMatrixType, true)) {
                            if (getGlobalConfig().contains(childType.getName().toLowerCase())) {
                                result = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * Получение информации поддерживает ли тип грифы из настроек матрицы доступа
     * @param typeName
     * @return
     */
    boolean isSupportStampByAccessMatrix(String typeName){
        boolean result = false;

        AccessMatrixConfig acessMatrix =
                configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(typeName);
        if (acessMatrix != null) {
            if (acessMatrix.isSupportSecurityStamp() != null && acessMatrix.isSupportSecurityStamp() && acessMatrix.getMatrixReference() == null) {
                // Права не заимствуются, настройка в собственной матрице
                result = acessMatrix.isSupportSecurityStamp();
            }else if (acessMatrix.getMatrixReference() != null){
                // Права заимствуются, ищем матрицу, которую заимствуем и смотрим настройки грифов там.
                String matrixType = configurationExplorer.getMatrixReferenceTypeName(typeName);
                if (matrixType != null){
                    // Относительно найденного типа берем рутовый тип
                    String matrixRootType = configurationExplorer.getDomainObjectRootType(matrixType);
                    // Проверяем есть ли матрица у root Типа
                    result = isSupportStampByAccessMatrix(matrixRootType);

                    if (!result) {
                        // Проверяем есть хоть одна дочка с грифом
                        result = isChildSupportStampByAccessMatrix(matrixRootType);
                    }
                }
            }
        }

        // Если хоть один наследник поддерживает грифы, то и базовый тип должен поддерживать грифы,
        // чтоб корректно работал запрос типа select * from base_type
        if (!result) {
            result = isChildSupportStampByAccessMatrix(typeName);
        }

        return result;
    }

    /**
     * Проверка поддерживает ли конфигурация на уровне матриц грифы для хотя бы одного дочернего типа
     * @param typeName
     * @return
     */
    private boolean isChildSupportStampByAccessMatrix(String typeName){
        boolean result = false;
        Collection<DomainObjectTypeConfig> childTypeConfigs =
                configurationExplorer.findChildDomainObjectTypes(typeName, true);
        for (DomainObjectTypeConfig childTypeConfig : childTypeConfigs) {
            if (isSupportStampByAccessMatrix(childTypeConfig.getName())){
                result = true;
                break;
            }
        }

        return result;
    }

    @Override
    public void onClearGlobalCache() {
            configs.clear();
            globalConfigs = null;
    }
}
