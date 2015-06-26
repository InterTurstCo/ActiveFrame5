package ru.intertrust.performance.jmetertools;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class JsonViewer extends JDialog{

    public JsonViewer(Window owner, String text) {
        super(owner);
        init(text);
    }

    private void init(String text) {
        setModal(true);
        setSize(800, 600);
        setResizable(true);
        setLocationRelativeTo(getParent());
        setTitle("JSON Viewer");
        JTextArea textArea = new JTextArea();
        textArea.setText(text);
        
        JScrollPane panel = new JScrollPane(textArea);
        
        add(panel, BorderLayout.CENTER);        
    }
    
    

}
