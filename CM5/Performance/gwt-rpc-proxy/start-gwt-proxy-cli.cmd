set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_67

"%JAVA_HOME%\bin\java" -cp gwt-rpc-proxy-0.5.16-2-SNAPSHOT.jar;lib\* ru.intertrust.performance.gwtrpcproxy.GwtRpcProxy http://localhost:8080 8090 "result.xml"