package ru.intertrust.cm.plugins.attachments;

import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.plugins.PlatformPluginBase;

import javax.ejb.EJBContext;

@Plugin(name = "GenerateAttachmentsQuery",
        description = "Формирование запросов для анализа хранилища вложений",
        transactional = false)
public class GenerateAttachmentsQuery implements PluginHandler {

    @Autowired
    private ConfigurationExplorer configExplorer;

    @Override
    public String execute(EJBContext context, String param) {
        String result = "select count(*), sum(contentlength) from (\n";

        String[] attachmentsTypes = configExplorer.getAllAttachmentTypes();
        boolean firstRow = true;
        for (String attachmentsType : attachmentsTypes) {
            if (firstRow){
                firstRow = false;
            }else{
                result += "\tunion \n";
            }
            result += "\tselect created_date, contentlength, path, '" + attachmentsType + "' attachment_type, '"
                    + configExplorer.getAttachmentParentType(attachmentsType) + "' parent_type from " + attachmentsType + "\n";
        }
        result += ") t";

        return result;
    }
}
