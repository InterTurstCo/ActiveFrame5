package ru.intertrust.performance.gwtrpcproxy;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;

public class GwtProxyGui extends JFrame {
    private static final long serialVersionUID = 3278486895503584915L;

    private GwtRpcProxy proxy;

    private JPanel contentPane;
    private JTextField textFieldServer;
    private JTextField textFieldPort;
    private JTextField textFieldGroupName;
    private JTextField textFieldManualPause;
    private JButton btnStartStop;
    private JLabel lblStatus;
    private DefaultListModel listModel;
    private JList list;
    private JRadioButton rdbtnManual;
    private JRadioButton radioAuto;
    private ButtonGroup buttonGroup;
    private JLabel labelDetectGroupInterval;
    private JLabel labelAutoPause;
    private JLayeredPane layeredPaneManualGroup;
    private JTextField textFieldResultFile;
    private JTextField textFieldDetectGroupInterval;
    private JTextField textFieldAutoPause;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GwtProxyGui frame = new GwtProxyGui();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public GwtProxyGui() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(GwtProxyGui.class.getResource("/41_64x64.png")));
        setTitle("GWT Proxy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 552, 535);
        contentPane = new JPanel();
        contentPane.setLocation(0, -185);
        contentPane.setBorder(new CompoundBorder());
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel label_2 = new JLabel("Способ формирования групп:");
        label_2.setBounds(10, 92, 182, 14);
        contentPane.add(label_2);

        JLabel lblNewLabel = new JLabel("Сервер:");
        lblNewLabel.setBounds(10, 11, 46, 14);
        contentPane.add(lblNewLabel);

        JLabel lblNewLabel_1 = new JLabel("Порт:");
        lblNewLabel_1.setBounds(10, 39, 46, 14);
        contentPane.add(lblNewLabel_1);

        btnStartStop = new JButton("Старт");
        btnStartStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (proxy == null) {
                        //Старт сервера
                        GwtRpcProxy.GroupStrategy strategy = rdbtnManual.isSelected() ? GwtRpcProxy.GroupStrategy.MANUAL : GwtRpcProxy.GroupStrategy.AUTOMATIC;
                        proxy = new GwtRpcProxy(
                                textFieldServer.getText(), 
                                Integer.parseInt(textFieldPort.getText()), 
                                textFieldResultFile.getText(), 
                                strategy, 
                                Integer.parseInt(textFieldDetectGroupInterval.getText()), 
                                Integer.parseInt(textFieldAutoPause.getText()));
                        //Создание потока "serverThread"
                        Thread serverThread = new Thread(proxy);
                        serverThread.start();
                        btnStartStop.setText("Стоп");
                        lblStatus.setText("Запущен");
                        lblStatus.setForeground(Color.GREEN);
                        listModel.clear();
                        //proxy.addListener(new GuiContextListener());
                    } else {
                        //остановка сервера
                        proxy.stop();
                        btnStartStop.setText("Старт");
                        lblStatus.setText("Остановлен");
                        lblStatus.setForeground(Color.RED);
                        //proxy.clearListeners();
                        proxy = null;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        btnStartStop.setBounds(10, 211, 89, 23);
        contentPane.add(btnStartStop);

        textFieldServer = new JTextField();
        textFieldServer.setText("http://localhost:8080");
        textFieldServer.setBounds(186, 8, 340, 20);
        contentPane.add(textFieldServer);
        textFieldServer.setColumns(10);

        textFieldPort = new JTextField();
        textFieldPort.setText("8090");
        textFieldPort.setBounds(186, 36, 340, 20);
        contentPane.add(textFieldPort);
        textFieldPort.setColumns(10);

        layeredPaneManualGroup = new JLayeredPane();
        layeredPaneManualGroup.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        layeredPaneManualGroup.setBounds(10, 245, 516, 241);
        contentPane.add(layeredPaneManualGroup);

        JLabel lblNewLabelGroupName = new JLabel("Имя группы:");
        lblNewLabelGroupName.setBounds(10, 11, 95, 14);
        layeredPaneManualGroup.add(lblNewLabelGroupName);

        JLabel label = new JLabel("Задержка (сек.):");
        label.setBounds(10, 36, 95, 14);
        layeredPaneManualGroup.add(label);

        JButton button = new JButton("Добавить");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                proxy.addGroup(textFieldGroupName.getText(), Integer.parseInt(textFieldManualPause.getText()));
                listModel.addElement("Добавлена группа: " + textFieldGroupName.getText());
                list.ensureIndexIsVisible(list.getSelectedIndex());
            }
        });
        button.setBounds(10, 61, 123, 23);
        layeredPaneManualGroup.add(button);

        textFieldGroupName = new JTextField();
        textFieldGroupName.setBounds(177, 8, 329, 20);
        layeredPaneManualGroup.add(textFieldGroupName);
        textFieldGroupName.setColumns(10);

        textFieldManualPause = new JTextField();
        textFieldManualPause.setText("10");
        textFieldManualPause.setBounds(177, 33, 329, 20);
        layeredPaneManualGroup.add(textFieldManualPause);
        textFieldManualPause.setColumns(10);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 95, 496, 140);
        layeredPaneManualGroup.add(scrollPane);

        listModel = new DefaultListModel();
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setViewportView(list);

        lblStatus = new JLabel("Остановлен");
        lblStatus.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblStatus.setForeground(Color.RED);
        lblStatus.setBounds(122, 215, 137, 14);
        contentPane.add(lblStatus);

        JLabel label_1 = new JLabel("Файл скрипта:");
        label_1.setBounds(10, 67, 182, 14);
        contentPane.add(label_1);

        textFieldResultFile = new JTextField();
        textFieldResultFile.setText("Result.jmx");
        textFieldResultFile.setColumns(10);
        textFieldResultFile.setBounds(186, 64, 340, 20);
        contentPane.add(textFieldResultFile);

        JLayeredPane layeredPaneGroupType = new JLayeredPane();
        layeredPaneGroupType.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        layeredPaneGroupType.setBounds(10, 117, 516, 87);
        contentPane.add(layeredPaneGroupType);

        buttonGroup = new ButtonGroup();

        rdbtnManual = new JRadioButton("Ручной");
        rdbtnManual.setSelected(true);
        rdbtnManual.setBounds(6, 7, 195, 23);
        rdbtnManual.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableControls();
            }
        });
        layeredPaneGroupType.add(rdbtnManual);

        radioAuto = new JRadioButton("Автоматический");
        radioAuto.setBounds(227, 7, 195, 23);
        radioAuto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableControls();
            }
        });
        layeredPaneGroupType.add(radioAuto);

        buttonGroup.add(rdbtnManual);
        buttonGroup.add(radioAuto);

        labelDetectGroupInterval = new JLabel("Время простоя для начала новой группы:");
        labelDetectGroupInterval.setBounds(6, 40, 328, 14);
        layeredPaneGroupType.add(labelDetectGroupInterval);

        textFieldDetectGroupInterval = new JTextField();
        textFieldDetectGroupInterval.setText("1");
        textFieldDetectGroupInterval.setColumns(10);
        textFieldDetectGroupInterval.setBounds(344, 37, 162, 20);
        layeredPaneGroupType.add(textFieldDetectGroupInterval);

        labelAutoPause = new JLabel("Пауза между группами:");
        labelAutoPause.setBounds(6, 65, 293, 14);
        layeredPaneGroupType.add(labelAutoPause);

        textFieldAutoPause = new JTextField();
        textFieldAutoPause.setText("10");
        textFieldAutoPause.setColumns(10);
        textFieldAutoPause.setBounds(344, 62, 162, 20);
        layeredPaneGroupType.add(textFieldAutoPause);

        enableControls();
    }

    private void enableControls() {
        if (rdbtnManual.isSelected()) {
            textFieldDetectGroupInterval.setEnabled(false);
            textFieldAutoPause.setEnabled(false);
            labelDetectGroupInterval.setEnabled(false);
            labelAutoPause.setEnabled(false);
            enablePanel(layeredPaneManualGroup, true);
        } else {
            textFieldDetectGroupInterval.setEnabled(true);
            textFieldAutoPause.setEnabled(true);
            labelDetectGroupInterval.setEnabled(true);
            labelAutoPause.setEnabled(true);
            enablePanel(layeredPaneManualGroup, false);
        }
    }

    private void enablePanel(JLayeredPane panel, boolean enable) {
        for (Component component : panel.getComponents()) {
            component.setEnabled(enable);
        }
    }

    public class GuiContextListener implements ProxyContextListener {

        @Override
        public void onAddGroup(GwtInteractionGroup group) {
            listModel.addElement("Add group " + group.getName());
        }

        @Override
        public void onAddGwtInteraction(GwtInteraction requestResponse) {
            listModel.addElement("Add request " + requestResponse.getRequest().getUrl() + "." + requestResponse.getRequest().getServiceClass() + "."
                    + requestResponse.getRequest().getMethod());
        }
    }
}
