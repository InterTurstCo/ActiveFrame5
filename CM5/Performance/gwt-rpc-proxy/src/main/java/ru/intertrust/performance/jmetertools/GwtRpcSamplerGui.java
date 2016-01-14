package ru.intertrust.performance.jmetertools;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

public class GwtRpcSamplerGui extends AbstractSamplerGui{
    private static final long serialVersionUID = 657165756414410113L;

    @Override
    public String getStaticLabel() {
        return "Temp Gwt Rpc Sampler";
    }     

    @Override
    public String getLabelResource() {
        return "gwt_rpc_sampler_gui_title";
    }
    
    @Override
    public TestElement createTestElement() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        // TODO Auto-generated method stub
        
    }

}
