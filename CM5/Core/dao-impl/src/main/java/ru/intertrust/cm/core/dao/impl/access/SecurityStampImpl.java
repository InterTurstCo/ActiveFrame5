package ru.intertrust.cm.core.dao.impl.access;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.GlobalServerSettingsService;
import ru.intertrust.cm.core.config.AccessMatrixConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
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
            // Проверяем все родительские типы
            for (String parentType: configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(typeName)) {
                if (getGlobalConfig().contains(parentType.toLowerCase())){
                    result = true;
                    break;
                }
            }
        }

        if (!result){
            // Проверяем типы, откуда заимствуем права
            AccessMatrixConfig accessmatrix = configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(typeName);
            if (accessmatrix.getMatrixReference() != null) {
                String referenceMatrixType = configurationExplorer.getMatrixReferenceTypeName(typeName);
                if (referenceMatrixType != null) {

                    // Проверяем включен ли у типа откуда заимствуем права
                    result = getGlobalConfig().contains(typeName.toLowerCase());

                    if (!result){

                        // Проверяем у родительских типов, отнотительно того, откуда заимствуем права
                        for (String parentType: configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(referenceMatrixType)) {
                            if (getGlobalConfig().contains(parentType.toLowerCase())){
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
                    AccessMatrixConfig referenceMatrix = configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(matrixType);
                    if (referenceMatrix != null){
                        result = referenceMatrix.isSupportSecurityStamp();
                    }
                }
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
