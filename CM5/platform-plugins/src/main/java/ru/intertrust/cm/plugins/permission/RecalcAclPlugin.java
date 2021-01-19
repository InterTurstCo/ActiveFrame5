package ru.intertrust.cm.plugins.permission;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.plugins.PlatformPluginBase;

import java.util.ArrayList;
import java.util.List;

@Plugin(description = "Плагин инициирует пересчет ACL на доменные объекты.",
        name = "RecalcAclPlugin",
        transactional = false)
public class RecalcAclPlugin extends PlatformPluginBase {
    public static final String SQL_PARAM = "sql";
    public static final String HELP_PARAM = "help";
    public static final String TYPE_PARAM = "type";

    @Autowired
    private PermissionService permissionService;

    @Override
    protected void execute() throws Exception {
        if (getCommandLine().hasOption(HELP_PARAM) ||
                (!getCommandLine().hasOption(SQL_PARAM) && !getCommandLine().hasOption(TYPE_PARAM))) {
            logHelpMessage("Plugin fore recalc ACL");
        } else {
            // Получаем идентификаторы контекстов
            List<Id> contextIds;
            if (getCommandLine().hasOption(SQL_PARAM)) {
                String[] queryArray = getCommandLine().getOptionValues(SQL_PARAM);
                String query = "";
                for (int i = 0; i < queryArray.length ; i++) {
                    query += queryArray[i] + " ";
                }
                info("SQL:  " + query);
                contextIds = getIdsFromQuery(query);
            } else {
                String types = getCommandLine().getOptionValue(TYPE_PARAM);
                String[] typeArray = types.split("[,;]");
                contextIds = new ArrayList<Id>();
                for (String typeName : typeArray) {
                    contextIds.addAll(getIdsFromQuery("select id from " + typeName));
                }
            }

            // Пересчитываем права
            long count = 0;
            for (Id contextId : contextIds) {
                permissionService.refreshAclFor(contextId);
                info("Recalc ACL fore " + contextId.toStringRepresentation());
                count++;
            }
            info("Recalc " + count + " ACLs");
        }
    }

    @Override
    protected Options createOptions() {
        Options options = new Options();
        options.addOption(new Option(HELP_PARAM, "Prints this help message."));
        Option sql = new Option(SQL_PARAM, true,
                "A query that returns the identifiers of domain objects fore recalc ACL.");
        sql.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(sql);
        options.addOption(new Option(TYPE_PARAM, true, "Types of domain objects, separated by commas, ACLs of which should be recalculated. If the \"sql\" parameter is specified, this parameter is ignored."));
        return options;
    }

}
