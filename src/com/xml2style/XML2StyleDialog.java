package com.xml2style;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class XML2StyleDialog extends DialogWrapper {
    private JPanel contentPane;
    private JButton btnConvert;
    private JButton btnClose;
    private JTextArea txtInput;
    private JTextArea txtOutput;

    public XML2StyleDialog(PsiClass psiClass) {
        super(psiClass.getProject());
        setModal(true);
        getRootPane().setDefaultButton(btnConvert);
        setTitle("com.xml2style.XML2Style convert");

        btnConvert.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onConvert();
            }
        });

        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        btnClose.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // TODO Get selected text from current file

        // Init
        init();
    }

    private void onCancel() {
        dispose();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    private void onConvert() {
        String input = txtInput.getText().toString();
        txtOutput.setText(convert(input));
    }

    /**
     * Do conversion logic
     *
     * @param input
     * @return
     */
    private String convert(String input) {

        // Check if input is from XML file
        if (input.contains("<item name=")) {
            return style2XML(input);
        } else {
            return xml2Style(input);
        }
    }

    /**
     * Convert xml text to style
     *
     * @param input Input text
     * @return Text to show
     */
    private String xml2Style(String input) {
        StringBuilder builder = new StringBuilder();
        String[] lines = input.split("\n");
        String style = "", parent = "";
        for (String line : lines) {
            line = line.trim();

            // Skip id
            if (!line.startsWith("android:id") && !line.equals("")) {
                if (line.startsWith("style=")) {
                    style = line.replace("style=\"@style/", "").replace("\"", "");
                    if (style.contains(".")) {
                        String[] split = style.split(".");
                        for (int i = 0; i < split.length; i++) {
                            if (i < split.length - 1) {
                                parent = parent + split[i];
                            } else {
                                style = split[i];
                            }
                        }
                    }
                } else {
                    builder.append("<item name=\"");
                    System.out.println(line);
                    int firstPart = line.indexOf("=\"");
                    builder.append(line.substring(0, firstPart));
                    builder.append("\">");
                    builder.append(line.substring(firstPart + 2, line.indexOf("\"", firstPart + 2)));
                    builder.append("</item>");
                    builder.append("\n");
                }
            }
        }
        StringBuilder finalS = new StringBuilder();
        finalS.append("<style name=\"");
        finalS.append(style);
        finalS.append("\"");
        if (!parent.equals("")) {
            finalS.append(" parent=\"");
            finalS.append(parent);
            finalS.append("\"");
        }
        finalS.append(">");
        finalS.append("\n");
        finalS.append(builder);
        finalS.append("</style>");
        return finalS.toString();
    }

    /**
     * Convert style format to XML format
     *
     * @param input
     * @return
     */
    private String style2XML(String input) {
        StringBuilder builder = new StringBuilder();
        String[] lines = input.split("\n");
        for (String line : lines) {
            line = line.trim();

            // Check if style
            if (line.startsWith("<style")) {
                builder.append("style=\"@style/");

                // Name
                int indexOfNameFirst = line.indexOf("\"") + 1;
                int indexOfNameLast = line.indexOf("\"", indexOfNameFirst);
                String name = line.substring(indexOfNameFirst, indexOfNameLast);

                // Parent
                if (line.contains("parent=\"")) {
                    int indexOfParentFirst = line.indexOf("\"", indexOfNameLast + 1) + 1;
                    int indexOfParentLast = line.indexOf("\"", indexOfParentFirst);
                    String parent = line.substring(indexOfParentFirst, indexOfParentLast);
                    builder.append(parent);
                    builder.append(".");
                }
                builder.append(name);
                builder.append("\"");
                builder.append("\n");
            } else if (line.startsWith("<item name")) {
                builder.append(line.replace("<item name=\"", "").replace("</item>", "\"").replace("\">", "=\""));
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
