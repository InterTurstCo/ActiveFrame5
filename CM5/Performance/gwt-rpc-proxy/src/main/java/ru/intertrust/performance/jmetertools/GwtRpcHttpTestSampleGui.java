package ru.intertrust.performance.jmetertools;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class GwtRpcHttpTestSampleGui extends AbstractSamplerGui {
    private static final long serialVersionUID = -6917992030933433477L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private String requestJson;
    private String responceJson;

    public GwtRpcHttpTestSampleGui() {
        super();
        init();
    }

    @Override
    public String getStaticLabel() {
        return "Gwt Rpc Sampler";
    }    
    
    private void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);
        
        VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.setBorder(BorderFactory.createEtchedBorder());
        add(contentPanel, BorderLayout.CENTER);
    }


    @Override
    public void configure(TestElement element) {
        super.configure(element);
        final HTTPSamplerBase samplerBase = (HTTPSamplerBase) element;
        requestJson = samplerBase.getPropertyAsString("GwtRpcRequestJson");
        responceJson = samplerBase.getPropertyAsString("GwtRpcResponceJson");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        HTTPSamplerBase sampler = new GwtRpcSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        final HTTPSamplerBase samplerBase = (HTTPSamplerBase) sampler;
        samplerBase.setProperty("GwtRpcRequestJson", requestJson);
        samplerBase.setProperty("GwtRpcResponceJson", responceJson);
    }

    @Override
    public String getLabelResource() {
        return "GwtRpcHttpTestSampleGui";
    }
}
