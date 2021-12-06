package ru.intertrust.cm.plugins.permission;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.access.*;
import ru.intertrust.cm.plugins.PlatformPluginBase;

import java.util.Collection;

@Plugin(description = "Плагин удаляет записи в таблице *_acl у тех типов, где права не зависят от контекста",
        name = "CleanAclPlugin",
        transactional = false)
public class CleanAclPlugin extends PlatformPluginBase {

    public static final String CLEAN_PARAM = "clean";

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcOperations jdbcTemplate;

    @Autowired
    private PermissionServiceDao permissionServiceDao;

    @Override
    protected void execute() throws Exception {
        if (getCommandLine().hasOption(HELP_PARAM)) {
            logHelpMessage("Plugin clean unnecessary acl rows");
        }else {
            Collection<AccessMatrixConfig> accessMatrixConfigs = configurationExplorer.getConfigs(AccessMatrixConfig.class);
            for (AccessMatrixConfig accessMatrixConfig : accessMatrixConfigs) {
                if (accessMatrixConfig.getMatrixReference() == null
                        || (accessMatrixConfig.getMatrixReference() != null &&
                        (accessMatrixConfig.getBorrowPermissisons() == AccessMatrixConfig.BorrowPermissisonsMode.none
                                || accessMatrixConfig.getBorrowPermissisons() == AccessMatrixConfig.BorrowPermissisonsMode.read))) {

                    // Проверка на то что "все" статусы в матрице безконтекстные
                    // Можно было бы усложнить и искать только те объекты, у которых в текущем статусе настройки бехконтекстные,
                    // но это сильно усложнит и замедлит работу плагина. К тому же я не помню, что такие матрицы вообще есть,
                    // поэтому так не делаем
                    if (isWithoutContextPermissionConfig(accessMatrixConfig)) {
                        if (getCommandLine().hasOption(CLEAN_PARAM)) {
                            info("Start clean {0} acl", accessMatrixConfig.getType());
                            // Для найленных типов записи xxx_acl не нужны, удаляем их
                            int count = jdbcTemplate.update("delete from " + accessMatrixConfig.getType() + "_acl");
                            info("End clean {0} acl. Delete {1} rows", accessMatrixConfig.getType(), count);
                        } else {
                            info("Start analyze {0} acl", accessMatrixConfig.getType());
                            // Для найленных типов записи xxx_acl не нужны, удаляем их
                            long count = 0;
                            if (getOptions().hasOption(CLEAN_PARAM)) {
                                count = jdbcTemplate.update("delete from " + accessMatrixConfig.getType() + "_acl");
                            } else {
                                count = jdbcTemplate.queryForObject(
                                        "select count(*) from " + accessMatrixConfig.getType() + "_acl", Long.class);
                            }
                            info("End analyze {0} acl. Find {1} unnecessary rows", accessMatrixConfig.getType(), count);
                        }
                    }
                }
            }
        }
    }

    /**
     * Проверка что в матрице не используются контекстные настройки.
     * Проверяются все. кроме READ, так как read всегда хранится, удалять его нельзя.
     * Кроме того метот анализирует наличие настройки прав, если настройки прав нет то и удалять нечего
     * @param accessMatrixConfig
     * @return
     */
    private boolean isWithoutContextPermissionConfig(AccessMatrixConfig accessMatrixConfig) {
        // Поиск хотя бы одной настройки прав, которая зависит от контекста, если находится сразу возвращается false
        int size = 0;
        for (AccessMatrixStatusConfig accessMatrixStatusConfig : accessMatrixConfig.getStatus()){
            for (BaseOperationPermitConfig permitConfig : accessMatrixStatusConfig.getPermissions()) {
                AccessType accessType = getAccessType(permitConfig);
                if (accessType != null) {
                    if (!permissionServiceDao.isWithoutContextPermissionConfig(accessMatrixStatusConfig, accessType)) {
                        return false;
                    }else{
                        size++;
                    }
                }
            }
        }

        // не нашли настроек которые зависят от контекста, проверяем есть ли в принципе настройки прав, отличных от READ
        if (size > 0) {
            return true;
        }else{
            return false;
        }
    }

    /**
     * Получение AccessType исходя из настроек.
     * Если проверяется DomainObjectAccessType.READ - возвращается false& так как эту настройку игнорируем при анализе
     * @param permitConfig
     * @return
     */
    private AccessType getAccessType(BaseOperationPermitConfig permitConfig) {
        AccessType result = null;
        if (permitConfig instanceof WriteConfig){
            result = DomainObjectAccessType.WRITE;
        }else if(permitConfig instanceof DeleteConfig){
            result = DomainObjectAccessType.DELETE;
        }else if(permitConfig instanceof CreateChildConfig){
            result = new CreateChildAccessType(((CreateChildConfig)permitConfig).getType());
        }else if(permitConfig instanceof ExecuteActionConfig){
            result = new ExecuteActionAccessType(((ExecuteActionConfig)permitConfig).getName());
        }else if(permitConfig instanceof ReadAttachmentConfig){
            result = DomainObjectAccessType.READ_ATTACH;
        }
        return result;
    }

    @Override
    protected Options createOptions() {
        Options options = new Options();
        options.addOption(new Option(HELP_PARAM, "Prints this help message."));
        options.addOption(new Option(CLEAN_PARAM,
                "Flag for enabling the cleaning mode. " +
                        "Without this flag, the plugin will output the information to the log, " +
                        "but it will not actually delete it."));
        return options;
    }
}
