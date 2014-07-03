package ru.intertrust.cm.remoteclient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;

/**
 * Базовый класс для удаленного клиента. Содержит методы для парсинга командной строки, получения удаленных сервисов и вывода в лог. Для использования
 * необходимо отнаследоватся и в методе main наследника вызвать метод execute. В переопределенном методе Execute необходимо вызвать базовый метод.
 * @author larin
 * 
 */
public abstract class ClientBase {
    private CommandLine commandLine;
    private Properties properties = new Properties();
    private StringBuilder log = new StringBuilder();
    private String address;
    private String user;
    private String password;
    private String logPath;
    /**
     * Контекст должен быть обязательно членам класса, и жить все время пока живет процесс. Инициализируется при получение первого сервиса и используется все
     * время только один экземпляр
     */
    private InitialContext ctx = null;
    private String ctxLogin = null;
    

    /**
     * Парсинг переданных параметров
     * @param args
     * @param argNames
     * @throws ParseException
     */
    private void parseArguments(String[] args, String[] argNames)
            throws ParseException {
        Options options = new Options();
        for (String argName : argNames) {
            Option option = new Option(argName, true, null);
            options.addOption(option);
        }

        CommandLineParser parser = new GnuParser();

        commandLine = parser.parse(options, args);
    }

    /**
     * Получение распарсенных параметров командной строки
     * @return
     */
    protected String getParamerer(String name) {
        String result = null;
        if (properties.getProperty(name) != null) {
            result = properties.getProperty(name);
        } else {
            result = commandLine.getOptionValue(name);
        }
        return result;
    }

    /**
     * Добавление записи в лог файл и одновременно в консоль
     * @param message
     */
    protected void log(String message) {
        System.out.println(message);
        log.append(message);
        log.append("\n");
    }

    /**
     * Метод вызывается из метода main наследников. Производит парсинг командной строки
     * @param args
     * @throws Exception
     */
    protected void execute(String[] args) throws Exception {
        execute(args, null);
    }

    /**
     * Метод вызывается из метода main наследников. Производит парсинг командной строки
     * @param args
     * @param optionNames
     * @throws Exception
     */
    protected void execute(String[] args, String[] optionNames) throws Exception {

        List<String> optionNamesList = new ArrayList<String>();
        optionNamesList.add("address");
        optionNamesList.add("user");
        optionNamesList.add("password");
        optionNamesList.add("log");
        if (optionNames != null) {
            for (int i = 0; i < optionNames.length; i++) {
                optionNamesList.add(optionNames[i]);
            }
        }

        parseArguments(args, optionNamesList.toArray(new String[optionNamesList.size()]));

        File propFile = new File("client.properties");
        if (propFile.exists()) {
            properties.load(new FileInputStream(propFile));
        }

        address = properties.getProperty("address") != null ? properties.getProperty("address") : commandLine.getOptionValue("address");
        user = properties.getProperty("user") != null ? properties.getProperty("user") : commandLine.getOptionValue("user");
        password = properties.getProperty("password") != null ? properties.getProperty("password") : commandLine.getOptionValue("password");
        logPath = properties.getProperty("log") != null ? properties.getProperty("log") : commandLine.getOptionValue("log");
    }

    /**
     * Получение удаленного сервиса
     * @param serviceName
     * @param remoteInterfaceClass
     * @return
     * @throws NamingException
     */
    protected Object getService(String serviceName, Class remoteInterfaceClass) throws NamingException {
        return getService(serviceName, remoteInterfaceClass, this.user, this.password);
    }

    protected Object getService(String serviceName, Class remoteInterfaceClass, String login, String password) throws NamingException {
        if (!login.equals(ctxLogin) && ctx != null){
            ctx.close();
            ctx = null;
        }
        
        if (ctx == null) {
            Properties jndiProps = new Properties();
            /*jndiProps.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.jboss.naming.remote.client.InitialContextFactory");
            jndiProps.put(Context.PROVIDER_URL, "remote://" + address);
            jndiProps.put("jboss.naming.client.ejb.context", "true");
            jndiProps
                    .put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT",
                            "false");
            jndiProps.put(Context.SECURITY_PRINCIPAL, login);
            jndiProps.put(Context.SECURITY_CREDENTIALS, password);*/

            
            Properties clientProperties = new Properties();
            clientProperties.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
            clientProperties.put("remote.connections", "default");
            clientProperties.put("remote.connection.default.port", "4447");
            clientProperties.put("remote.connection.default.host", "localhost");
            clientProperties.put("remote.connection.default.username", login);
            clientProperties.put("remote.connection.default.password", password);
            clientProperties.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
            clientProperties.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");

            EJBClientConfiguration ejbClientConfiguration = new PropertiesBasedEJBClientConfiguration(clientProperties);
            ContextSelector<EJBClientContext> contextSelector = new ConfigBasedEJBClientContextSelector(ejbClientConfiguration);
            EJBClientContext.setSelector(contextSelector);

            jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
            
            
            ctx = new InitialContext(jndiProps);
            ctxLogin = login;
        }

        //Object service = ctx.lookup("cm-sochi/web-app/" + serviceName + "!" + remoteInterfaceClass.getName());
        Object service = ctx.lookup("ejb:" + getAppName() + "/" + getModuleName() + "//" + serviceName + "!" + remoteInterfaceClass.getName());
        return service;

    }    
    
    protected String getAppName(){
        return "cm-sochi";
    }
    
    protected String getModuleName(){
        return "web-app";
    }
    /**
     * Запись лог файла. Необходимо вызывать в finally блоке переопределенного метода execute
     * @throws IOException
     */
    protected void writeLog() throws IOException {
        FileOutputStream out = null;
        try {
            if (logPath != null) {
                File logFile = new File(logPath);
                if (logFile.exists()){
                    logFile.delete();
                }
                out = new FileOutputStream(logFile);
                out.write(log.toString().getBytes());
            }
        } finally {
            out.close();
        }
    }

    /**
     * Получение аргументов командной строки
     * @return
     */
    protected String[] getCommandLineArgs() {
        return commandLine.getArgs();
    }

    protected void assertTrue(String message, boolean param) throws Exception {
        if (!param) {
            throw new Exception("[" + new Date() + "] " + message);
        }
        log(message + ": OK");
    }

    protected void assertFalse(String message, boolean param) throws Exception {
        if (param) {
            throw new Exception("[" + new Date() + "] " + message);
        }
        log(message + ": OK");
    }

    /**
     * Получение файла в виде массива байт
     * @param file
     * @return
     * @throws IOException
     */
    protected byte[] readFile(File file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } finally {
            input.close();
        }
    }
}
