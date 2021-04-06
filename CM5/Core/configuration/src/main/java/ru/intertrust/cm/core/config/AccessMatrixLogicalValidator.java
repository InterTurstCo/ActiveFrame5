package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lesia Puhova
 *         Date: 04.04.14
 *         Time: 12:57
 */
public class AccessMatrixLogicalValidator implements ConfigurationValidator {

    private final ConfigurationExplorer configurationExplorer;
    private final ModuleService moduleService;

    private final static Logger logger = LoggerFactory.getLogger(AccessMatrixLogicalValidator.class);

    public AccessMatrixLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
        this.moduleService = ((ConfigurationExplorerImpl)configurationExplorer).getModuleService();
    }

    @Override
    public List<LogicalErrors> validate() {
        List<LogicalErrors> logicalErrorsList = new ArrayList<>();

        Collection<AccessMatrixConfig> configList = configurationExplorer.getConfigs(AccessMatrixConfig.class);
        Map<String, List<AccessMatrixConfig>> typesAccessMatrixes = new HashMap<>();
        for (AccessMatrixConfig matrixConfig : configList) {
            // Группируем матрицы для типов, для валидации наследования
            List<AccessMatrixConfig> typeAccessMatrixes = typesAccessMatrixes.get(matrixConfig.getType().toLowerCase());
            if (typeAccessMatrixes == null){
                typeAccessMatrixes = new ArrayList<>();
                typesAccessMatrixes.put(matrixConfig.getType().toLowerCase(), typeAccessMatrixes);
            }
            typeAccessMatrixes.add(matrixConfig);

            // Логическа валидация
            LogicalErrors logicalErrors = LogicalErrors.getInstance(matrixConfig.getName(), "access-matrix");
            for (AccessMatrixStatusConfig accessMatrixStatus : matrixConfig.getStatus()) {

                // Проверка корректности read-everybody
                for (BaseOperationPermitConfig permission : accessMatrixStatus.getPermissions()) {
                    if (ReadConfig.class.equals(permission.getClass())) {
                        if (Boolean.TRUE.equals(matrixConfig.isReadEverybody())) {
                            String error = "access-matrix configured with read-everybody=\"true\"" +
                                    " cannot include any <read> tags";
                            logicalErrors.addError(error);
                            logger.error(error);
                            break;
                        } else if (matrixConfig.isReadEverybody() != null  //that is, explicitly specified as "false"
                             && ((ReadConfig) permission).isPermitEverybody() != null)  {
                             String error = "Using read-everybody attribute of <access-matrix> tag together with " +
                                     "permit-everybody attribute of <read> tag is not allowed";
                             logicalErrors.addError(error);
                            break;
                        }
                    }
                }

                // Проверка наличия типа для матрицы
                DomainObjectTypeConfig domainObjectTypeConfig =
                        configurationExplorer.getDomainObjectTypeConfig(matrixConfig.getType());
                if (domainObjectTypeConfig == null){
                    String error = "Not found type " + matrixConfig.getType() +
                            " for access-matrix in module " + matrixConfig.getModuleName();
                    logicalErrors.addError(error);
                }

            }

            if (logicalErrors.getErrorCount() > 0) {
                logicalErrorsList.add(logicalErrors);
            }
        }

        // Проверка наследования матриц
        for (String typeName : typesAccessMatrixes.keySet()){
            List<AccessMatrixConfig> typeAccessMatrixes = typesAccessMatrixes.get(typeName);
            // Валидацию выполняем только если более одной матрицы на тип
            if (typeAccessMatrixes.size() > 1){
                LogicalErrors logicalErrors = validateMatrixExtension(typeName, typeAccessMatrixes);
                if (logicalErrors.getErrorCount() > 0) {
                    logicalErrorsList.add(logicalErrors);
                }
            }
        }

        return logicalErrorsList;
    }

    /**
     * Логическая валидация расширения матриц
     * @param typeName
     * @param typeAccessMatrixes
     */
    private LogicalErrors validateMatrixExtension(String typeName, List<AccessMatrixConfig> typeAccessMatrixes) {
        LogicalErrors result = LogicalErrors.getInstance(typeName, "access-matrix");

        // Проверяем дублирование матриц в модуле
        Set<String> modulesWithMatrix = new HashSet<>();
        for (AccessMatrixConfig accessMatrixConfig : typeAccessMatrixes){
            if (!modulesWithMatrix.add(accessMatrixConfig.getModuleName().toLowerCase())){
                result.addError("Module " + accessMatrixConfig.getModuleName() +
                        " contains more then one access-matrix fore " + typeName);
            }
        }

        // Дальнейшая валидация строится на том, что в одном модуле может быть только одна матрица
        if (result.getErrorCount() == 0) {
            // Строим граф матриц
            MatrixGraph matrixGraph = new MatrixGraph(typeAccessMatrixes);
            // Проверяем нет ли нескольких корневых матриц
            // (корневая матрица такая, у которой в родительских модулях нет матриц для этого же типа)
            if (matrixGraph.getRootMatrixes().size() > 1){
                result.addError("Configurations contains more then one root access matrix for type " + typeName);
            }

            // Проверяем что корневая матрица разрешает наследование
            AccessMatrixConfig rootMatrix = matrixGraph.getRootMatrixes().get(0);
            if (rootMatrix.getExtendable() == null || !rootMatrix.getExtendable()){
                result.addError("Access matrix for type " + typeName + " in module " + rootMatrix.getModuleName() +
                        " is not extendable, but config contains another access matrix configs for this type");
            }

            // Проверяем что у корневой матрицы нет типа наследования
            if (rootMatrix.getExtendType() != null){
                result.addError("Access matrix for type " + typeName + " in module " + rootMatrix.getModuleName() +
                        " is root, but contains extend-type attribute");
            }

            // Проверяем что у не корневых матриц у всех есть атрибут extend-type
            for (AccessMatrixConfig matrixConfig : matrixGraph.getNotRootMatrixes()){
                if (matrixConfig.getExtendType() == null){
                    result.addError("Access matrix for type " + typeName + " in module " + rootMatrix.getModuleName() +
                            " is not root, but extend-type attribute is empty");
                }
            }

            // Проверяем что у всех дочерних матриц родительская матрица разрешает наследование
            for (AccessMatrixConfig matrixConfig : matrixGraph.getNotRootMatrixes()){
                List<AccessMatrixConfig> parentMatrixConfigs = matrixGraph.getParentMatrixes(matrixConfig.getModuleName());
                for (AccessMatrixConfig parentMatrixConfig : parentMatrixConfigs){
                    if (parentMatrixConfig.getExtendable() == null || !parentMatrixConfig.getExtendable()){
                        result.addError("Access matrix for type " + typeName + " in module " + rootMatrix.getModuleName() +
                                " is not root, but parent matrix is not marked as extendable");
                    }
                }
            }

            // Проверяем то, что если у хотя бы одной дочки тип наследования replace, то все матрицы должны быть в одной цепочке
            // Получаем информацию есть ли ветвления в матрице на любом уровне
            boolean hasMoreThenOneChild = hasMoreThenOneChildMatrix(matrixGraph, rootMatrix.getModuleName());
            // Если есть ветвления проверяем есть ли матрица с типом replace
            if (hasMoreThenOneChild) {
                for (AccessMatrixConfig childMatrixConfig : matrixGraph.getNotRootMatrixes()) {
                    if (childMatrixConfig.getExtendType() == AccessMatrixConfig.AccessMatrixExtendType.replace) {
                        result.addError("Access matrix for type " + typeName + " in module " + childMatrixConfig.getModuleName() +
                                " has extend-type='replace', therefore access matrix is uncertainty");
                    }
                }
            }
        }
        return result;
    }

    /**
     * Рекурсивная функция, проверяющая есть ли у переданого модуля, разные ветки дочерних модулей, которые переопределяют матрицу
     * @return
     */
    private boolean hasMoreThenOneChildMatrix(MatrixGraph matrixGraph, String module) {
        List<AccessMatrixConfig> children = matrixGraph.getChildMatrixes(module);
        boolean result = children.size() > 1;
        if (!result) {
            for (AccessMatrixConfig config : children) {
                result = result || hasMoreThenOneChildMatrix(matrixGraph, config.getModuleName());
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("There are more then one child found for the module {}. Children: {}", module, children);
            }
        }
        return result;
    }

    /**
     * Класс, описывапющий граф матриц
     */
    public class MatrixGraph {
        private Map<String, AccessMatrixConfig> matrixByModule = new HashMap<>();
        private Map<String, MatrixNode> matrixNodes = new HashMap<>();
        private Set<String> rootMatrix = new HashSet<>();

        public MatrixGraph(List<AccessMatrixConfig> typeAccessMatrixes){
            for (AccessMatrixConfig accessMatrixConfig : typeAccessMatrixes) {
                matrixByModule.put(accessMatrixConfig.getModuleName().toLowerCase(), accessMatrixConfig);
                matrixNodes.put(accessMatrixConfig.getModuleName().toLowerCase(),
                        new MatrixNode(accessMatrixConfig.getModuleName()));
            }

            // Строим зависимости
            for (ModuleConfiguration rootModule : moduleService.getRootModules()) {
                addChildModules(rootModule, null);
            }

            // Получаем корневые матрицы
            for (MatrixNode matrixNode :matrixNodes.values()) {
                if (matrixNode.parents.size() == 0){
                    rootMatrix.add(matrixNode.module.toLowerCase());
                }
            }
        }

        /**
         * Рекурсивная функция для построения зависимостей матриц
         * @param parentModule
         * @param parentMatrixNode
         */
        private void addChildModules(ModuleConfiguration parentModule, MatrixNode parentMatrixNode) {
            MatrixNode currentMatrixNode = matrixNodes.get(parentModule.getName().toLowerCase());
            if (currentMatrixNode == null) {
                currentMatrixNode = parentMatrixNode;
            }
            for (ModuleConfiguration childModules : moduleService.getChildModules(parentModule.getName())){
                if (currentMatrixNode != null) {
                    MatrixNode childMatrixNode = matrixNodes.get(childModules.getName().toLowerCase());
                    if (childMatrixNode != null) {
                        currentMatrixNode.children.add(childMatrixNode.module.toLowerCase());
                        childMatrixNode.parents.add(currentMatrixNode.module.toLowerCase());
                    }
                }
                addChildModules(childModules, currentMatrixNode);
            }
        }

        public List<AccessMatrixConfig> getRootMatrixes() {
            return rootMatrix.stream()
                    .map(item -> matrixByModule.get(item))
                    .collect(Collectors.toList());
        }

        public List<AccessMatrixConfig> getNotRootMatrixes() {
            List<AccessMatrixConfig> result = new ArrayList<>();
            for (AccessMatrixConfig config : matrixByModule.values()) {
                if (!rootMatrix.contains(config.getModuleName().toLowerCase())){
                    result.add(config);
                }
            }
            return result;
        }

        public List<AccessMatrixConfig> getChildMatrixes(String module) {
            if (matrixNodes.get(module.toLowerCase()) != null) {
                return matrixNodes.get(module.toLowerCase()).children.stream().map(item -> matrixByModule.get(item)).
                        collect(Collectors.toList());
            }else{
                return Collections.emptyList();
            }
        }

        public List<AccessMatrixConfig> getParentMatrixes(String module) {
            if (matrixNodes.get(module.toLowerCase()) != null) {
                return matrixNodes.get(module.toLowerCase()).parents.stream().map(item -> matrixByModule.get(item)).
                        collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        }
    }

    public class MatrixNode {
        private String module;
        private List<String> parents = new ArrayList<>();
        private List<String> children = new ArrayList<>();

        public MatrixNode(String module){
            this.module = module;
        }
    }
}
