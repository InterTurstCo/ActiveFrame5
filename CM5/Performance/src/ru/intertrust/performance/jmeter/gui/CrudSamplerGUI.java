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

/**
 * Класс, реализующий интерфейс пользователя.
 * @author Stepygin Sergey Date: 30.08.13 Time: 17:23
 * 
 */
public class CrudSamplerGUI extends AbstractSamplerGui {

    private static final long serialVersionUID = 1L;
    private static final String PROVIDER_URL_FIELD = "providerUrl";
    private static final String SECURITY_PRINCIPAL_FIELD = "securityPrincipal";
    private static final String SECURITY_CREDENTIALS_FIELD = "securityCredentials";
    //private static final String OBJECT_NAME_FIELD = "objectName";
    private static final String ATRIBUTE_NAME_FIELD = "atributeName";

    private static final String ACTION_CREATE_FIELD = "actionCreate";
    private static final String ACTION_FIND_FIELD = "actionFind";
    private static final String ACTION_MODIFY_FIELD = "actionModify";

    private JTextField providerUrl;
    private JTextField securityPrincipal;
    private JTextField securityCredentials;
    //private JTextField objectName;
    private JTextField atributeName;

    private JRadioButton createButton;
    private JRadioButton findButton;
    private JRadioButton modifyButton;
    private ButtonGroup actionGroup;
    private final static String CREATE = "create";
    private final static String FIND = "find";
    private final static String MODIFY = "modify";

    private CrudSampler model;

    public CrudSamplerGUI() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "rmi_sampler";
    }

    @Override
    public TestElement createTestElement() {
        CrudSampler sampler = new CrudSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        model = (CrudSampler) element;
        providerUrl.setText(model.getProviderUrl());
        securityPrincipal.setText(model.getSecurityPrincipal());
        securityCredentials.setText(model.getSecurityCredentials());
        //objectName.setText(model.getObjectName());
        atributeName.setText(model.getAtributeName());
        if (model.getActionName().equals(CREATE)) {
            createButton.setSelected(true);
        } else if (model.getActionName().equals(FIND)) {
            findButton.setSelected(true);
        } else if (model.getActionName().equals(MODIFY)) {
            modifyButton.setSelected(true);
        }
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
        if (element instanceof CrudSampler) {
            model = (CrudSampler) element;
            model.setProviderUrl(providerUrl.getText());
            model.setSecurityPrincipal(securityPrincipal.getText());
            model.setSecurityCredentials(securityCredentials.getText());
            //model.setObjectName(objectName.getText());
            model.setAtributeName(atributeName.getText());

            if (createButton.isSelected()) {
                model.setActionName(CREATE);
            } else if (findButton.isSelected()) {
                model.setActionName(FIND);
            } else if (modifyButton.isSelected()) {
                model.setActionName(MODIFY);
            }
        }

    }

    // ----
    public String getStaticLabel() {
        return "Jndi Sampler";
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

        //objectName = new JTextField("", 40);
        //objectName.setName(OBJECT_NAME_FIELD);

        //JLabel objectNameLabel = new JLabel("Object name");
        //objectNameLabel.setLabelFor(objectName);

        atributeName = new JTextField("", 40);
        atributeName.setName(ATRIBUTE_NAME_FIELD);

        JLabel atributeNameLabel = new JLabel("Atribute name");
        atributeNameLabel.setLabelFor(atributeName);

        createButton = new JRadioButton();
        createButton.setName(ACTION_CREATE_FIELD);
        createButton.setSelected(true);
        createButton.setText("Create object");

        findButton = new JRadioButton();
        findButton.setName(ACTION_FIND_FIELD);
        findButton.setSelected(false);
        findButton.setText("Find object");

        modifyButton = new JRadioButton();
        modifyButton.setName(ACTION_MODIFY_FIELD);
        modifyButton.setSelected(false);
        modifyButton.setText("Modify object");

        actionGroup = new ButtonGroup();
        actionGroup.add(createButton);
        actionGroup.add(findButton);
        actionGroup.add(modifyButton);

        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new GridLayout(1, 3));
        actionPanel.add(createButton);
        actionPanel.add(findButton);
        actionPanel.add(modifyButton);

        actionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Choice action:"));

        Box providerUrlBox = Box.createVerticalBox();
        providerUrlBox.add(providerUrlLabel);
        providerUrlBox.add(providerUrl);
        providerUrlBox.add(securityPrincipalLabel);
        providerUrlBox.add(securityPrincipal);
        providerUrlBox.add(securityCredentialsLabel);
        providerUrlBox.add(securityCredentials);
        //providerUrlBox.add(objectNameLabel);
        //providerUrlBox.add(objectName);
        providerUrlBox.add(atributeNameLabel);
        providerUrlBox.add(atributeName);

        providerUrlBox.setBorder(BorderFactory.createEtchedBorder());

        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BorderLayout());
        configPanel.add(providerUrlBox, BorderLayout.NORTH);

        JPanel actionPanelLayout = new JPanel();
        actionPanelLayout.setLayout(new BorderLayout());
        actionPanelLayout.add(actionPanel, BorderLayout.NORTH);
        configPanel.add(actionPanelLayout, BorderLayout.CENTER);

        add(configPanel, BorderLayout.CENTER);
    }
}
