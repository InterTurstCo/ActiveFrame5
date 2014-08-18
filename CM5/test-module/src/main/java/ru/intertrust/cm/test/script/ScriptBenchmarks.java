package ru.intertrust.cm.test.script;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class ScriptBenchmarks {
    private String script = "sms.getServiceNumber() == '777' && sms.getMobileNumber().startsWith('8050')";
    private Sms sms = new Sms();
    private int loops = 10000;

    private void init() {
        sms.setMobileNumber("80500000000");
        sms.setServiceNumber("777");
    }

    public static void main(String[] args) throws ScriptException {
        ScriptBenchmarks scriptBenchmarks = new ScriptBenchmarks();
        scriptBenchmarks.init();

        /*scriptBenchmarks.testInitAnEvalInsideLoop();
        scriptBenchmarks.testInitOutsideEvalInsideLoop();
        scriptBenchmarks.testPrecompiledScript();*/
        scriptBenchmarks.testScriptBuinding();
    }

    // testInitAndEvalInsideLoop average: 1ms
    private void testInitAnEvalInsideLoop() throws ScriptException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < loops; i++) {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
            engine.put("sms", sms);
            engine.eval(script);
        }
        long total = System.currentTimeMillis() - start;
        System.out.println("testInitAnEvalInsideLoop average: " + ((float) total / loops));
    }

    // testInitOutsideEvalInsideLoop average: 0.1ms
    private void testInitOutsideEvalInsideLoop() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        long start = System.currentTimeMillis();
        for (int i = 0; i < loops; i++) {
            engine.put("sms", sms);
            engine.eval(script);
        }
        long total = System.currentTimeMillis() - start;
        System.out.println("testInitOutsideEvalInsideLoop average: " + ((float) total / loops));
    }

    // testPrecompiledScript average: 0.05ms - 0.1ms
    private void testPrecompiledScript() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        CompiledScript compiledScript = ((Compilable) engine).compile(script);
        long start = System.currentTimeMillis();
        for (int i = 0; i < loops; i++) {
            engine.put("sms", sms);
            compiledScript.eval();
        }
        long total = System.currentTimeMillis() - start;
        System.out.println("testPrecompiledScript average: " + ((float) total / loops));
    }

    private void testScriptBuinding() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        CompiledScript compiledScript = ((Compilable) engine).compile(script);
        
        Sms sms1 = new Sms("777", "80500000000");
        Sms sms2 = new Sms("777", "58580000000");
        Sms sms3 = new Sms("777", "80508050000");

        SimpleBindings bindings1 = new SimpleBindings();
        bindings1.put("sms", sms1);

        SimpleBindings bindings2 = new SimpleBindings();
        bindings2.put("sms", sms2);

        SimpleBindings bindings3 = new SimpleBindings();
        bindings3.put("sms", sms3);
        
        
        Object result = compiledScript.eval(bindings1);
        System.out.println(result);

        result = compiledScript.eval(bindings2);
        System.out.println(result);

        result = compiledScript.eval(bindings3);
        System.out.println(result);
    }

    public class Sms {
        private String mobileNumber;
        private String serviceNumber;

        public Sms() {
        }

        public Sms(String serviceNumber, String mobileNumber) {
            this.serviceNumber = serviceNumber;
            this.mobileNumber = mobileNumber;
        }

        public String getMobileNumber() {
            return mobileNumber;
        }

        public void setMobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
        }

        public String getServiceNumber() {
            return serviceNumber;
        }

        public void setServiceNumber(String serviceNumber) {
            this.serviceNumber = serviceNumber;
        }
    }

}
