package ru.intertrust.performance.gwtrpcproxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.DefaultHttpClientIODispatch;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.protocol.HttpAsyncRequester;
import org.apache.http.nio.protocol.UriHttpAsyncRequestHandlerMapper;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.control.gui.TransactionControllerGui;
import org.apache.jmeter.extractor.BeanShellPostProcessor;
import org.apache.jmeter.modifiers.BeanShellPreProcessor;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.sampler.TestAction;
import org.apache.jmeter.sampler.gui.TestActionGui;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.performance.jmetertools.GwtProcySerializationPolicyProvider;
import ru.intertrust.performance.jmetertools.GwtRpcHttpTestSampleGui;
import ru.intertrust.performance.jmetertools.GwtRpcSampler;
import ru.intertrust.performance.jmetertools.HttpUploadSampleGui;

public class GwtRpcProxy implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(GwtRpcProxy.class);

    private String targetUri;
    private int localPort;
    private String outFile;
    private Integer automaticGroupDetectInterval;
    private GroupStrategy groupStrategy;
    private Thread serverThread;
    private ProxyContext context = new ProxyContext();
    private ConnectingIOReactor connectingIOReactor;
    private Integer betweenGroupPause;

    public enum GroupStrategy {
        AUTOMATIC,
        MANUAL
    }

    public static void main(String[] args) {
        Options options = new Options();
        try {
            //Парсим командную строку
            CommandLine commandLine;
            CommandLineParser parser = new GnuParser();

            options.addOption("t", "target-uri", true, "Server URI. Required.");
            options.addOption("p", "local-port", true, "Local port. Optional. Default value: 8090.");
            options.addOption("f", "output-file", true, "Output script file. Optional. Default value: Result.jmx.");
            options.addOption("d", "group-detect-interval", true, "Time interval, after that start new group. Optional. Default value: 1 sec.");
            options.addOption("s", "group-pause", true, "Between group pauthes. Optional. Default value: 1 sec.");
            options.addOption("h", "help", false, "Show help");

            commandLine = parser.parse(options, args, true);

            String targetUri;
            int localPort = 8090;
            String outFile = "Result.jmx";
            int automaticGroupDetectInterval = 1;
            int betweenGroupPause = 1;

            if (commandLine.hasOption("target-uri")) {
                targetUri = commandLine.getOptionValue("target-uri");
            } else {
                throw new ParseException("target-uri parameter is required");
            }
            if (commandLine.hasOption("local-port")) {
                localPort = Integer.parseInt(commandLine.getOptionValue("local-port"));
            }
            if (commandLine.hasOption("output-file")) {
                outFile = commandLine.getOptionValue("output-file");
            }
            if (commandLine.hasOption("group-detect-interval")) {
                automaticGroupDetectInterval = Integer.parseInt(commandLine.getOptionValue("group-detect-interval"));
            }
            if (commandLine.hasOption("group-pause")) {
                betweenGroupPause = Integer.parseInt(commandLine.getOptionValue("group-pause"));
            }
            if (commandLine.hasOption("help")) {
                showHelp(options);
                System.exit(0);
            }

            final GwtRpcProxy gwtRpcProxy =
                    new GwtRpcProxy(targetUri, localPort, outFile, GroupStrategy.AUTOMATIC, automaticGroupDetectInterval, betweenGroupPause);

            System.out.println("Press CTRL+C for terminate");

            //Shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    logger.info("Running Shutdown Hook");
                    try {
                        gwtRpcProxy.stop();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            gwtRpcProxy.start();
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
            System.out.println("");
            showHelp(options);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void showHelp(Options options){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -cp gwt-rpc-proxy.jar;lib/* ru.intertrust.performance.gwtrpcproxy.GwtRpcProxy <options>", options);
    }
    
    public GwtRpcProxy(String targetUri, int localPort, String outFile, GroupStrategy groupStrategy, Integer automaticGroupDetectInterval,
            Integer betweenGroupPause) {
        this.targetUri = targetUri;
        this.localPort = localPort;
        this.outFile = outFile;
        this.groupStrategy = groupStrategy;
        this.automaticGroupDetectInterval = automaticGroupDetectInterval;
        this.betweenGroupPause = betweenGroupPause;
    }

    public void start() throws Exception {
        URI uri = new URI(targetUri);

        //Init contex
        context.setTargetUri(targetUri);
        context.setLocalPort(localPort);
        context.setOutFile(outFile);
        context.setAutomaticGroupDetectInterval(automaticGroupDetectInterval);
        context.setStrategy(groupStrategy);
        context.setBetweenGroupPause(betweenGroupPause);

        // Target host
        HttpHost targetHost = new HttpHost(
                uri.getHost(),
                uri.getPort() > 0 ? uri.getPort() : 80,
                uri.getScheme() != null ? uri.getScheme() : "http");

        logger.info("Start reverse proxy to " + targetHost + " on local port " + localPort);

        IOReactorConfig config = IOReactorConfig.custom()
                .setIoThreadCount(1)
                .setSoTimeout(30000)
                .setConnectTimeout(30000)
                .build();
        connectingIOReactor = new DefaultConnectingIOReactor(config);
        final ListeningIOReactor listeningIOReactor = new DefaultListeningIOReactor(config);

        // Set up HTTP protocol processor for incoming connections
        HttpProcessor inhttpproc = new ImmutableHttpProcessor(
                new HttpResponseInterceptor[] {
                        new ResponseDate(),
                        new ResponseServer("Test/1.1"),
                        new ResponseContent(),
                        new ResponseConnControl()
                });

        // Set up HTTP protocol processor for outgoing connections
        HttpProcessor outhttpproc = new ImmutableHttpProcessor(
                new HttpRequestInterceptor[] {
                        new RequestContent(),
                        new RequestTargetHost(),
                        new RequestConnControl(),
                        new RequestUserAgent("Test/1.1"),
                        new RequestExpectContinue(true)
                });

        ProxyClientProtocolHandler clientHandler = new ProxyClientProtocolHandler();
        HttpAsyncRequester executor = new HttpAsyncRequester(
                outhttpproc, new ProxyOutgoingConnectionReuseStrategy());

        ProxyConnPool connPool = new ProxyConnPool(connectingIOReactor, ConnectionConfig.DEFAULT);
        connPool.setMaxTotal(100);
        connPool.setDefaultMaxPerRoute(20);

        UriHttpAsyncRequestHandlerMapper handlerRegistry = new UriHttpAsyncRequestHandlerMapper();
        handlerRegistry.register("*", new ProxyRequestHandler(targetHost, executor, connPool, context));

        ProxyServiceHandler serviceHandler = new ProxyServiceHandler(
                inhttpproc,
                new ProxyIncomingConnectionReuseStrategy(),
                handlerRegistry);

        final IOEventDispatch connectingEventDispatch = new DefaultHttpClientIODispatch(
                clientHandler, ConnectionConfig.DEFAULT);

        final IOEventDispatch listeningEventDispatch = new DefaultHttpServerIODispatch(
                serviceHandler, ConnectionConfig.DEFAULT);

        serverThread = new Thread(new Runnable() {

            public void run() {
                try {
                    connectingIOReactor.execute(connectingEventDispatch);
                } catch (InterruptedIOException ex) {
                    System.err.println("Interrupted");
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        listeningIOReactor.shutdown();
                    } catch (IOException ex2) {
                        ex2.printStackTrace();
                    }
                }
            }

        });
        serverThread.start();
        try {
            listeningIOReactor.listen(new InetSocketAddress(localPort));
            listeningIOReactor.execute(listeningEventDispatch);
            logger.info("End work server.");
        } catch (InterruptedIOException ex) {
            System.err.println("Interrupted");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                connectingIOReactor.shutdown();
            } catch (IOException ex2) {
                ex2.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void saveResult() {
        try {
            logger.info("Save out file");
            //Сохранение простого xml для отладки
            /*Serializer serializer = new Persister();
            File result = new File(context.getOutFile() + ".xml");
            serializer.write(context.getJournal(), result);*/

            //Сохранения jmeter скрипта
            //Загрузка шаблона
            JMeterUtils.setJMeterHome(System.getProperty("user.dir"));
            JMeterUtils.loadJMeterProperties(JMeterUtils.getJMeterHome() + "/bin/jmeter.properties");
            HashTree script = SaveService.loadTree(new File("gwt-proxy-template.jmx"));

            writeScriptParams(script);
            writeSystemParams(script);    
            writeHeaderManager(script);
            
            HashTree recordScript = getHashTreeByName(script, "Работа клиента");
            int samplerNo = 0;
            int groupNo = 0;
            for (GwtInteractionGroup group : context.getJournal().getGroupList()) {
                //Добавляем группировку
                TransactionController genericController = new TransactionController();
                genericController.setName(group.getName());
                genericController.setProperty(TestElement.GUI_CLASS, TransactionControllerGui.class.getName());
                genericController.setProperty(TestElement.TEST_CLASS, TransactionController.class.getName());
                genericController.setIncludeTimers(false);
                genericController.setParent(false);
                genericController.setEnabled(true);

                HashTree genericControllerTree = new ListedHashTree();

                for (GwtInteraction requestResponce : group.getRequestResponceList()) {

                    HTTPSamplerBase sampler = null;
                    if (requestResponce.getRequest().getServiceClass() != null) {
                        sampler = new GwtRpcSampler();
                        sampler.setProperty(TestElement.TEST_CLASS, GwtRpcSampler.class.getName());
                        sampler.setProperty(TestElement.GUI_CLASS, GwtRpcHttpTestSampleGui.class.getName());
                        sampler.setName(samplerNo + "-" + requestResponce.getRequest().getServiceClass() + "."
                                + requestResponce.getRequest().getServiceMethod());
                    } else if (requestResponce.getRequest().getFile() != null) {
                        sampler = new HTTPSamplerProxy();
                        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
                        sampler.setProperty(TestElement.GUI_CLASS, HttpUploadSampleGui.class.getName());
                        sampler.setName(samplerNo + "-Upload file: " + requestResponce.getRequest().getFile().getFileName());
                    } else {
                        sampler = new HTTPSamplerProxy();
                        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
                        sampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
                        sampler.setName(samplerNo + "-" + requestResponce.getRequest().getUrl());
                    }
                    sampler.setEnabled(true);
                    sampler.setMethod(requestResponce.getRequest().getMethod());
                    sampler.setPath(requestResponce.getRequest().getUrl());
                    sampler.setFollowRedirects(true);
                    sampler.setAutoRedirects(false);
                    sampler.setUseKeepAlive(true);
                    sampler.setDoMultipartPost(false);
                    sampler.setMonitor(false);                    
                    sampler.setPostBodyRaw(true);

                    if (requestResponce.getRequest().getFile() != null) {
                        sampler.setDoMultipartPost(true);
                        FileInfo fileInfo = requestResponce.getRequest().getFile();
                        HTTPFileArg fileArg = new HTTPFileArg("${BASE_DIR}" + fileInfo.getFileName(), fileInfo.getParamName(), fileInfo.getFileContentType());
                        sampler.setHTTPFiles(new HTTPFileArg[] { fileArg });
                        sampler.setProperty("GwtRpcResponceJson", requestResponce.getResponce().getBody());
                    } else {
                        sampler.getArguments().addArgument(new HTTPArgument("", requestResponce.getRequest().getBody()));
                        if (requestResponce.getRequest().getJson() != null) {
                            sampler.setProperty("GwtRpcRequestJson", Base64.encodeBase64String(requestResponce.getRequest().getJson().getBytes("UTF-8")));
                        }
                        if (requestResponce.getResponce().getJson() != null) {
                            sampler.setProperty("GwtRpcResponceJson", Base64.encodeBase64String(requestResponce.getResponce().getJson().getBytes("UTF-8")));
                        }
                    }

                    HashTree samplerTree = new ListedHashTree();

                    //Формирование препроцессора
                    BeanShellPreProcessor preProcessor = new BeanShellPreProcessor();
                    preProcessor.setName("Формирование запроса");
                    preProcessor.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());
                    preProcessor.setProperty(TestElement.TEST_CLASS, BeanShellPreProcessor.class.getName());
                    preProcessor.setEnabled(true);

                    preProcessor.setProperty("resetInterpreter", false);
                    preProcessor.setProperty("parameters", "");
                    preProcessor.setProperty("filename", "");

                    String scriptText = "import ru.intertrust.performance.jmetertools.*;\n";

                    scriptText += "try{\n";
                    if (requestResponce.getRequest().getFile() == null) {
                        //Обновление доменных объектов и идентификаторов в запросе
                        scriptText += "\t//Змена ID и доменных объектов\n";
                        scriptText += "\tGwtUtil.preRequestProcessing(ctx);\n";
                    }
                    scriptText += "\t//-----  Пользовательский код   ----------------------------\n";
                    scriptText += "\t\n";
                    scriptText += "\t//----------------------------------------------------------\n";

                    scriptText += "}catch(Exception ex){\n";
                    scriptText += "\tlog.error(\"Pre request error in sampler \" + sampler.getName(), ex);\n";
                    scriptText += "\tsampler.setError(true);\n";
                    scriptText += "}\n";

                    preProcessor.setProperty("script", scriptText);

                    HashTree preProcessorTree = new ListedHashTree();
                    samplerTree.add(preProcessor, preProcessorTree);

                    //Формирование пост обработчика
                    BeanShellPostProcessor postProcessor = new BeanShellPostProcessor();
                    postProcessor.setName("Анализ результата");
                    postProcessor.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());
                    postProcessor.setProperty(TestElement.TEST_CLASS, BeanShellPostProcessor.class.getName());
                    postProcessor.setEnabled(true);

                    postProcessor.setProperty("resetInterpreter", false);
                    postProcessor.setProperty("parameters", "");
                    postProcessor.setProperty("filename", "");

                    scriptText = "import ru.intertrust.performance.jmetertools.*;\n";
                    scriptText += "try{\n";
                    scriptText += "\t//Проверка на ошибки\n";
                    scriptText += "\tif (GwtUtil.isError(ctx)){\n";
                    scriptText += "\t\tprev.setSuccessful(false);\n";
                    scriptText += "\t}else{\n";

                    //Для вложения добавляем сохранялку имени временного контента
                    if (requestResponce.getRequest().getFile() != null) {
                        scriptText += "\t\t//Сохранение имени вложения\n";
                        scriptText += "\t\tGwtUtil.storeUploadResult(ctx);\n";
                    } else {
                        //Сохранение пришедших доменных объектов и идентификаторов в контексте
                        scriptText += "\t\t//Сохранение ID и доменных объектов\n";
                        scriptText += "\t\tGwtUtil.postResponseProcessing(ctx);\n";
                    }
                    scriptText += "\t\t//-----  Пользовательский код   ----------------------------\n";
                    scriptText += "\t\t\n";
                    scriptText += "\t\t//----------------------------------------------------------\n";
                    scriptText += "\t}\n";
                    scriptText += "}catch(Exception ex){\n";
                    scriptText += "\tprev.setSuccessful(false);\n";
                    scriptText += "\tlog.error(\"Post request error in sampler \" + ctx.getCurrentSampler().getName(), ex);\n";
                    scriptText += "}\n";

                    postProcessor.setProperty("script", scriptText);

                    HashTree postProcessorTree = new ListedHashTree();
                    samplerTree.add(postProcessor, postProcessorTree);

                    genericControllerTree.add(sampler, samplerTree);
                    samplerNo++;
                }
                if (recordScript != null && genericControllerTree.size() > 0) {
                    if (groupNo > 0) {
                        TestAction timer = new TestAction();
                        timer.setEnabled(true);
                        timer.setName("Пауза " + groupNo);
                        timer.setDuration("" + group.getBeforePause() * 1000);
                        timer.setTarget(TestAction.THREAD);
                        timer.setAction(TestAction.PAUSE);
                        timer.setProperty(TestElement.GUI_CLASS, TestActionGui.class.getName());
                        timer.setProperty(TestElement.TEST_CLASS, TestAction.class.getName());

                        recordScript.add(timer);
                    }

                    recordScript.add(genericController, genericControllerTree);
                }
                groupNo++;
            }

            File resultJmx = new File(context.getOutFile());
            SaveService.saveTree(script, new FileOutputStream(resultJmx));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void writeScriptParams(HashTree script) throws URISyntaxException {
        Arguments sysParams = (Arguments)getSamplerByName(script, "Конфигурация скрипта");
        if (sysParams != null) {
            URI uri = new URI(targetUri);

            for (int i = 0; i < sysParams.getArgumentCount(); i++) {
                Argument arg = sysParams.getArgument(i);
                if (arg.getName().equalsIgnoreCase("HOST")) {
                    arg.setValue(uri.getHost());
                }
                else if (arg.getName().equalsIgnoreCase("PORT")) {
                    arg.setValue(String.valueOf(uri.getPort()));
                }
            }
        }
    }

    private void writeHeaderManager(HashTree script){
        HeaderManager headerManager = (HeaderManager)getSamplerByName(script, "HTTP Header Manager");
        //Добавляем Header
        if (headerManager != null) {
            headerManager.add(new Header(ProxyContext.X_GWT_PERMUTATION, context.getJournal().getxGwtPermutation()));
        }
    }
    
    private void writeSystemParams(HashTree script) {
        Arguments sysParams = (Arguments)getSamplerByName(script, "Системные переменные");
        if (sysParams != null) {
            Map<String, String> strongNames = GwtProcySerializationPolicyProvider.getPolicyMap();

            Set<String> uniqueStrongNames = new HashSet<String>();

            for (String key : strongNames.keySet()) {
                String policyKey = GwtRpcSampler.POLICY_PARAM_PREFIX + key;

                if (!uniqueStrongNames.contains(policyKey)) {
                    sysParams.addArgument(policyKey, strongNames.get(key));
                    uniqueStrongNames.add(policyKey);
                }
            }
        }
    }

    private HashTree getHashTreeByName(HashTree script, String name) {
        HashTree result = null;
        for (Object key : script.keySet()) {
            if (key instanceof AbstractTestElement) {
                AbstractTestElement controller = (AbstractTestElement) key;
                if (controller.getName().equalsIgnoreCase(name)) {
                    result = script.getTree(controller);
                } else {
                    result = getHashTreeByName(script.getTree(key), name);
                }
            } else {
                result = getHashTreeByName(script.getTree(key), name);
            }
            if (result != null)
                break;
        }
        return result;
    }
    
    private AbstractTestElement getSamplerByName(HashTree script, String name){
        AbstractTestElement result = null;
        for (Object key : script.keySet()) {
            if (key instanceof AbstractTestElement) {
                AbstractTestElement controller = (AbstractTestElement) key;
                if (controller.getName().equalsIgnoreCase(name)) {
                    result = controller;
                } else {
                    result = getSamplerByName(script.getTree(key), name);
                }
            } else {
                result = getSamplerByName(script.getTree(key), name);
            }
            if (result != null)
                break;
        }
        return result;
        
    }

    public void stop() throws IOException {
        connectingIOReactor.shutdown();
        saveResult();
    }

    public void addGroup(String name, int beforeGroupPause) {
        context.setManualStategyGroupName(name);
        context.setBetweenGroupPause(beforeGroupPause);
    }

    public void addListener(ProxyContextListener listener) {
        context.addListener(listener);
    }

    public void clearListeners() {
        context.clearListeners();
    }
}
