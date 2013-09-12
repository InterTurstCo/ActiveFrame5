package ru.intertrust.cm.performance.dataset.client;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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

import ru.intertrust.cm.performance.dataset.DatasetGenerationService;

/**
 * 
 * @author erentsov
 *
 */
public class DatasetGenerationServiceClient {

    /**
     * @param args
     * @throws NamingException 
     * @throws ParseException 
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        String[] argNames = new String[]{"address", "user", "password", "log"};
        Options options = new Options();
        for(String argName : argNames){
            Option option = new Option(argName, true, null);
            if(!argName.equals("log")){
                option.setRequired(true);
            } else{
                option.setType(Boolean.class);
            }
            options.addOption(option);
        }
        
        CommandLineParser parser = new GnuParser();

        try{
            CommandLine cmd = parser.parse(options, args);       
            String address = cmd.getOptionValue("address");
            String user = cmd.getOptionValue("user");
            String password = cmd.getOptionValue("password");
            Boolean log = (Boolean) cmd.getParsedOptionValue("log");
            boolean isLog = log != null ? log : false;
            String xml = cmd.getArgs().length == 0 ? null : cmd.getArgs()[0];
            if(xml == null) return;
        
            Files.readAllBytes(FileSystems.getDefault().getPath(xml));
            
            Properties jndiProps = new Properties();
            jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
            jndiProps.put(Context.PROVIDER_URL,"remote://" + address);
            jndiProps.put("jboss.naming.client.ejb.context", true);
            jndiProps.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");        
            jndiProps.put(Context.SECURITY_PRINCIPAL, user);
            jndiProps.put(Context.SECURITY_CREDENTIALS, password);
            jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
            
            Context ctx = new InitialContext(jndiProps);
            
            DatasetGenerationService.Remote service = (DatasetGenerationService.Remote) 
                ctx.lookup(
                    "java:cm-sochi-1.0-SNAPSHOT/web-app/DatasetGenerationServiceImpl!ru.intertrust.cm.performance.dataset.DatasetGenerationService$Remote"
            );
            
            byte[] data = Files.readAllBytes(FileSystems.getDefault().getPath(xml));
            
            String result = service.execute(data);
            System.out.println(result);
            if(isLog){
                Path logPath = FileSystems.getDefault().getPath("log.txt");
                Files.deleteIfExists(logPath);
                Files.createFile(logPath);
                Files.write(logPath, result.getBytes());
            }
        } catch(ParseException exception){
            System.out.println(exception.getMessage());
        }  catch (NamingException e) {         
            e.printStackTrace();
        }
    }

}
