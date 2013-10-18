package ru.intertrust.performance.jmeter.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import ru.intertrust.performance.jmeter.CrudSampler;
import ru.intertrust.performance.jmeter.UserJobTestScriptSampler;

/**
 * Клаcc реализующий интерфейс пользователя для задания параметров UserJobTestScriptSampler 
 * @author Stepygin Sergey Date: 30.08.13 Time: 17:23
 * 
 */
public class UserJobTestScriptSamplerGUI extends AbstractSamplerGui {

    private static final long serialVersionUID = 1L;
    private static final String PROVIDER_URL_FIELD = "providerUrl";
    private static final String SECURITY_PRINCIPAL_FIELD = "securityPrincipal";
    private static final String SECURITY_CREDENTIALS_FIELD = "securityCredentials";

    private JTextField providerUrl;
    private JTextField securityPrincipal;
    private JTextField securityCredentials;

    private UserJobTestScriptSampler model;

    public UserJobTestScriptSamplerGUI() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "user_job_test_script";
    }

    @Override
    public TestElement createTestElement() {
    	UserJobTestScriptSampler sampler = new UserJobTestScriptSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        model = (UserJobTestScriptSampler) element;
        providerUrl.setText(model.getProviderUrl());
        securityPrincipal.setText(model.getSecurityPrincipal());
        securityCredentials.setText(model.getSecurityCredentials());    
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
        if (element instanceof UserJobTestScriptSampler) {
            model = (UserJobTestScriptSampler) element;
            model.setProviderUrl(providerUrl.getText());
            model.setSecurityPrincipal(securityPrincipal.getText());
            model.setSecurityCredentials(securityCredentials.getText());
        }

    }

    // ----
    public String getStaticLabel() {
        return "UserJobTestScript";
    }

    private void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        providerUrl = new JTextField("", 40);
        providerUrl.setName(PROVIDER_URL_FIELD);
        JLabel providerUrlLabel = new JLabel("Provider Url");
        providerUrlLabel.setLabelFor(providerUrl);
        
        securityPrincipal = new JTextField("", 40);
        securityPrincipal.setName(SECURITY_PRINCIPAL_FIELD);
        JLabel securityPrincipalLabel = new JLabel("Security principal");
        securityPrincipalLabel.setLabelFor(securityPrincipal);
        
        securityCredentials = new JTextField("", 40);
        securityCredentials.setName(SECURITY_CREDENTIALS_FIELD);
        JLabel securityCredentialsLabel = new JLabel("Security credentails");
        securityCredentialsLabel.setLabelFor(securityCredentials);
        
        Box providerUrlBox = Box.createVerticalBox();
        providerUrlBox.add(providerUrlLabel);
        providerUrlBox.add(providerUrl);
        providerUrlBox.add(securityPrincipalLabel);
        providerUrlBox.add(securityPrincipal);
        providerUrlBox.add(securityCredentialsLabel);
        providerUrlBox.add(securityCredentials);
        providerUrlBox.setBorder(BorderFactory.createEtchedBorder());
        
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BorderLayout());
        configPanel.add(providerUrlBox, BorderLayout.NORTH);
        JPanel actionPanelLayout = new JPanel();
        
        actionPanelLayout.setLayout(new BorderLayout());
        configPanel.add(actionPanelLayout, BorderLayout.CENTER);
        add(configPanel, BorderLayout.CENTER);
    }
}
