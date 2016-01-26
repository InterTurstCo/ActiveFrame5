package ru.intertrust.performance.jmetertools;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public class GwtRpcHttpTestSampleGui extends AbstractSamplerGui {
    private static final long serialVersionUID = -6917992030933433477L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private JSyntaxTextArea requestTextBox;
    private JSyntaxTextArea responseTextBox;
    private JTextField pathField;

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

        VerticalPanel headerFrame = new VerticalPanel();
        headerFrame.setBorder(BorderFactory.createEtchedBorder());
        add(headerFrame, BorderLayout.NORTH);
        
        Container titlePanel = makeTitlePanel();
        
        headerFrame.add(titlePanel, BorderLayout.NORTH);
        
        JPanel pathPanel = new HorizontalPanel();
        titlePanel.add(pathPanel, BorderLayout.CENTER);
        
        JLabel pathLabel = new JLabel("Path");
        pathPanel.add(pathLabel);
        pathField = new JTextField(15);
        pathLabel.setLabelFor(pathField);        
        pathPanel.add(pathField);        
        
        VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.setBorder(BorderFactory.createEtchedBorder());
        add(contentPanel, BorderLayout.CENTER);

        JTabbedPane tabbedPane = new JTabbedPane();
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        requestTextBox = new JSyntaxTextArea(30, 50);
        requestTextBox.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        
        JTextScrollPane requestScrollPanel = new JTextScrollPane(requestTextBox);
        tabbedPane.add("Request", requestScrollPanel);

        responseTextBox = new JSyntaxTextArea(30, 50);
        responseTextBox.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        
        JTextScrollPane responseScrollPanel = new JTextScrollPane(responseTextBox);
        tabbedPane.add("Response", responseScrollPanel);
    }

    @Override
    public void configure(TestElement element) {
        try {
            super.configure(element);
            if (element instanceof GwtRpcSampler) {
                final GwtRpcSampler sampler = (GwtRpcSampler) element;
                requestTextBox.setText(sampler.getRequestJson());
                responseTextBox.setText(sampler.getResponseJson());
                pathField.setText(sampler.getPath());
                //Скрол на верх
                requestTextBox.setCaretPosition(0);
                responseTextBox.setCaretPosition(0);
            } else {
                requestTextBox.setText("");
                responseTextBox.setText("");
            }            
        } catch (Exception ex) {
            log.error("Error configure test element", ex);
            throw new RuntimeException("Error configure test element", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        GwtRpcSampler sampler = new GwtRpcSampler();
        sampler.setProperty(TestElement.TEST_CLASS, GwtRpcSampler.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, GwtRpcHttpTestSampleGui.class.getName());
        sampler.setName("Gwt Rpc Sampler");
        
        sampler.setEnabled(true);
        sampler.setMethod("POST");
        sampler.setPath("");
        sampler.setFollowRedirects(true);
        sampler.setAutoRedirects(false);
        sampler.setUseKeepAlive(true);
        sampler.setDoMultipartPost(false);
        sampler.setMonitor(false);                    
        sampler.setPostBodyRaw(true);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement element) {
        try {
            if (element instanceof GwtRpcSampler) {
                final GwtRpcSampler sampler = (GwtRpcSampler) element;
                sampler.setRequestJson(requestTextBox.getText());
                sampler.setResponseJson(responseTextBox.getText());
                sampler.setPath(pathField.getText());
            }
            element.setName(getName());
            element.setComment(getComment());
        } catch (Exception ex) {
            log.error("Error configure test element", ex);
            throw new RuntimeException("Error modify test element", ex);
        }
    }

    @Override
    public String getLabelResource() {
        return "GwtRpcHttpTestSampleGui";
    }
}
