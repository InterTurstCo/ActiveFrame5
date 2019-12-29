package ru.intertrust.cm.plugins.attachments;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.FileSystemAttachmentCleaner;
import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;
import ru.intertrust.cm.plugins.PluginBase;

import javax.ejb.EJBContext;

@Plugin(name = "FileSystemAttachmentCleanerPlugin",
        description = "Очистка файловой системы от не использующихся контентов. ВНИМАНИЕ!!!! Запускать только при наличие резервной копии каталогов вложений.",
        transactional = false)
public class FileSystemAttachmentCleanerPlugin extends PluginBase implements PluginHandler {

    @Autowired
    private FileSystemAttachmentCleaner cleaner;

    @Override
    public String execute(EJBContext context, String param) {
        String result = null;
        // Защита от случайного запуска.
        if (param.equalsIgnoreCase("clean")){
            result = cleaner.clean(context);
        }else{
            result = "Для выполнения очистки фаловой системы необходимо вызвать плагин с параметром clean";
        }

        return result;
    }
}
