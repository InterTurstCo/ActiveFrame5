package ru.intertrust.cm.core.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import static org.mockito.Matchers.anyString;

import java.util.*;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessMatrixExtensionTest {

    @Mock
    private ModuleService moduleService;

    @Before
    public void init(){
        // Формируем следующую структуру модулей:
        //
        //            module3
        //         /
        // module1
        //         \
        //            module4
        //         /
        // module2
        //         \
        //            module5
        //                    \
        //                      module6
        //

        List<ModuleConfiguration> modules = new ArrayList<>();
        ModuleConfiguration module1 = new ModuleConfiguration("module1");
        modules.add(module1);
        ModuleConfiguration module2 = new ModuleConfiguration("module2");
        modules.add(module2);
        ModuleConfiguration module3 = new ModuleConfiguration("module3", "module1");
        modules.add(module3);
        ModuleConfiguration module4 = new ModuleConfiguration("module4", "module1", "module2");
        modules.add(module4);
        ModuleConfiguration module5 = new ModuleConfiguration("module5", "module2");
        modules.add(module5);
        ModuleConfiguration module6 = new ModuleConfiguration("module6", "module5");
        modules.add(module6);
        when(moduleService.getModuleList()).thenReturn(modules);

        when(moduleService.getRootModules()).thenReturn(Arrays.asList(module1, module2));

        when(moduleService.getChildModules(anyString())).thenAnswer( invocation-> {
            Object[] arguments = invocation.getArguments();
            if (arguments[0].equals("module1")){
                return Arrays.asList(module3, module4);
            }else if (arguments[0].equals("module2")){
                return Arrays.asList(module4, module5);
            }else if (arguments[0].equals("module5")){
                return Arrays.asList(module6);
            }
            return Collections.emptyList();
        });
    }

    private ConfigurationExplorer createConfigurationExplorer(Configuration configuration){

        // Добавляем GlobalSettings к конфигурации если еще нет
        boolean hasGlobalConfig = false;
        for (TopLevelConfig topConfig : configuration.getConfigurationList()){
            if (topConfig instanceof GlobalSettingsConfig){
                hasGlobalConfig = true;
                break;
            }
        }

        if (!hasGlobalConfig) {
            GlobalSettingsConfig globalSettings = new GlobalSettingsConfig();
            configuration.getConfigurationList().add(globalSettings);
        }

        // Формируем ConfigurationExplorer
        ConfigurationExplorerImpl configExplorer = new ConfigurationExplorerImpl(configuration);
        ReflectionTestUtils.setField(configExplorer, "moduleService", moduleService);
        configExplorer.init();
        configExplorer.validate();
        return configExplorer;
    }

    @Test
    public void nullMatrixTest(){
        Configuration configuration = new Configuration();
        DomainObjectTypeConfig type1Config = new DomainObjectTypeConfig();
        type1Config.setName("type1");
        configuration.getConfigurationList().add(type1Config);
        DomainObjectTypeConfig type2Config = new DomainObjectTypeConfig();
        type2Config.setName("type2");
        configuration.getConfigurationList().add(type2Config);

        AccessMatrixConfig accessConfig = new AccessMatrixConfig();
        accessConfig.setType("type1");
        AccessMatrixStatusConfig statusConfig = new AccessMatrixStatusConfig();
        statusConfig.setName("status1");
        statusConfig.getPermissions().add(new ReadConfig(Arrays.asList(new PermitGroup("group1")), false));
        accessConfig.getStatus().add(statusConfig);
        configuration.getConfigurationList().add(accessConfig);

        ConfigurationExplorer configExplorer = createConfigurationExplorer(configuration);
        assertNull(configExplorer.getAccessMatrixByObjectType("type2"));
        assertNull(configExplorer.getAccessMatrixByObjectTypeAndStatus("type2", "status1"));

        assertNotNull(configExplorer.getAccessMatrixByObjectType("type1"));
        assertNotNull(configExplorer.getAccessMatrixByObjectTypeAndStatus("type1", "status1"));
    }

    @Test
    public void testMergeMatrix(){
        Configuration configuration = new Configuration();
        DomainObjectTypeConfig type1Config = new DomainObjectTypeConfig();
        type1Config.setName("type1");
        configuration.getConfigurationList().add(type1Config);

        AccessMatrixConfig access1Config = new AccessMatrixConfig();
        access1Config.setType("type1");
        access1Config.setModuleName("module1");
        access1Config.setExtendable(true);
        AccessMatrixStatusConfig status1Config = new AccessMatrixStatusConfig();
        status1Config.setName("status1");
        status1Config.getPermissions().add(new ReadConfig(Arrays.asList(new PermitGroup("group1")), false));
        access1Config.getStatus().add(status1Config);
        configuration.getConfigurationList().add(access1Config);

        AccessMatrixConfig access2Config = new AccessMatrixConfig();
        access2Config.setType("type1");
        access2Config.setModuleName("module3");
        access2Config.setExtendType(AccessMatrixConfig.AccessMatrixExtendType.extend);
        AccessMatrixStatusConfig status2Config = new AccessMatrixStatusConfig();
        status2Config.setName("status1");
        status2Config.getPermissions().add(new ReadConfig(Arrays.asList(new PermitGroup("group2")), false));
        access2Config.getStatus().add(status2Config);
        configuration.getConfigurationList().add(access2Config);

        ConfigurationExplorer configExplorer = createConfigurationExplorer(configuration);

        assertNotNull(configExplorer.getAccessMatrixByObjectType("type1"));
        AccessMatrixStatusConfig checkStatus1Config = configExplorer.getAccessMatrixByObjectTypeAndStatus(
                "type1", "status1");
        assertNotNull(checkStatus1Config);
        assertTrue(checkStatus1Config.getName().equals("status1"));
        assertTrue(checkStatus1Config.getPermissions().size() == 1);
        assertTrue(checkStatus1Config.getPermissions().get(0) instanceof ReadConfig);
        assertTrue(((ReadConfig)checkStatus1Config.getPermissions().get(0)).getPermitConfigs().size() == 2);
        assertTrue(((ReadConfig)checkStatus1Config.getPermissions().get(0)).getPermitConfigs().contains(new PermitGroup("group1")));
        assertTrue(((ReadConfig)checkStatus1Config.getPermissions().get(0)).getPermitConfigs().contains(new PermitGroup("group2")));
    }

    @Test
    public void testReplaceMatrix(){
        Configuration configuration = new Configuration();
        DomainObjectTypeConfig type1Config = new DomainObjectTypeConfig();
        type1Config.setName("type1");
        configuration.getConfigurationList().add(type1Config);

        AccessMatrixConfig access1Config = new AccessMatrixConfig();
        access1Config.setType("type1");
        access1Config.setModuleName("module1");
        access1Config.setExtendable(true);
        AccessMatrixStatusConfig status1Config = new AccessMatrixStatusConfig();
        status1Config.setName("status1");
        status1Config.getPermissions().add(new ReadConfig(Arrays.asList(new PermitGroup("group1")), false));
        access1Config.getStatus().add(status1Config);
        configuration.getConfigurationList().add(access1Config);

        AccessMatrixConfig access2Config = new AccessMatrixConfig();
        access2Config.setType("type1");
        access2Config.setModuleName("module3");
        access2Config.setExtendType(AccessMatrixConfig.AccessMatrixExtendType.replace);
        AccessMatrixStatusConfig status2Config = new AccessMatrixStatusConfig();
        status2Config.setName("status1");
        status2Config.getPermissions().add(new ReadConfig(Arrays.asList(new PermitGroup("group2")), false));
        access2Config.getStatus().add(status2Config);
        configuration.getConfigurationList().add(access2Config);

        ConfigurationExplorer configExplorer = createConfigurationExplorer(configuration);

        assertNotNull(configExplorer.getAccessMatrixByObjectType("type1"));
        AccessMatrixStatusConfig checkStatus1Config = configExplorer.getAccessMatrixByObjectTypeAndStatus(
                "type1", "status1");
        assertNotNull(checkStatus1Config);
        assertTrue(checkStatus1Config.getName().equals("status1"));
        assertTrue(checkStatus1Config.getPermissions().size() == 1);
        assertTrue(checkStatus1Config.getPermissions().get(0) instanceof ReadConfig);
        assertTrue(((ReadConfig)checkStatus1Config.getPermissions().get(0)).getPermitConfigs().size() == 1);
        assertTrue(((ReadConfig)checkStatus1Config.getPermissions().get(0)).getPermitConfigs().contains(new PermitGroup("group2")));
    }

    @Test
    public void testValidateAccessMatrixExtensions(){
        Configuration configuration = new Configuration();
        DomainObjectTypeConfig type1Config = new DomainObjectTypeConfig();
        type1Config.setName("type1");
        configuration.getConfigurationList().add(type1Config);

        AccessMatrixConfig access1Config = new AccessMatrixConfig();
        access1Config.setType("type1");
        access1Config.setModuleName("module1");
        AccessMatrixStatusConfig status1Config = new AccessMatrixStatusConfig();
        status1Config.setName("status1");
        status1Config.getPermissions().add(new ReadConfig(Arrays.asList(new PermitGroup("group1")), false));
        access1Config.getStatus().add(status1Config);
        configuration.getConfigurationList().add(access1Config);

        AccessMatrixConfig access2Config = new AccessMatrixConfig();
        access2Config.setType("type1");
        access2Config.setModuleName("module1");
        AccessMatrixStatusConfig status2Config = new AccessMatrixStatusConfig();
        status2Config.setName("status1");
        status2Config.getPermissions().add(new ReadConfig(Arrays.asList(new PermitGroup("group2")), false));
        access2Config.getStatus().add(status2Config);
        configuration.getConfigurationList().add(access2Config);

        checkConfigValidation(configuration, "Две матрицы в одном модуле", "There are top level configurations with identical name");

        access2Config.setModuleName("module2");
        checkConfigValidation(configuration, "Две корневых матрицы 1", "more then one root access matrix");

        access1Config.setModuleName("module4");
        access2Config.setModuleName("module5");
        checkConfigValidation(configuration, "Две корневых матрицы 2", "more then one root access matrix");

        access1Config.setModuleName("module2");
        access2Config.setModuleName("module5");
        checkConfigValidation(configuration, "Корневая матрица не помечена как расширяемая", "is not extendable, but config contains another access matrix");

        access1Config.setExtendable(true);
        access1Config.setExtendType(AccessMatrixConfig.AccessMatrixExtendType.extend);
        checkConfigValidation(configuration, "Корневая матрица имеет атрибут extend-type", "is root, but contains extend-type attribute");

        access1Config.setExtendType(null);
        checkConfigValidation(configuration, "Дочерняя матрица не имеет атрибут extend-type", "is not root, but extend-type attribute is empty");

        access2Config.setExtendType(AccessMatrixConfig.AccessMatrixExtendType.extend);
        AccessMatrixConfig access3Config = new AccessMatrixConfig();
        access3Config.setType("type1");
        access3Config.setModuleName("module6");
        access3Config.setExtendType(AccessMatrixConfig.AccessMatrixExtendType.replace);
        AccessMatrixStatusConfig status3Config = new AccessMatrixStatusConfig();
        status3Config.setName("status1");
        status3Config.getPermissions().add(new ReadConfig(Arrays.asList(new PermitGroup("group3")), false));
        access3Config.getStatus().add(status3Config);
        configuration.getConfigurationList().add(access3Config);
        checkConfigValidation(configuration, "У промежуточной матрицы не установлен флаг расширяемый", "is not root, but parent matrix is not marked as extendable");

        access2Config.setExtendable(true);
        AccessMatrixConfig access4Config = new AccessMatrixConfig();
        access4Config.setType("type1");
        access4Config.setModuleName("module4");
        access4Config.setExtendType(AccessMatrixConfig.AccessMatrixExtendType.replace);
        AccessMatrixStatusConfig status4Config = new AccessMatrixStatusConfig();
        status4Config.setName("status1");
        status4Config.getPermissions().add(new ReadConfig(Arrays.asList(new PermitGroup("group4")), false));
        access4Config.getStatus().add(status4Config);
        configuration.getConfigurationList().add(access4Config);
        checkConfigValidation(configuration, "Неопределенность в получение окончательной матрицы", "has extend-type='replace', therefore access matrix is uncertainty");

        access4Config.setExtendType(AccessMatrixConfig.AccessMatrixExtendType.extend);
        access3Config.setExtendType(AccessMatrixConfig.AccessMatrixExtendType.extend);
        // Все ошибки валидации поправлены, конфигурация должна загрузится
        createConfigurationExplorer(configuration);
    }

    /**
     * Проверка корректности валидации
     * @param configuration конфигурация
     * @param validationType тип валидации, нужен для того чтоб различить вызовы, произвольная строка, попадет в лог
     * @param exceptionTextFragment фрагмент текста ошибки, чтоб удостоверится что сформировано корректное исключение
     */
    private void checkConfigValidation(Configuration configuration, String validationType, String exceptionTextFragment){
        try {
            createConfigurationExplorer(configuration);
            assertTrue(validationType + ", не сформировано исключение", false);
        }catch (Exception ex){
            if (ex.getCause() instanceof ConfigurationException){
                // Правильное исключение, сравниваем текст в ошибке
                if (!ex.getCause().getMessage().contains(exceptionTextFragment)){
                    assertTrue(validationType + ", некорректный текст исключения", false);
                }
            }else {
                assertTrue(validationType + ", некорректное исключение", false);
            }
        }

    }
}
