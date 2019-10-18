package ru.intertrust.performance.jmetertools;

import java.awt.BorderLayout;
import java.awt.Window;
import java.io.UnsupportedEncodingException;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.codec.binary.Base64;

public class JsonViewer extends JDialog {

    public JsonViewer(Window owner, String text) {
        super(owner);
        init(text);
    }

    private void init(String text) {
        try {
            setModal(true);
            setSize(800, 600);
            setResizable(true);
            setLocationRelativeTo(getParent());
            setTitle("JSON Viewer");
            JTextArea textArea = new JTextArea();
            textArea.setText(new String(Base64.decodeBase64(text), "UTF-8"));

            JScrollPane panel = new JScrollPane(textArea);

            add(panel, BorderLayout.CENTER);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error init JSON View dialogs", e);
        }
    }

}
