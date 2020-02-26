package ru.intertrust.cm.plugins.attachments;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.FileSystemAttachmentCleaner;
import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.plugins.PluginBase;

import javax.ejb.EJBContext;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Plugin(name = "FileSystemAttachmentCleanerPlugin",
        description = "Очистка файловой системы от не использующихся контентов. ВНИМАНИЕ!!!! Запускать только при наличие резервной копии каталогов вложений.",
        transactional = false)
public class FileSystemAttachmentCleanerPlugin extends PluginBase implements PluginHandler {
    public static String CLEAN_PARAM = "clean";
    public static String FROM_PARAM = "from";
    public static String TO_PARAM = "to";

    public SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private FileSystemAttachmentCleaner cleaner;

    @Override
    public String execute(EJBContext context, String param) {
        try {
            String result = null;

            Map<String, String> paramMap = getParametersMap(param);
            // Защита от случайного запуска.
            if (paramMap.keySet().contains(CLEAN_PARAM)) {
                Date from = null;
                Date to = null;
                if (paramMap.keySet().contains(FROM_PARAM)) {
                    from = format.parse(paramMap.get(FROM_PARAM));
                }
                if (paramMap.keySet().contains(TO_PARAM)) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(format.parse(paramMap.get(TO_PARAM)));
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    calendar.set(Calendar.MILLISECOND, 999);
                    to = calendar.getTime();
                }
                result = cleaner.clean(context, from, to);
            } else {
                result = "Для выполнения очистки фаловой системы необходимо вызвать плагин с параметром clean";
            }

            return result;
        } catch (Exception ex) {
            throw new FatalException("Error execute File System Attachment Cleaner Plugin", ex);
        }
    }
}
