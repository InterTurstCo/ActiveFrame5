package ru.intertrust.performance.jmetertools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.config.gui.MultipartUrlConfigGui;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class GwtRpcHttpTestSampleGui extends HttpTestSampleGui {
    private static final long serialVersionUID = -6917992030933433477L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private String requestJson;
    private String responceJson;

    public GwtRpcHttpTestSampleGui() {
        super();
        init();
    }

    // For use by AJP
    protected GwtRpcHttpTestSampleGui(boolean ajp) {
        super(ajp);
        init();
    }

    @Override
    public String getStaticLabel() {
        return "Gwt Rpc Sampler";
    }    
    
    private void init() {
        MultipartUrlConfigGui paramPanel = findPanel(this, MultipartUrlConfigGui.class);

        JButton showRequestJsonButton = new JButton("Show request as JSON");
        showRequestJsonButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showJsonViewer(requestJson);
            }
        });

        JButton showResponceJsonButton = new JButton("Show responce as JSON");
        showResponceJsonButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showJsonViewer(responceJson);
            }
        });

        JPanel jsonButtonPanel = new VerticalPanel();

        jsonButtonPanel.add(showRequestJsonButton);
        jsonButtonPanel.add(showResponceJsonButton);
        ((JPanel) paramPanel.getComponents()[4]).add(jsonButtonPanel, BorderLayout.EAST);
    }

    private void showJsonViewer(String json) {
        JsonViewer jsonViewer = new JsonViewer(getWindowForComponent(GwtRpcHttpTestSampleGui.this), json);
        jsonViewer.setVisible(true);
    }

    private Window getWindowForComponent(Component parentComponent)
            throws HeadlessException {
        if (parentComponent instanceof Frame || parentComponent instanceof Dialog)
            return (Window) parentComponent;
        return getWindowForComponent(parentComponent.getParent());
    }

    private <T> T findPanel(JPanel base, Class T) {
        Component result = null;
        for (Component child : base.getComponents()) {
            if (T.isAssignableFrom(child.getClass())) {
                result = child;
                break;
            } else {
                if (child instanceof JPanel) {
                    result = findPanel((JPanel) child, T);
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        return (T) result;
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
        super.modifyTestElement(sampler);
        final HTTPSamplerBase samplerBase = (HTTPSamplerBase) sampler;
        samplerBase.setProperty("GwtRpcRequestJson", requestJson);
        samplerBase.setProperty("GwtRpcResponceJson", responceJson);
    }
}
