package ru.intertrust.cm.plugins.attachments;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.FileSystemAttachmentCleaner;
import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.plugins.PlatformPluginBase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Plugin(name = "FileSystemAttachmentCleanerPlugin",
        description = "Очистка файловой системы от не использующихся контентов. ВНИМАНИЕ!!!! Запускать только при наличие резервной копии каталогов вложений.",
        transactional = false)
public class FileSystemAttachmentCleanerPlugin extends PlatformPluginBase{
    public static String CLEAN_PARAM = "clean";
    public static String FROM_PARAM = "from";
    public static String TO_PARAM = "to";

    public SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    public SimpleDateFormat logFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Autowired
    private FileSystemAttachmentCleaner cleaner;

    @Override
    public void execute() throws Exception {
        // Защита от случайного запуска.
        if (getCommandLine().hasOption(HELP_PARAM) || !getCommandLine().hasOption(CLEAN_PARAM)) {
            logHelpMessage("Plugin fore recalc ACL");
        } else {
            Date from = null;
            Date to = null;
            if (getCommandLine().hasOption(FROM_PARAM)) {
                from = format.parse(getCommandLine().getOptionValue(FROM_PARAM));
                info("From date {0}", logFormat.format(from));
            }
            if (getCommandLine().hasOption(TO_PARAM)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(format.parse(getCommandLine().getOptionValue(TO_PARAM)));
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                to = calendar.getTime();
                info("To date {0}", logFormat.format(to));
            }
            info(cleaner.clean(getContext(), from, to));
        }
    }

    @Override
    protected Options createOptions() {
        Options options = new Options();
        options.addOption(new Option(HELP_PARAM, "Prints this help message."));
        options.addOption(new Option(CLEAN_PARAM, false, "Flag to real delete attachments. If not exists plugin not started."));
        options.addOption(new Option(FROM_PARAM, true, "The beginning of the period for which attachments are deleted. Format: yyyy-MM-dd"));
        options.addOption(new Option(TO_PARAM, true, "The end of the period for which attachments are deleted. Format: yyyy-MM-dd"));
        return options;
    }
}
