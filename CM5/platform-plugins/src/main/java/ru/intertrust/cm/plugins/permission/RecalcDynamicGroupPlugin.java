package ru.intertrust.cm.plugins.permission;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.core.config.DynamicGroupConfig;
import ru.intertrust.cm.core.dao.access.DynamicGroupService;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.plugins.PlatformPluginBase;

import javax.transaction.Status;
import java.util.*;

@Plugin(description = "Плагин инициирует пересчет всех динамических групп для объектов. Запуск без параметров выводит в лог справку.",
        name = "RecalcDynamicGroupPlugin",
        transactional = false)
public class RecalcDynamicGroupPlugin extends PlatformPluginBase {
    public static final String SQL_PARAM = "sql";
    public static final String HELP_PARAM = "help";
    public static final String GROUP_NAME_PARAM = "groupname";
    public static final String TYPE_PARAM = "type";

    @Autowired
    private DynamicGroupService dynamicGroupService;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeCache;

    private Map<String, Set<String>> typeGroupNames = new HashMap<String, Set<String>>();

    @Override
    protected Options createOptions() {
        Options options = new Options();
        options.addOption(new Option(HELP_PARAM, "Prints this help message."));
        Option sql = new Option(SQL_PARAM, true,
                "A query that returns the identifiers of domain objects, which are contexts for dynamic groups.");
        sql.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(sql);
        options.addOption(new Option(GROUP_NAME_PARAM, true,
                "The names of the dynamic groups, separated by commas, that you want to calculate. If parameter not specified recalc all dynamic groups fore context object."));
        options.addOption(new Option(TYPE_PARAM, true,
                "Types of domain objects, separated by commas, context groups of which should be recalculated. If the \"sql\" parameter is specified, this parameter is ignored."));
        return options;
    }

    @Override
    protected void execute() throws Exception {
        if (getCommandLine().hasOption(HELP_PARAM) ||
                (!getCommandLine().hasOption(SQL_PARAM) && !getCommandLine().hasOption(TYPE_PARAM))) {
            logHelpMessage("Plugin fore recalc dynamic groups");
        } else {
            // Получаем идентификаторы контекстов
            List<Id> contextIds;
            if (getCommandLine().hasOption(SQL_PARAM)) {
                String[] queryArray = getCommandLine().getOptionValues(SQL_PARAM);
                String query = "";
                for (int i = 0; i < queryArray.length ; i++) {
                    query += queryArray[i] + " ";
                }
                info("SQL: " + query);
                contextIds = getIdsFromQuery(query);
            } else {
                String types = getCommandLine().getOptionValue(TYPE_PARAM);
                String[] typeArray = types.split("[,;]");
                contextIds = new ArrayList<Id>();
                for (String typeName : typeArray) {
                    contextIds.addAll(getIdsFromQuery("select id from " + typeName));
                }
            }

            // Получаем группы, которые надо пересчитать, из параметров
            Set<String> groupNames = null;
            boolean useFixetGroupList = false;
            if (getCommandLine().hasOption(GROUP_NAME_PARAM)) {
                useFixetGroupList = true;
                groupNames = new HashSet<String>();
                String groupNameParam = getCommandLine().getOptionValue(GROUP_NAME_PARAM);
                String[] groupNameArray = groupNameParam.split("[,;]");
                for (String groupName : groupNameArray) {
                    groupNames.add(groupName.trim());
                }
            }

            // Пересчитываем группы каждого контекста
            long count = 0;
            for (Id contextId : contextIds) {
                if (!useFixetGroupList) {
                    groupNames = getRecalcGroupNames(contextId);
                }

                for (String groupName : groupNames) {
                    try {
                        getContext().getUserTransaction().begin();
                        dynamicGroupService.recalcGroup(groupName, contextId);
                        info("Recalc Group " + groupName + " for " + contextId.toStringRepresentation());
                        count++;
                        getContext().getUserTransaction().commit();
                    }catch (Exception ex) {
                        if (getContext().getUserTransaction().getStatus() == Status.STATUS_ACTIVE) {
                            getContext().getUserTransaction().rollback();
                        }
                    }
                }
            }

            info("Recalc " + count + " groups");
        }
    }

    /**
     * Получение имен всех динамических групп, которые настроены для
     * контекстного объекта
     * @param contextId
     * @return
     */
    private Set<String> getRecalcGroupNames(Id contextId) {
        String domainObjectType = domainObjectTypeCache.getName(contextId);
        Set<String> result = typeGroupNames.get(domainObjectType.toLowerCase());
        if (result == null) {
            result = new HashSet<String>();
            List<DynamicGroupConfig> groupConfigs = dynamicGroupService.getTypeDynamicGroupConfigs(domainObjectType);
            for (DynamicGroupConfig dynamicGroupConfig : groupConfigs) {
                result.add(dynamicGroupConfig.getName());
            }
            typeGroupNames.put(domainObjectType.toLowerCase(), result);
        }
        return result;
    }
}
