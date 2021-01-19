package ru.intertrust.cm.plugins;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.cli.*;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;
import ru.intertrust.cm.core.model.FatalException;

import javax.ejb.EJBContext;

public abstract class PlatformPluginBase implements PluginHandler {
    public static final String HELP_PARAM = "help";

    @Autowired
    private CollectionsService collectionsService;

    private Logger logger;
    private StringBuffer log = new StringBuffer();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss.SSS");
    private EJBContext context;
    private String param;
    private CommandLine commandLine;
    private Options options;
    
    public PlatformPluginBase(){
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public String execute(EJBContext context, String param) {
        info("Start Plugin " + this.getClass().getName() + " with param " + param);
        this.context = context;
        this.param = param;

        options = createOptions();
        CommandLineParser parser = new GnuParser();
        commandLine = null;

        try {
            commandLine = parser.parse(options, param.split("[ ]+"));
            execute();
            info("Finish Plugin " + this.getClass().getName());
        } catch (ParseException ex) {
            error("Parameter parsing failed", ex);
        } catch (Exception ex) {
            error("Plugin execution failed", ex);
        }

        return getLog();
    }

    protected abstract void execute() throws Exception;

    protected abstract Options createOptions();

    protected void info(String message, Object ... params){
        String formatedMessage = null;
        if (params != null){
            formatedMessage = MessageFormat.format(message, params);
        }else{
            formatedMessage = message;
        }
        
        logger.info(formatedMessage);
        log.append(dateFormat.format(new Date()) + " " + formatedMessage + "\n");
    }

    public CollectionsService getCollectionService() {
        return collectionsService;
    }

    protected void debug(String message, Object ... params){
        String formatedMessage = null;
        if (params != null){
            formatedMessage = MessageFormat.format(message, params);
        }else{
            formatedMessage = message;
        }
        logger.debug(formatedMessage);
    }

    protected void error(String message, Throwable ex){
        logger.error(message, ex);
        log.append(dateFormat.format(new Date()) + " " + message + "\n" + ExceptionUtils.getStackTrace(ex) + "\n");
    }

    protected CommandLine getCommandLine() {
        return commandLine;
    }

    protected String getLog(){
        return log.toString();
    }

    protected Options getOptions() {
        return options;
    }

    public EJBContext getContext() {
        return context;
    }

    public String getParam() {
        return param;
    }

    protected void logHelpMessage(String header) {
        HelpFormatter formatter = new HelpFormatter();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(stream);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, " ", header, options, HelpFormatter.DEFAULT_LEFT_PAD,
                HelpFormatter.DEFAULT_DESC_PAD, null, true);
        writer.flush();
        try {
            info(stream.toString("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new FatalException(ex);
        }
    }

    protected List<Id> getIdsFromQuery(String query) {
        IdentifiableObjectCollection collection = getCollectionService().findCollectionByQuery(query);
        List<Id> result = new ArrayList<Id>();
        for (IdentifiableObject row : collection) {
            result.add(row.getId());
        }
        return result;
    }

}
