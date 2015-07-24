package ru.intertrust.performance.gwtrpcproxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.URI;

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
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.extractor.BeanShellPostProcessor;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import ru.intertrust.performance.jmetertools.GwtRpcHttpTestSampleGui;

public class GwtRpcProxy {
    public static void main(String[] args) {
        try {

            if (args.length < 1) {
                System.out.println("Usage: ru.intertrust.performance.gwtrpcproxy.GwtRpcProxy <targetUri> [localPort] [output_file] [group_pause in sec]");
                System.out.println("Default port is 8080");
                System.out.println("Default file name is gwtProxyOut.xml");
                System.out.println("Default group pause is 1 sec");
                System.out.println("Example: ru.intertrust.performance.gwtrpcproxy.GwtRpcProxy http://cm45.inttrust.ru:8080 8090 my_gwtProxyOut.xml 2");
                System.exit(1);
            }

            GwtRpcProxy gwtRpcProxy = new GwtRpcProxy();
            gwtRpcProxy.start(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void start(String[] args) throws Exception {

        String targetUri = args[0];

        URI uri = new URI(targetUri);
        int localPort = 8080;
        if (args.length > 1) {
            localPort = Integer.parseInt(args[1]);
        }

        //Init contex
        final ProxyContext context = new ProxyContext();
        context.setTargetUri(targetUri);
        context.setLocalPort(localPort);

        if (args.length > 2) {
            context.setOutFile(args[2]);
        }

        if (args.length > 3) {
            context.setGroupPause(Integer.parseInt(args[3]));
        } else {
            context.setGroupPause(1);
        }

        // Target host
        HttpHost targetHost = new HttpHost(
                uri.getHost(),
                uri.getPort() > 0 ? uri.getPort() : 80,
                uri.getScheme() != null ? uri.getScheme() : "http");

        System.out.println("Reverse proxy to " + targetHost);

        IOReactorConfig config = IOReactorConfig.custom()
                .setIoThreadCount(1)
                .setSoTimeout(30000)
                .setConnectTimeout(30000)
                .build();
        final ConnectingIOReactor connectingIOReactor = new DefaultConnectingIOReactor(config);
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

        //Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    //Сохранение простого xml для отладки
                    System.out.println("Running Shutdown Hook. Save out file");
                    Serializer serializer = new Persister();
                    File result = new File(context.getOutFile());
                    serializer.write(context.getJournal(), result);

                    //Сохранения jmeter скрипта
                    //Загрузка шаблона
                    JMeterUtils.setJMeterHome(System.getProperty("user.dir"));
                    JMeterUtils.loadJMeterProperties(JMeterUtils.getJMeterHome() + "/bin/jmeter.properties");
                    HashTree script = SaveService.loadTree(new File("gwt-proxy-template.jmx"));

                    HashTree recordScript = getRecordScript(script);
                    int samplerNo = 0;
                    for (GwtInteractionGroup group : context.getJournal().getGroupList()) {
                        //Добавляем группировку
                        GenericController genericController = new GenericController();
                        genericController.setName(group.getName());
                        genericController.setProperty(TestElement.GUI_CLASS, LogicControllerGui.class.getName());
                        genericController.setProperty(TestElement.TEST_CLASS, GenericController.class.getName());
                        genericController.setEnabled(true);
                        
                        HashTree genericControllerTree = new ListedHashTree();
                        
                        //Добавляем HeaderManager
                        HeaderManager headerManager = new HeaderManager();
                        headerManager.setName(group.getName() + " HTTP Header Manager");
                        headerManager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
                        headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
                        headerManager.setEnabled(true);
                        headerManager.add(new Header("Content-Type", "text/x-gwt-rpc;charset=UTF-8"));
                        // TODO вынести из кода
                        headerManager.add(new Header("X-GWT-Permutation", "A3EF20039C3580B544F259C1A09C834B"));                        

                        genericControllerTree.add(headerManager);
                        
                        for (GwtInteraction requestResponce : group.getRequestResponceList()) {
                            
                            GenericController uploadGenericController = null;
                            HashTree uploadGenericControllerTree = null;
                            if (requestResponce.getRequest().getFile() != null){
                                //Создаем индивидуальную группу для upload вложений
                                uploadGenericController = new GenericController();
                                uploadGenericController.setName("Upload attachment group");
                                uploadGenericController.setProperty(TestElement.GUI_CLASS, LogicControllerGui.class.getName());
                                uploadGenericController.setProperty(TestElement.TEST_CLASS, GenericController.class.getName());
                                uploadGenericController.setEnabled(true);

                                uploadGenericControllerTree = new ListedHashTree();
                                
                                //Добавляем HeaderManager
                                HeaderManager uploadHeaderManager = new HeaderManager();
                                uploadHeaderManager.setName("Upload attachment HTTP Header Manager");
                                uploadHeaderManager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
                                uploadHeaderManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
                                uploadHeaderManager.setEnabled(true);
                                // TODO вынести из кода
                                uploadHeaderManager.add(new Header("X-GWT-Permutation", "A3EF20039C3580B544F259C1A09C834B"));
                                
                                uploadGenericControllerTree.add(uploadHeaderManager);                                
                            }
                            
                            HTTPSamplerProxy sampler = new HTTPSamplerProxy();
                            sampler.setName(samplerNo + "-" + requestResponce.getRequest().getUrl());
                            sampler.setEnabled(true);
                            sampler.setMethod(requestResponce.getRequest().getMethod());
                            sampler.setPath(requestResponce.getRequest().getUrl());
                            sampler.setFollowRedirects(true);
                            sampler.setAutoRedirects(false);
                            sampler.setUseKeepAlive(true);
                            sampler.setDoMultipartPost(false);
                            sampler.setMonitor(false);
                            sampler.setProperty(TestElement.GUI_CLASS, GwtRpcHttpTestSampleGui.class.getName());
                            sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
                            sampler.setPostBodyRaw(true);
                            
                            if (requestResponce.getRequest().getFile() != null){
                                sampler.setDoMultipartPost(true);
                                FileInfo fileInfo = requestResponce.getRequest().getFile();
                                HTTPFileArg fileArg = new HTTPFileArg("${BASE_DIR}" + fileInfo.getFileName(), fileInfo.getParamName(), fileInfo.getFileContentType());                                
                                sampler.setHTTPFiles(new HTTPFileArg[]{fileArg});
                            }else{
                                sampler.getArguments().addArgument(new HTTPArgument("", requestResponce.getRequest().getBody()));
                                sampler.setProperty("GwtRpcRequestJson", requestResponce.getRequest().getJson());
                            }
                            sampler.setProperty("GwtRpcResponceJson", requestResponce.getResponce().getJson());
                            
                            HashTree samplerTree = new ListedHashTree();

                            BeanShellPostProcessor postProcessor = new BeanShellPostProcessor();
                            postProcessor.setName("Анализ результата");
                            postProcessor.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());
                            postProcessor.setProperty(TestElement.TEST_CLASS, BeanShellPostProcessor.class.getName());
                            postProcessor.setEnabled(true);

                            postProcessor.setProperty("resetInterpreter", false);
                            postProcessor.setProperty("parameters", "");
                            postProcessor.setProperty("filename", "");

                            String scriptText = "import ru.intertrust.performance.jmetertools.*;\n";
                            scriptText += "//Проверка на ошибки\n";
                            scriptText += "if (GwtUtil.isError(prev)){\n";
                            scriptText += "\tlog.error(GwtUtil.decodeResponce(prev).toString());\n";
                            scriptText += "\tprev.setSuccessful(false);\n";
                            scriptText += "}\n";
                            
                            //Для вложения добавляем сохранялку имени временного контента
                            if (uploadGenericController != null){
                                scriptText += "//Получение имени вложения \n";
                                scriptText += "String tempAttacheName = prev.getResponseDataAsString().substring(0, prev.getResponseDataAsString().length() - 1);\n";
                                scriptText += "log.info(\"Temp attachment name - \" + tempAttacheName);\n";
                                scriptText += "vars.put(\"ATTACHMENT_TEMP_NAME\", tempAttacheName);\n";
                            }

                            postProcessor.setProperty("script", scriptText);

                            HashTree postProcessorTree = new ListedHashTree();
                            samplerTree.add(postProcessor, postProcessorTree);

                            if (uploadGenericController != null){
                                uploadGenericControllerTree.add(sampler, samplerTree);
                                recordScript.add(uploadGenericController, uploadGenericControllerTree);
                            }else{
                                genericControllerTree.add(sampler, samplerTree);
                            }
                            samplerNo++;
                        }
                        if (genericControllerTree.size() > 0){
                            recordScript.add(genericController, genericControllerTree);
                        }
                    }

                    File resultJmx = new File(context.getOutFile() + ".jmx");
                    SaveService.saveTree(script, new FileOutputStream(resultJmx));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

            private HashTree getRecordScript(HashTree script) {
                HashTree result = null;
                for (Object key : script.keySet()) {
                    if (key instanceof GenericController) {
                        GenericController controller = (GenericController) key;
                        if (controller.getName().equalsIgnoreCase("Работа клиента")) {
                            result = script.getTree(controller);
                        } else {
                            result = getRecordScript(script.getTree(key));
                        }
                    } else {
                        result = getRecordScript(script.getTree(key));
                    }
                    if (result != null)
                        break;
                }
                return result;
            }
        });

        Thread t = new Thread(new Runnable() {

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
        t.start();
        try {
            listeningIOReactor.listen(new InetSocketAddress(localPort));
            listeningIOReactor.execute(listeningEventDispatch);
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
}
