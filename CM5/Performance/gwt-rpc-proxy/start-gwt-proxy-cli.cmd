set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_67
set TARGET_URI=http://localhost:8080
set LOCAL_PORT=8090
SET OUTPUT_FILE=result.jmx


"%JAVA_HOME%\bin\java" -cp gwt-rpc-proxy-0.5.16-2-SNAPSHOT.jar;lib\* ru.intertrust.performance.gwtrpcproxy.GwtRpcProxy --target-uri %TARGET_URI% --local-port %LOCAL_PORT% --output-file "%OUTPUT_FILE%"