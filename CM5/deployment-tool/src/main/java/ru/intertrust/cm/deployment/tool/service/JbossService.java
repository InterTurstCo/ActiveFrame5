package ru.intertrust.cm.deployment.tool.service;

import org.jboss.as.cli.CliInitializationException;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandLineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.deployment.tool.property.UpgradeProperties;
import ru.intertrust.cm.deployment.tool.util.ProcessPrintUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static ru.intertrust.cm.deployment.tool.config.AppConstants.APP_URL_PATTERN;
import static ru.intertrust.cm.deployment.tool.config.AppConstants.SERVER_URL_PATTERN;

/**
 * Created by Alexander Bogatyrenko on 28.07.16.
 * <p>
 * This class represents...
 */
@Service
public class JbossService {

    private static Logger logger = LoggerFactory.getLogger(JbossService.class);
    private static List<String> output = new ArrayList<>();
    @Autowired
    private UpgradeProperties props;

    @Autowired
    private Boolean isWindowsEnv;

    public void start() {
        try {
            String command = Paths.get(props.getJbossHome(), "bin", isWindowsEnv ? "standalone.bat" : "standalone.sh").toString();
            logger.info("Jboss start...");
            Process process = Runtime.getRuntime().exec(command);
            ProcessPrintUtil.printInBackground(process);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void stop() {
        try {
            Path path = Paths.get(props.getJbossHome(), "bin", isWindowsEnv ? "jboss-cli.bat" : "jboss-cli.sh");
            String command = path +
                    " --connect" +
                    " --controller=" + props.getJbossCliController() + ":" + props.getJbossCliPort() +
                    " --user=" + props.getJbossUser() +
                    " --password=" + props.getJbossPassword() +
                    " command=:shutdown";
            logger.info("Jboss stop...");
            Process process = Runtime.getRuntime().exec(command);
            ProcessPrintUtil.print(process);
            while (isServerStarted()) {
                try {
                    logger.info("Waiting jboss stopping 10 seconds");
                    Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            logger.info("Server has been stopped");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void waitAdminJbossStart() {
        while (!isServerStarted()) {
            try {
                logger.info("Waiting jboss admin panel starting 5 seconds");
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public boolean isServerStarted() {
        try {
            URL url = new URL(format(SERVER_URL_PATTERN, props.getJbossHost(), props.getJbossAdminPort()));
            Integer code = isUrlAvailable(url, "GET");
            if (code != null) {
                if (code.equals(200)) {
                    logger.info("Server is running");
                    return true;
                }
                if (code.equals(-1)) return false;
            }
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public Boolean isDeployed(String ear) {
        String deployment = Paths.get(props.getJbossHome(), "standalone", "deployments").toString();
        Path success = Paths.get(deployment, ear + ".ear.deployed");
        Path fail = Paths.get(deployment, ear + ".ear.failed");
        if (Files.exists(success)) {
            logger.info("Ear {} has been deployed successful", ear);
            return true;
        } else if (Files.exists(fail)) {
            logger.info("Ear {} deployment is failed", ear);
            return false;
        }
        return null;
    }

    public boolean isAppStarted() {
        try {
            URL url = new URL(format(APP_URL_PATTERN, props.getJbossHost(), props.getJbossAppPort(), props.getJbossAppUrl()));
            Integer code = isUrlAvailable(url, "GET");
            if (code != null) {
                if (code.equals(200)) return true;
                if (code.equals(-1)) return false;
                if (code.equals(404)) {
                    logger.info("The application not responding, url - {}, code - {}", url.toString(), code);
                    return false;
                }
            }
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    private Integer isUrlAvailable(URL url, String method) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.connect();
            return connection.getResponseCode();

        } catch (IOException e) {
            boolean serverDown = "Connection refused".equalsIgnoreCase(e.getMessage());
            if (serverDown) {
                return -1;
            } else {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    }

    public boolean isDeploySuccess(String ear) {
        while (isDeployed(ear) == null) {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        boolean started = isAppStarted();
        logger.info("The application {} started correctly", started ? "was" : "was not");
        return started;
    }

    public void undeployApplication(String name, String controllerAddress) {
        CommandContext ctx;
        try {
            PrintStream stdout = System.out;
            System.setOut(new MyFilterPrintStream(System.out));
            ctx = org.jboss.as.cli.CommandContextFactory.getInstance().newCommandContext();
            logger.info("Trying to connect on CLI API to "+controllerAddress);
            ctx.connectController(controllerAddress);
            ctx.handle("connect");
            ctx.handle("ls deployment");
            String dName = contains(name);
            if (dName != null) {
                ctx.handle("undeploy " + dName);
            }
            ctx.disconnectController();
            System.setOut(stdout);
        } catch (CliInitializationException e) {
            e.printStackTrace();
        } catch (CommandLineException e) {
            e.printStackTrace();
        }
    }

    class MyFilterPrintStream extends PrintStream {
        public MyFilterPrintStream(OutputStream out) {
            super(out);
        }

        @Override
        public void print(String s) {
            output.add(s);
            super.print(s);
        }
    }

    private String contains(String pattern) {
        for (String s : output) {
            if (s.contains(pattern))
                return s;
        }
        return null;
    }


}
