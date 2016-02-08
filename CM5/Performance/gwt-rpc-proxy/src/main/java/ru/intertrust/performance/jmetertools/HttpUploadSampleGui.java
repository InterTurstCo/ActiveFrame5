package ru.intertrust.performance.jmetertools;

import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Класс модифицирующий представление стандартного HttpTestSample для возможности сохранять имена вложений
 * @author larin
 *
 */
public class HttpUploadSampleGui extends HttpTestSampleGui {
    private static final long serialVersionUID = -6917992030933433477L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private String requestJson;
    private String responceJson;

    public HttpUploadSampleGui() {
        super();
    }

    // For use by AJP
    protected HttpUploadSampleGui(boolean ajp) {
        super(ajp);
    }

    @Override
    public String getStaticLabel() {
        return "Upload file Sampler";
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
        HTTPSamplerBase sampler = new HTTPSamplerProxy();
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
        super.modifyTestElement(sampler);
        final HTTPSamplerBase samplerBase = (HTTPSamplerBase) sampler;
        samplerBase.setProperty("GwtRpcRequestJson", requestJson);
        samplerBase.setProperty("GwtRpcResponceJson", responceJson);
    }
}
